package com.bpcbt.marketplace.boot.chat.config;

import com.bpcbt.marketplace.boot.commons.NoOpAuthenticationManager;
import com.bpcbt.marketplace.boot.commons.properties.ApplicationClientsProperties;
import com.bpcbt.marketplace.boot.commons.properties.JwtBackendProperties;
import com.bpcbt.marketplace.boot.commons.web.config.ActuatorSecurityConfig;
import com.bpcbt.marketplace.boot.commons.web.config.SwaggerSecurityConfig;
import com.bpcbt.marketplace.boot.user.api.auth.Http401AuthenticationEntryPoint;
import com.bpcbt.marketplace.boot.user.api.auth.JwtBackendAuthenticationFilter;
import com.bpcbt.marketplace.boot.user.api.auth.JwtUserAuthenticationFilter;
import com.bpcbt.marketplace.chat.api.ChatRoutes;
import com.bpcbt.marketplace.commons.util.crypto.CryptoUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration(proxyBeanMethods = false)
@Import({ActuatorSecurityConfig.class, SwaggerSecurityConfig.class})
public class SecurityConfig {

    @Bean
    public HttpFirewall defaultHttpFirewall() {
        return new DefaultHttpFirewall();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public JwtUserAuthenticationFilter jwtUserAuthenticationFilter(ObjectMapper mapper, JwtBackendProperties jwtBackendProperties) {
        return new JwtUserAuthenticationFilter(CryptoUtils.getPublicKey(jwtBackendProperties.getClient2BackPublicKey()),
                new Http401AuthenticationEntryPoint(),
                mapper,
                new AntPathRequestMatcher(ChatRoutes.ROOT + "/**")
        );
    }

    @Bean
    public JwtBackendAuthenticationFilter jwtBackendAuthenticationFilter(JwtBackendProperties jwtBackendProperties, ObjectMapper mapper) {
        return new JwtBackendAuthenticationFilter(new Http401AuthenticationEntryPoint(), Keys.hmacShaKeyFor(jwtBackendProperties.getBack2BackKey().getBytes()), mapper);
    }

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager() {
        return MessageMatcherDelegatingAuthorizationManager
                .builder()
                .anyMessage().authenticated()
                .build();
    }

    @Configuration(proxyBeanMethods = false)
    static class MainSecurityConfig {
        private final JwtUserAuthenticationFilter jwtUserAuthenticationFilter;
        private final JwtBackendAuthenticationFilter jwtBackendAuthenticationFilter;
        private final ApplicationClientsProperties applicationClientsProperties;
        private final PasswordEncoder passwordEncoder;
        private final boolean swaggerEnabled;

        MainSecurityConfig(@Qualifier("jwtUserAuthenticationFilter") JwtUserAuthenticationFilter jwtUserAuthenticationFilter,
                           JwtBackendAuthenticationFilter jwtBackendAuthenticationFilter,
                           ApplicationClientsProperties applicationClientsProperties, PasswordEncoder passwordEncoder,
                           @Value("${springdoc.api-docs.enabled:false}") boolean swaggerEnabled) {
            this.jwtUserAuthenticationFilter = jwtUserAuthenticationFilter;
            this.jwtBackendAuthenticationFilter = jwtBackendAuthenticationFilter;
            this.applicationClientsProperties = applicationClientsProperties;
            this.passwordEncoder = passwordEncoder;
            this.swaggerEnabled = swaggerEnabled;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            var config = http.authorizeHttpRequests(req -> req.anyRequest().permitAll());
            config
                    .headers(AbstractHttpConfigurer::disable)
                    .sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .logout(AbstractHttpConfigurer::disable)
                    .csrf(AbstractHttpConfigurer::disable)
                    .addFilterAt(jwtUserAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(jwtBackendAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .authenticationManager(this.getAuthManager(http));
            if (swaggerEnabled) {
                http.httpBasic(Customizer.withDefaults());
            }
            return http.build();
        }

        private AuthenticationManager getAuthManager(HttpSecurity http) throws Exception {
            if (swaggerEnabled) {
                return SwaggerSecurityConfig.swaggerAuthenticationManager(http, applicationClientsProperties, passwordEncoder);
            } else {
                return new NoOpAuthenticationManager();
            }
        }
    }
}
