package com.example.base.service.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.base.bean.entity.*;
import com.example.base.bean.entity.enums.DemandCondition;
import com.example.base.bean.entity.enums.MessageType;
import com.example.base.bean.entity.enums.PaymentCondition;
import com.example.base.bean.entity.enums.WorksCondition;
import com.example.base.client.redis.RedisStringClient;
import com.example.base.constant.GenericConstant;
import com.example.base.constant.RabbitMQConstant;
import com.example.base.constant.RedisConstant;
import com.example.base.controller.bean.dto.cooperate_demand.*;
import com.example.base.controller.bean.vo.OrderVo;
import com.example.base.controller.bean.vo.SysDemandVo;
import com.example.base.controller.bean.vo.SysUserVo;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.netty.pojo.MessageAction;
import com.example.base.netty.pojo.UserConnectPool;
import com.example.base.repository.*;
import com.example.base.service.admin.NoticeMessageService;
import com.example.base.service.plain.RabbitService;
import com.example.base.service.plain.SysOrderService;
import com.example.base.utils.BeanCopyUtil;
import com.example.base.utils.JsonUtil;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
//监听死信队列
@Service
public class SysCooperateDemandService {

    final SysCooperateDemandRepository sysCooperateDemandRepository;
    final SysDemandService sysDemandService;
    final NoticeMessageService noticeMessageService;
    final SysUserRepository sysUserRepository;
    final RedisStringClient redisStringClient;
    final SysDemandRepository sysDemandRepository;
    final RabbitService rabbitService;

    final SysOrderService sysOrderService;

    static final BigDecimal PAYOUT_RATIO = new BigDecimal("0.3");


    @CacheEvict(value = RedisConstant.CACHE_DEMAND_ID, key = "#insertDto.demandId")
    @Transactional
    public void insert(SysCooperateDemandInsertDto insertDto) {
        //确认需求状态
        SysDemand sysDemand = sysDemandService.queryById(insertDto.getDemandId());
        if (!Objects.equals(sysDemand.getDemandCondition(), DemandCondition.IN_BIDDING)) {
            throw GlobalRuntimeException.of("无法开始合作，需求状态不为竞拍中");
        }
        //更新状态，避免重复提交
        sysDemand.setDemandCondition(DemandCondition.IN_COOPERATION);
        sysDemandRepository.updateById(sysDemand);

        //发送消息到ES
        rabbitService.toMsg(RabbitMQConstant.DESIGNER_UPDATE_EXCHANGE, RabbitMQConstant.DEMAND_UPDATE_QUEUE, sysDemand);

        /*
        * 通知用户
        * */
        //支付订金
        String deposit = new BigDecimal(insertDto.getMoney()).multiply(PAYOUT_RATIO).toString();

        //生成支付订单
        OrderVo orderVo = sysOrderService.pay(insertDto.getDemandId(), insertDto.getUserId(), deposit);

        //发送订单到MQ
        //发送MQ消息
        SysCooperateDemand sysCooperateDemand = BeanCopyUtil.copy(insertDto, SysCooperateDemand.class);
        sysCooperateDemand.setDeposit(deposit);
        sysCooperateDemand.setPartOrder(orderVo.getId());
        //提前生成订单ID
        //sysCooperateDemandRepository.insert(sysCooperateDemand);
        rabbitService.toMsg(RabbitMQConstant.DESIGNER_EXCHANGE, RabbitMQConstant.TTL_ROUTE, sysCooperateDemand);

        //发送MQ消息
        //rabbitService.toMsg(RabbitMQConstant.DESIGNER_EXCHANGE, RabbitMQConstant.TTL_ROUTE, insertDto);

        //发送消息给用户
        sendQRCode(insertDto.getUserId(), orderVo.getUrl());

        //发送通知给用户
        SysMessage userNotice = SysMessage.sendSystemNoticeMessage(insertDto.getUserId(),
                GenericConstant.POST_FORM_SUCCESS,
                "订单编号: " + orderVo.getId() + "\n" + "支付链接: " + orderVo.getUrl());

        noticeMessageService.publishSysNoticeMessage(userNotice);

        /*
        * 通知设计师
        * */
        //发送通知给设计师
        SysMessage designerNotice = SysMessage.sendSystemNoticeMessage(insertDto.getDesignerId(), GenericConstant.COOPERATION_REQUEST, appendDesignerNoticeMessage(insertDto));
        noticeMessageService.publishSysNoticeMessage(designerNotice);

        //向双方发送合作表单
        sendCooperateForm(insertDto);
    }

