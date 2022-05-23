package com.miotech.kun.security.dao;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.security.model.bo.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RoleDao {

    private static final String ROLE_TABLE_NAME = "kun_security_role";

    private static final List<String> roleCols = ImmutableList.of("module", "name", "description");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(Role role) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(roleCols.toArray(new String[0]))
                .into(ROLE_TABLE_NAME)
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(sql, role.getModule(), role.getName(), role.getDescription());
    }

    public Role findByModuleAndName(String module, String name) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(roleCols.toArray(new String[0]))
                .from(ROLE_TABLE_NAME)
                .where("module = ? and name = ?")
                .getSQL();

        return jdbcTemplate.queryForObject(sql, RoleMapper.INSTANCE, module, name);
    }

    public static class RoleMapper implements RowMapper<Role> {
        public static final RoleDao.RoleMapper INSTANCE = new RoleDao.RoleMapper();

        @Override
        public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
            Role role = new Role();
            role.setModule(rs.getString("module"));
            role.setName(rs.getString("name"));
            role.setDescription(rs.getString("description"));
            return role;
        }
    }

}
