package com.miotech.kun.security;

import com.miotech.kun.security.common.CustomAuthenticationFilter;
import com.miotech.kun.security.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
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
@ConditionalOnExpression("#{environment.getActiveProfiles()[0] != 'test'}") // disable this config in testing
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    SecurityService securityService;

    @Value("${spring.ldap.urls}")
    private String[] ldapUrls;

    @Value("${spring.ldap.base}")
    private String ldapRootBase;

    @Value("${security.ldap.user-dn-pattern}")
    private String userDnPattern;

    @Value("${security.ldap.user-search-base}")
    private String userSearchBase;

    @Value("${security.pass-token:40A4C5379B73F31D6CD24F6A7C5C3ACB}")
    private String passToken;

    @Value("${security.pdf-coa-pass-token:54BF50AFC104E2AE80165EF89D2161A1}")
    private String pdfCoaPassToken;

    private String apiPrefix = "/kun/api";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable();
        http
                .authorizeRequests()
                .antMatchers(apiPrefix + "/**")
                .authenticated()
                .and()
                .addFilterBefore(
                        customAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .logout()
                .logoutUrl(apiPrefix + "/v1/user/logout")
                .logoutSuccessHandler(securityService.logoutSuccessHandler())

                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .ldapAuthentication()
                .userDnPatterns(userDnPattern)
                .groupSearchBase(userSearchBase)
                .contextSource()
                .url(ldapUrls[0] + "/" + ldapRootBase);
    }

    @Bean
    public AbstractAuthenticationProcessingFilter customAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter authenticationFilter = new CustomAuthenticationFilter();
        authenticationFilter.setAuthenticationSuccessHandler(securityService.loginSuccessHandler());
        authenticationFilter.setAuthenticationFailureHandler(securityService.loginFailureHandler());
        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(apiPrefix + "/v1/user/login", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        authenticationFilter.setSecurityService(securityService);
        authenticationFilter.setPassToken(passToken);
        authenticationFilter.setPdfCoaPassToken(pdfCoaPassToken);
        return authenticationFilter;
    }

}