    /**
     * 向用户发送在线消息
     */
    private void sendQRCode(Long userId, String payUrl) {
        UserConnectPool.send(userId, MessageAction.USER_COOPERATE_MESSAGE.ACTION, payUrl);
    }

    final SysMessageRepository sysMessageRepository;

    /**
     * 向设计师发送在线消息
     */
    private void sendCooperateForm(SysCooperateDemandInsertDto insertDto) {
        SysMessage sysMessage = SysMessage.sendNormalMessage(insertDto.getUserId(), insertDto.getDesignerId(), null, JsonUtil.toJson(insertDto));
        sysMessage.setMessageType(MessageType.COOPERATE_MESSAGE);

        //向接收者发送消息
        UserConnectPool.send(sysMessage.getReceiverId(), MessageAction.DESIGNER_CHAT_COOPERATE_MESSAGE.ACTION, sysMessage);
        //向发送者也发送表单
        UserConnectPool.send(sysMessage.getSenderId(), MessageAction.DESIGNER_CHAT_COOPERATE_MESSAGE.ACTION, sysMessage);

        //插入数据库
        sysMessageRepository.insert(sysMessage);

        //添加未读消息数
        redisStringClient.increase(RedisConstant.chatKeyFormat(sysMessage.getSenderId(), sysMessage.getReceiverId()));
    }


    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String appendDesignerNoticeMessage(SysCooperateDemandInsertDto insertDto) {
        return String.format(GenericConstant.DESIGNER_COOPERATE_NOTICE_FORMAT,
                insertDto.getUserId(),
                insertDto.getDemandId(),
                insertDto.getMoney(),
                insertDto.getDeadTime().format(formatter)
        );
    }

    final SysUserService sysUserService;

    final SysOrderRepository sysOrderRepository;

