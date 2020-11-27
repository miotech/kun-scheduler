package com.miotech.kun.security;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.security.authenticate.CustomAuthenticationFilter;
import com.miotech.kun.security.authenticate.JsonAuthenticateProvider;
import com.miotech.kun.security.model.constant.SecurityType;
import com.miotech.kun.security.service.AbstractSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityServerConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AbstractSecurityService abstractSecurityService;

    @Value("${security.auth.type:JSON}")
    SecurityType securityType;

    @Value("${spring.ldap.urls:}")
    private String[] ldapUrls;

    @Value("${spring.ldap.base:}")
    private String ldapRootBase;

    @Value("${spring.ldap.user-dn-pattern:}")
    private String userDnPattern;

    @Value("${spring.ldap.user-search-base:}")
    private String userSearchBase;

    @Value("${security.pass-token:***REMOVED***}")
    private String passToken;

    @Autowired
    @Qualifier("kunAuthProvider")
    private AuthenticationProvider customAuthProvider;

    private String apiPrefix = "/kun/api";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable();
        http
                .authorizeRequests()
                .antMatchers("/kun/api/**")
                .authenticated()
                .and()
                .authorizeRequests()
                .anyRequest()
                .permitAll()
                .and()
                .addFilterBefore(
                        customAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .logout()
                .logoutUrl(apiPrefix + "/v1/security/logout")
                .logoutSuccessHandler(abstractSecurityService.logoutSuccessHandler())

                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        switch (securityType) {
            case JSON:
                auth.authenticationProvider(new JsonAuthenticateProvider());
                break;
            case LDAP:
                auth
                        .ldapAuthentication()
                        .userDnPatterns(userDnPattern)
                        .groupSearchBase(userSearchBase)
                        .contextSource()
                        .url(ldapUrls[0] + "/" + ldapRootBase);
                break;
            case CUSTOM:
                auth.authenticationProvider(customAuthProvider);
                break;
            default:
                throw ExceptionUtils.wrapIfChecked(new RuntimeException("Unsupported security type: " + securityType));
        }

    }

    @Bean
    public AbstractAuthenticationProcessingFilter customAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter authenticationFilter = new CustomAuthenticationFilter();
        authenticationFilter.setAuthenticationSuccessHandler(abstractSecurityService.loginSuccessHandler());
        authenticationFilter.setAuthenticationFailureHandler(abstractSecurityService.loginFailureHandler());
        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(apiPrefix + "/v1/security/login", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        authenticationFilter.setAbstractSecurityService(abstractSecurityService);
        authenticationFilter.setPassToken(passToken);
        return authenticationFilter;
    }
}