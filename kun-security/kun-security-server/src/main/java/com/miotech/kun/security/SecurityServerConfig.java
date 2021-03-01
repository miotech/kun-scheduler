package com.miotech.kun.security;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.security.authenticate.DefaultSecurityService;
import com.miotech.kun.security.authenticate.filter.OAuth2AuthorizationCodeFilter;
import com.miotech.kun.security.authenticate.filter.PassTokenFilter;
import com.miotech.kun.security.authenticate.filter.SyncUserInfoFilter;
import com.miotech.kun.security.authenticate.filter.UsernamePasswordAuthenticationFilter;
import com.miotech.kun.security.model.constant.SecurityType;
import com.miotech.kun.security.saml2.ResponseToAuthenticationConverter;
import com.miotech.kun.security.saml2.Saml2AuthorityAttributeLookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import javax.servlet.Filter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityServerConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    DefaultSecurityService defaultSecurityService;

    @Autowired
    @Qualifier("defaultUserDetailsService")
    UserDetailsService userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${security.auth.type}")
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

    @Value("${spring.security.oauth2.client.enable:false}")
    private Boolean oauth2ClientEnable;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Autowired
    @Qualifier("kunAuthProvider")
    private AuthenticationProvider customAuthProvider;

    private String apiPrefix = "/kun/api";

    private final Saml2AuthorityAttributeLookup saml2AuthorityAttributeLookup;

    private final Saml2RelyingPartyProperties saml2RelyingPartyProperties;

    private final OAuth2ClientProperties oAuth2ClientProperties;

    public SecurityServerConfig(Saml2AuthorityAttributeLookup lookup,
                                Saml2RelyingPartyProperties saml2RelyingPartyProperties,
                                OAuth2ClientProperties oAuth2ClientProperties) {
        this.saml2AuthorityAttributeLookup = lookup;
        this.saml2RelyingPartyProperties = saml2RelyingPartyProperties;
        this.oAuth2ClientProperties = oAuth2ClientProperties;
    }

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
                .addFilterAfter(passTokenFilter(), LogoutFilter.class)
                .addFilterAfter(usernamePasswordAuthenticationFilter(), PassTokenFilter.class)
                .addFilterAfter(oauth2AuthorizationCodeFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(syncUserInfoFilter(), OAuth2AuthorizationCodeFilter.class)
                .logout()
                .logoutUrl(apiPrefix + "/v1/security/logout")
                .logoutSuccessHandler(defaultSecurityService.logoutSuccessHandler())
                // 无效会话
                .invalidateHttpSession(true)
                // 清除身份验证
                .clearAuthentication(true)

                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

        if (oauth2ClientEnable) {
            http.oauth2Login();
        }

        saml2Configure(http);

        http.authorizeRequests().anyRequest().permitAll();
    }

    private void saml2Configure(HttpSecurity http) throws Exception {
        OpenSamlAuthenticationProvider authenticationProvider = new OpenSamlAuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(
                new ResponseToAuthenticationConverter(saml2AuthorityAttributeLookup));
        http
                .saml2Login(saml2 -> {
                    saml2.authenticationManager(new ProviderManager(authenticationProvider));
                    saml2.defaultSuccessUrl(frontendUrl);
                });

        Saml2RelyingPartyProperties.Registration registration = saml2RelyingPartyProperties.getRegistration().get("okta");
        registration.getAcs().setLocation(frontendUrl + "/api/login/saml2/sso/okta");
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        switch (securityType) {
            case DAO:
                auth
                        .userDetailsService(userDetailsService)
                        .passwordEncoder(passwordEncoder);

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
                auth
                        .authenticationProvider(customAuthProvider);
                break;
            default:
                throw ExceptionUtils.wrapIfChecked(new RuntimeException("Unsupported security type: " + securityType));
        }
    }

    @Bean
    public AbstractAuthenticationProcessingFilter usernamePasswordAuthenticationFilter() throws Exception {
        UsernamePasswordAuthenticationFilter authenticationFilter = new UsernamePasswordAuthenticationFilter(apiPrefix + "/v1/security/login");
        authenticationFilter.setAuthenticationSuccessHandler(defaultSecurityService.loginSuccessHandler());
        authenticationFilter.setAuthenticationFailureHandler(defaultSecurityService.loginFailureHandler());
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationFilter;
    }

    private AbstractAuthenticationProcessingFilter oauth2AuthorizationCodeFilter() throws Exception {
        OAuth2AuthorizationCodeFilter oauth2AuthorizationCodeFilter = new OAuth2AuthorizationCodeFilter(apiPrefix + "/v1/security/oauth2/token");
        oauth2AuthorizationCodeFilter.setAuthenticationSuccessHandler(defaultSecurityService.loginSuccessHandler());
        oauth2AuthorizationCodeFilter.setAuthenticationFailureHandler(defaultSecurityService.loginFailureHandler());
        oauth2AuthorizationCodeFilter.setAuthenticationManager(authenticationManagerBean());
        oauth2AuthorizationCodeFilter.setClientProperties(oAuth2ClientProperties);
        return oauth2AuthorizationCodeFilter;
    }

    private Filter passTokenFilter() {
        PassTokenFilter passTokenFilter = new PassTokenFilter();
        passTokenFilter.setPassToken(passToken);
        return passTokenFilter;
    }

    private Filter syncUserInfoFilter() {
        SyncUserInfoFilter syncUserInfoFilter = new SyncUserInfoFilter();
        syncUserInfoFilter.setDefaultSecurityService(defaultSecurityService);
        return syncUserInfoFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}