    //设置监听器监听消息
    @Transactional
    @RabbitListener(queues = RabbitMQConstant.DEAD_QUEUE)
    public void consumeMessage(Message message, Channel channel) throws IOException {

        rabbitService.accept(message, channel, SysCooperateDemand.class, sysCooperateDemand -> {
            boolean isPay = true;
            boolean isConfirm = true;
            //log.debug(message.toString());
            log.debug("body: {}", sysCooperateDemand);

            //1. 确定用户已经支付
            SysOrder sysOrder = sysOrderRepository.selectById(sysCooperateDemand.getPartOrder());
            String deposit = sysOrder.getMoney();
            if (!Objects.equals(sysOrder.getIsPay(), PaymentCondition.PAID)) {
                SysMessage sysMessage = SysMessage.sendSystemNoticeMessage(sysCooperateDemand.getUserId(),
                        GenericConstant.SYSTEM_NOTICE + "放弃合作",
                        "尊敬的用户，您在30分钟内未支付订金，视为放弃与设计师合作。");
                noticeMessageService.publishSysNoticeMessage(sysMessage);
                isPay = false;
                log.debug("用户未支付");
            }

            //2. 确认设计师已经确定
            Long designerId = redisStringClient.get(RedisConstant.DESIGNER_CONFIRM + sysCooperateDemand.getDemandId(), Long.class);
            if (designerId == null || !designerId.equals(sysCooperateDemand.getDesignerId())) {
                SysMessage sysMessage = SysMessage.sendSystemNoticeMessage(sysCooperateDemand.getDesignerId(),
                        GenericConstant.SYSTEM_NOTICE + ": 放弃合作",
                        "尊敬的设计师，您在30分钟内未同意此次需求合作，视为放弃此次合作。");
                noticeMessageService.publishSysNoticeMessage(sysMessage);
                //若设计师未确定
                isConfirm = false;
                log.debug("设计师未确定");
            }

            //删除证明记录
            //避免用户在订单关闭后支付
            sysOrderRepository.deleteById(sysCooperateDemand.getPartOrder());
            //不删除设计师记录，避免判断设计师是否答应的接口出现异常
            redisStringClient.delete(RedisConstant.DESIGNER_CONFIRM + sysCooperateDemand.getDemandId());


            if (isPay && isConfirm) {
                //初始化准备插入
                sysCooperateDemand.setDeposit(deposit);
                sysCooperateDemand.setPaymentCondition(PaymentCondition.PART_PAID);
                sysCooperateDemand.setWorksCondition(WorksCondition.UNFINISHED);
                sysCooperateDemandRepository.insert(sysCooperateDemand);

                String noticeUserFormat = "尊敬的用户，您与手机尾号为: %s的设计师合作成功，详情可在合作详情页查看";
                String noticeDesignerFormat = "尊敬的设计师，您与手机尾号为: %s的用户合作成功，详情可在合作详情页查看";

                SysUserVo user = sysUserService.queryById(sysCooperateDemand.getUserId());
                SysUserVo designer = sysUserService.queryById(sysCooperateDemand.getDesignerId());

                //通知用户
                SysMessage noticeUserMessage = SysMessage.sendSystemNoticeMessage(sysCooperateDemand.getUserId(),
                        GenericConstant.SYSTEM_NOTICE + ": 合作开始",
                        String.format(noticeUserFormat, user.getUsername().substring(7, 11))
                );

                noticeMessageService.publishSysNoticeMessage(noticeUserMessage);

                //通知设计师
                SysMessage noticeDesignerMessage = SysMessage.sendSystemNoticeMessage(sysCooperateDemand.getDesignerId(),
                        GenericConstant.SYSTEM_NOTICE + ": 合作开始",
                        String.format(noticeDesignerFormat, designer.getUsername().substring(7, 11))
                );
                noticeMessageService.publishSysNoticeMessage(noticeDesignerMessage);
            } else {
                //存在条件未通过
                //还原需求状态
                SysDemand sysDemand = new SysDemand();
                sysDemand.setDemandCondition(DemandCondition.IN_BIDDING);
                sysDemand.setId(sysCooperateDemand.getDemandId());
                sysDemandRepository.updateById(sysDemand);

                //删除缓存
                redisStringClient.delete(RedisConstant.CACHE_DEMAND_ID + "::" + sysCooperateDemand.getDemandId());

                //更新状态到ES
                rabbitService.toMsg(RabbitMQConstant.DESIGNER_UPDATE_EXCHANGE, RabbitMQConstant.DEMAND_UPDATE_QUEUE, sysDemand);
                if (isPay) {
                    //准备退款
                    SysMessage sysMessage = SysMessage.sendSystemNoticeMessage(sysCooperateDemand.getUserId(),
                            GenericConstant.SYSTEM_NOTICE + ": 退款通知",
                            String.format("尊敬的用户，抱歉设计师并没有同意与您合作，对此我们深表遗憾;\n" +
                                    "退款金额为: %s，已按原支付渠道打回您的账户，请注意查收。", deposit)
                    );
                    noticeMessageService.publishSysNoticeMessage(sysMessage);
                }
                if (isConfirm) {
                    //告知设计师，用户未付款
                    SysMessage sysMessage = SysMessage.sendSystemNoticeMessage(sysCooperateDemand.getDesignerId(),
                            GenericConstant.SYSTEM_NOTICE + ": 合作不愉快",
                            "尊敬的设计师，抱歉需求方没有在限定时间内提交订金，此次需求合作已失效，占用了您宝贵的时间，深感抱歉！"
                    );
                    noticeMessageService.publishSysNoticeMessage(sysMessage);
                }
            }
        });

    }

//    public String pay(Long id, Long money) {
//        //设置无期限，避免用户丢失钱
//        redisStringClient.set(RedisConstant.PAY + id, money, 30L, TimeUnit.MINUTES);
//        return String.format(GenericConstant.PAY_SUCCESS_FORMAT, money);
//    }

    final PasswordEncoder passwordEncoder;

