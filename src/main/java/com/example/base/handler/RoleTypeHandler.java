package com.example.base.handler;

import com.example.base.bean.entity.enums.Role;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author 雷佳宝
 */
@MappedJdbcTypes(value = JdbcType.TINYINT)
@MappedTypes(value = Role.class)
public class RoleTypeHandler extends BaseTypeHandler<Role> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Role parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.ordinal());
    }

    @Override
    public Role getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Role.search(rs.getInt(columnName));
    }

    @Override
    public Role getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Role.search(rs.getInt(columnIndex));
    }

    @Override
    public Role getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Role.search(cs.getInt(columnIndex));
    }
}
