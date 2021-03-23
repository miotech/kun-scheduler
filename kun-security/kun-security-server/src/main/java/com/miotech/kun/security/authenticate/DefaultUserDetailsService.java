package com.miotech.kun.security.authenticate;

import com.miotech.kun.security.model.bo.HasPermissionRequest;
import com.miotech.kun.security.model.constant.EntityType;
import com.miotech.kun.security.model.entity.Permissions;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.persistence.UserRepository;
import com.miotech.kun.security.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2021/1/29
 */
@Service("defaultUserDetailsService")
public class DefaultUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByName(username);
        HasPermissionRequest hasPermissionRequest = HasPermissionRequest.builder()
                .subjectId(user.getId())
                .subjectType(EntityType.USER)
                .build();
        Permissions permissions = permissionService.find(hasPermissionRequest);
        return DefaultUserDetails.builder()
                .username(user.getName())
                .password(user.getPassword())
                .authorities(convertAuthority(permissions))
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .isAccountNonLocked(true)
                .isEnabled(true)
                .build();
    }

    private List<GrantedAuthority> convertAuthority(Permissions permissions) {
        return permissions.getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.toPermissionString()))
                .collect(Collectors.toList());
    }
}
