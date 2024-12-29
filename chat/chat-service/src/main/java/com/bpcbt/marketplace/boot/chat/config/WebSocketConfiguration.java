package com.bpcbt.marketplace.boot.chat.config;

import com.bpcbt.marketplace.boot.chat.config.interceptor.JwtUserAuthenticationInterceptor;
import com.bpcbt.marketplace.boot.commons.properties.JwtBackendProperties;
import com.bpcbt.marketplace.chat.api.ChatRoutes;
import com.bpcbt.marketplace.commons.util.crypto.CryptoUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final ApplicationContext context;

    public WebSocketConfiguration(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        AuthorizationManager<Message<?>> myAuthorizationRules = AuthenticatedAuthorizationManager.authenticated();
        AuthorizationChannelInterceptor authz = new AuthorizationChannelInterceptor(myAuthorizationRules);
        AuthorizationEventPublisher publisher = new SpringAuthorizationEventPublisher(this.context);
        authz.setAuthorizationEventPublisher(publisher);
        final JwtUserAuthenticationInterceptor jwtUserAuthenticationInterceptor = context.getBean(JwtUserAuthenticationInterceptor.class);
        registration.interceptors(jwtUserAuthenticationInterceptor, new SecurityContextChannelInterceptor(), authz);
    }


    @Bean
    public JwtUserAuthenticationInterceptor jwtUserAuthenticationInterceptor(ObjectMapper mapper, JwtBackendProperties jwtBackendProperties) {
        return new JwtUserAuthenticationInterceptor(CryptoUtils.getPublicKey(jwtBackendProperties.getClient2BackPublicKey()), mapper);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(ChatRoutes.WS_REGISTRY).setAllowedOrigins("*");
    }
}
