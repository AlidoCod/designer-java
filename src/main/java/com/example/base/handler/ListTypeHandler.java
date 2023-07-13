package com.example.base.handler;

import com.example.base.utils.JsonUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author 雷佳宝
 */
@MappedJdbcTypes(value = JdbcType.VARCHAR)
@MappedTypes(value = List.class)
public class ListTypeHandler extends BaseTypeHandler<List> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JsonUtil.toJson(parameter));
    }

    @Override
    public List getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return JsonUtil.toPojo(rs.getString(columnName), List.class);
    }

    @Override
    public List getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return JsonUtil.toPojo(rs.getString(columnIndex), List.class);
    }

    @Override
    public List getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return JsonUtil.toPojo(cs.getString(columnIndex), List.class);
    }
}