    public Boolean confirm(SysCooperateDemandConfirmDto cooperateDemandConfirmDto) {
        SysUser sysUser = sysUserRepository.selectById(cooperateDemandConfirmDto.getDesignerId());
        if (sysUser == null || sysUser.getPassword() == null) {
            throw GlobalRuntimeException.of("数据库未找到对应数据");
        }
        boolean matches = passwordEncoder.matches(cooperateDemandConfirmDto.getPassword(), sysUser.getPassword());
        if (matches) {
            redisStringClient.set(RedisConstant.DESIGNER_CONFIRM + cooperateDemandConfirmDto.getDemandId(), cooperateDemandConfirmDto.getDesignerId());
        }
        return matches;
    }

    public Boolean isConfirm(Long demandId, Long designerId) {
        Long id = redisStringClient.get(RedisConstant.DESIGNER_CONFIRM + demandId, Long.class);
        return id != null && id.equals(designerId);
    }

    final SysWorksRepository sysWorksRepository;

    @Transactional
    public void upload(SysCooperateDemandWorksUploadDto uploadDto) {
        //插入作品
        SysWorks copy = BeanCopyUtil.copy(uploadDto, SysWorks.class);
        copy.setId(null);
        sysWorksRepository.insert(copy);

        SysCooperateDemand sysCooperateDemand = noticeUserPay(uploadDto.getId());


        //更新合作状态，和作品ID
        sysCooperateDemand.setId(uploadDto.getId());
        sysCooperateDemand.setWorksId(copy.getId());
        sysCooperateDemand.setWorksCondition(WorksCondition.COMPLETED);
        sysCooperateDemandRepository.updateById(sysCooperateDemand);

        //更新需求状态
        SysDemand sysDemand = new SysDemand();
        sysDemand.setId(uploadDto.getDemandId());
        sysDemand.setDemandCondition(DemandCondition.COMPLETED);
        sysDemandRepository.updateById(sysDemand);

        //更新到ES, 不进行上传，无法搜索保护隐私
        //rabbitService.toMsg(RabbitMQConstant.DESIGNER_UPDATE_EXCHANGE, RabbitMQConstant.DEMAND_UPDATE_QUEUE, sysDemand);
    }

    public SysCooperateDemand noticeUserPay(Long id) {

        SysCooperateDemand sysCooperateDemand = sysCooperateDemandRepository.selectById(id);

        //生成支付链接, 尾款订单
        String money = new BigDecimal(sysCooperateDemand.getMoney())
                .subtract(new BigDecimal(sysCooperateDemand.getDeposit())).toString();
        OrderVo orderVo = sysOrderService.pay(sysCooperateDemand.getDemandId(), sysCooperateDemand.getUserId(), money);
        String url = orderVo.getUrl();
        Long orderId = orderVo.getId();

        SysUserVo user = sysUserService.queryById(sysCooperateDemand.getUserId());
        SysUserVo designer = sysUserService.queryById(sysCooperateDemand.getDesignerId());
        SysDemandVo demand = sysDemandService.queryById(sysCooperateDemand.getDemandId());

        //发送通知给用户支付尾款
        String format = "尊敬的【%s】用户, 设计师: %s, 已为需求标题为: %s上传作品, 合作已结束。请支付尾款。\n" +
                "订单编号: %d\n" +
                "支付链接: %s";
        SysMessage sysMessage = SysMessage.sendSystemNoticeMessage(sysCooperateDemand.getUserId(),
                GenericConstant.SYSTEM_NOTICE + ": 请支付尾款",
                String.format(format, user.getNickname(), designer.getNickname(), demand.getTitle(), orderId, url));
        noticeMessageService.publishSysNoticeMessage(sysMessage);

        sysCooperateDemand.setAllOrder(orderId);
        return sysCooperateDemand;
    };

    /**
     * 生成支付URL
     */
    private static String generatePayUrl(Long id, String money) {
        return GenericConstant.POST_FORM_SUCCESS_MESSAGE + GenericConstant.ENCRYPTION_PROTOCOL + GenericConstant.HOST + String.format(GenericConstant.QRCODE_FORMAT, id, money);
    }

    public Page<SysCooperateDemand> queryByUserId(Page<SysCooperateDemand> page, Long userId, Integer workCondition) {
        return sysCooperateDemandRepository.selectPage(page,
                Wrappers.<SysCooperateDemand>lambdaQuery()
                        .eq(SysCooperateDemand::getUserId, userId)
                        .eq(SysCooperateDemand::getWorksCondition, workCondition)
                        //根据死线排序，快截止在前，即升序
                        .orderByAsc(SysCooperateDemand::getDeadTime)
                );
    }

