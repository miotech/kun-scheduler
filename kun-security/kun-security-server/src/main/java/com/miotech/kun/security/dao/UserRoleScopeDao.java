package com.miotech.kun.security.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.security.model.bo.UserRoleScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRoleScopeDao {

    private static final String USER_ROLE_SCOPE_TABLE_NAME = "kun_security_user_role_scope";
    private static final List<String> userRoleScopeCols = ImmutableList.of("username", "module", "rolename", "source_system_id");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<UserRoleScope> findByUsernameAndModule(String username, String module) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(userRoleScopeCols.toArray(new String[0]))
                .from(USER_ROLE_SCOPE_TABLE_NAME, "t1")
                .where("username = ? and module = ?")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            List<UserRoleScope> userRoleScopes = Lists.newArrayList();
            while (rs.next()) {
                UserRoleScope userRoleScope = new UserRoleScope();
                userRoleScope.setUsername(rs.getString("username"));
                userRoleScope.setModule(rs.getString("module"));
                userRoleScope.setRolename(rs.getString("rolename"));
                userRoleScope.setSourceSystemId(rs.getString("source_system_id"));
                userRoleScopes.add(userRoleScope);
            }

            return userRoleScopes;
        }, username, module);
    }

    public List<UserRoleScope> findByModuleAndRolenameAndSourceSystemId(String module, String rolename, String sourceSystemId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(userRoleScopeCols.toArray(new String[0]))
                .from(USER_ROLE_SCOPE_TABLE_NAME, "t1")
                .where("module = ? and rolename = ? and source_system_id = ?")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            List<UserRoleScope> userRoleScopes = Lists.newArrayList();
            while (rs.next()) {
                UserRoleScope userRoleScope = new UserRoleScope();
                userRoleScope.setUsername(rs.getString("username"));
                userRoleScope.setModule(rs.getString("module"));
                userRoleScope.setRolename(rs.getString("rolename"));
                userRoleScope.setSourceSystemId(rs.getString("source_system_id"));
                userRoleScopes.add(userRoleScope);
            }

            return userRoleScopes;
        }, module, rolename, sourceSystemId);
    }

    public void create(UserRoleScope userRoleScope) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(userRoleScopeCols.toArray(new String[0]))
                .into(USER_ROLE_SCOPE_TABLE_NAME)
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(sql, userRoleScope.getUsername(), userRoleScope.getModule(),
                userRoleScope.getRolename(), userRoleScope.getSourceSystemId());
    }

    public void delete(UserRoleScope userRoleScope) {
        String sql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(USER_ROLE_SCOPE_TABLE_NAME)
                .where("username = ? and module = ? and rolename = ? and source_system_id = ?")
                .getSQL();
        jdbcTemplate.update(sql, userRoleScope.getUsername(), userRoleScope.getModule(),
                userRoleScope.getRolename(), userRoleScope.getSourceSystemId());
    }

}
