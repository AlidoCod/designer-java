package com.example.base.service.plain;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.base.bean.entity.SysResource;
import com.example.base.conf.ResourceProperties;
import com.example.base.constant.GenericConstant;
import com.example.base.constant.RedisConstant;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.repository.SysResourceRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysResourceService implements WebMvcConfigurer {

    final SysResourceRepository resourceRepository;
    final MinioService minioService;
    static final String BUCKET = "resource";

    public Long check(String md5) {
        SysResource resource = resourceRepository.selectOne(new QueryWrapper<SysResource>().eq("md5", md5));
        return resource == null ? null : resource.getId();
    }

    @Transactional
    public Long upload(String md5, String suffix, MultipartFile file) {
        if (!verifyMd5(md5, file)) {
            //校验失败
            throw GlobalRuntimeException.of("文件损坏，上传失败");
        }
        //log.error(file.getOriginalFilename());
        //log.error(file.getName()); 这个是@RequestParam参数名
        String path = getFileFolder() + md5 + suffix;
        SysResource resource = SysResource.builder()
                .md5(md5)
                .contentType(file.getContentType())
                .path(path).build();
        int insert = resourceRepository.insert(resource);
        if (insert == 1) {
            try {
                minioService.upload(BUCKET, file.getInputStream(), path, file.getContentType());
                return resource.getId();
            } catch (Exception ex) {
                log.warn("", ex);
                throw GlobalRuntimeException.of("文件上传到文件资源服务器失败");
            }
        }
        log.error("insert: {}", insert);
        throw GlobalRuntimeException.of("文件资源上传数据库失败，文件资源是否检验成功?");
    }

    static final String CONTENT_DISPOSITION = "Content-Disposition";

    public void download(HttpServletResponse response, Long id) {
        SysResource resource = resourceRepository.selectById(id);
        Optional.ofNullable(resource).orElseThrow(() -> GlobalRuntimeException.of("传入的ID不存在，无法获取文件资源"));
        try {
            response.setContentType(resource.getContentType());
            String filename = resource.getPath().substring(resource.getPath().lastIndexOf('/') + 1);
            log.debug("filename: {}", filename);
            String msg = "inline;filename=" + filename;
            response.setHeader(CONTENT_DISPOSITION, msg);
            minioService.download(BUCKET, resource.getPath(), response.getOutputStream());
        } catch (Exception ex) {
            log.warn("", ex);
            throw GlobalRuntimeException.of("文件下载失败");
        }
    }

    final BeanFactory beanFactory;

    public SysResource queryById(Long id) {
        SysResourceService sysResourceService = beanFactory.getBean(SysResourceService.class);
        return sysResourceService.query(id);
    }

    @Cacheable(value = RedisConstant.RESOURCE, key = "#root.args[0]")
    public SysResource query(Long id) {
        return resourceRepository.selectById(id);
    }

    public String download(Long id) {
        if (id == null || id.equals(0L)) {
            return null;
        }
        SysResource resource = queryById(id);
        Optional.ofNullable(resource).orElseThrow(() -> GlobalRuntimeException.of("传入的ID不存在，无法获取文件资源"));
        String filename = resource.getPath().substring(resource.getPath().lastIndexOf('/') + 1);
        File tmp = new File(resourceProperties.getLocalPath().substring(0, resourceProperties.getLocalPath().length() - 1) ,filename);
        try (FileOutputStream outputStream = new FileOutputStream(tmp);
             InputStream inputStream = minioService.getDownloadInputStream(BUCKET, resource.getPath())
        ) {
            inputStream.transferTo(outputStream);
            outputStream.flush();
            return GenericConstant.ENCRYPTION_PROTOCOL +
                    GenericConstant.HOST +
                    resourceProperties.getUrlPath().substring(0, resourceProperties.getUrlPath().lastIndexOf('/') + 1) +
                    filename;
        } catch (Exception ex) {
            log.warn("", ex);
            throw GlobalRuntimeException.of("文件下载失败");
        }
    }


    private String phraseSuffix(String filename) {
        return filename.substring(filename.lastIndexOf('.'));
    }

    private boolean verifyMd5(String md5, MultipartFile multipartFile) {
//        if (md5 != null) {
//            return true;
//        }
//        if (md5 == null) {
//            return true;
//        }
        log.debug(md5);
        String digestAsHex;
        try {
            digestAsHex = DigestUtils.md5DigestAsHex(multipartFile.getBytes());
            log.debug(digestAsHex);
        } catch (IOException ex) {
            log.warn("", ex);
            throw GlobalRuntimeException.of("md5校验异常");
        }
        return digestAsHex.equals(md5);
    }

    private String getFileFolder() {
        LocalDate now = LocalDate.now();
        String path = now.toString();
        path = path.substring(0, path.indexOf("-"));
        return "/" + path.replace("-", "/") + "/";
    }

    final ResourceProperties resourceProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(resourceProperties.getUrlPath())
                .addResourceLocations("file:" + resourceProperties.getLocalPath());
    }
}