    public Page<SysCooperateDemand> queryByDesignerId(Page<SysCooperateDemand> page, Long designerId, Integer workCondition) {
        return sysCooperateDemandRepository.selectPage(page,
                Wrappers.<SysCooperateDemand>lambdaQuery()
                        .eq(SysCooperateDemand::getDesignerId, designerId)
                        .eq(SysCooperateDemand::getWorksCondition, workCondition)
                        //根据死线排序，快截止在前，即升序
                        .orderByAsc(SysCooperateDemand::getDeadTime)
                );
    }

    @Transactional
    public void evaluate(SysCooperateDemandEvaluationDto sysCooperateDemandEvaluationDto) {
        SysCooperateDemand sysCooperateDemand = BeanCopyUtil.copy(sysCooperateDemandEvaluationDto, SysCooperateDemand.class);
        SysWorks sysWorks = BeanCopyUtil.copy(sysCooperateDemandEvaluationDto, SysWorks.class);
        //修改ID
        sysWorks.setId(sysCooperateDemandEvaluationDto.getWorksId());

        sysCooperateDemandRepository.updateById(sysCooperateDemand);
        sysWorksRepository.updateById(sysWorks);
    }

    public void renewal(RenewalDto renewalDto) {
        SysCooperateDemand sysCooperateDemand = sysCooperateDemandRepository.selectById(renewalDto.getId());
        if (renewalDto.getDeadTime().isAfter(sysCooperateDemand.getDeadTime())) {
            sysCooperateDemand.setDeadTime(renewalDto.getDeadTime());
            sysCooperateDemandRepository.updateById(sysCooperateDemand);
        }
        throw GlobalRuntimeException.of("续期时间不能比之前早");
    }

    public Boolean isCooperate(Long demandId, Long designerId) {
        Long count = sysCooperateDemandRepository.selectCount(
                Wrappers.<SysCooperateDemand>lambdaQuery()
                        .eq(SysCooperateDemand::getDemandId, demandId)
                        .eq(SysCooperateDemand::getDesignerId, designerId)
        );
        return count == 1;
    }

    public SysCooperateDemand queryById(Long id) {
        return sysCooperateDemandRepository.selectById(id);
    }

    public Boolean isPayAll(Long id) {
        SysCooperateDemand sysCooperateDemand = queryById(id);
        return sysOrderService.isPay(sysCooperateDemand.getAllOrder());
    }

    public SysCooperateDemand queryCooperate(Long demandId, Long designerId) {
        try {
            return sysCooperateDemandRepository.selectOne(
                    Wrappers.<SysCooperateDemand>lambdaQuery()
                            .eq(SysCooperateDemand::getDemandId, demandId)
                            .eq(SysCooperateDemand::getDesignerId, designerId)
                            .and(
                                    o -> o.eq(SysCooperateDemand::getWorksCondition, WorksCondition.UNFINISHED)
                                            .or()
                                            .eq(SysCooperateDemand::getWorksCondition, WorksCondition.COMPLETED)
                            )
            );
        }catch (Exception ex) {
            throw GlobalRuntimeException.of("意料之外的，重复的合作，同一个需求不应该存在两次合作!");
        }
    }

    public SysCooperateDemand queryUserCooperation(Long demandId, Long userId) {
        try {
            return sysCooperateDemandRepository.selectOne(
                    Wrappers.<SysCooperateDemand>lambdaQuery()
                            .eq(SysCooperateDemand::getDemandId, demandId)
                            .eq(SysCooperateDemand::getUserId, userId)
                            .and(
                                    o -> o.eq(SysCooperateDemand::getWorksCondition, WorksCondition.UNFINISHED)
                                            .or()
                                            .eq(SysCooperateDemand::getWorksCondition, WorksCondition.COMPLETED)
                            )
            );
        }catch (Exception ex) {
            throw GlobalRuntimeException.of("意料之外的，重复的合作，同一个需求不应该存在两次合作!");
        }

    }
}
