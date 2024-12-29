package com.bpcbt.marketplace.boot.chat.config;

import com.bpcbt.marketplace.boot.commons.config.ExceptionDetailsConfiguration;
import com.bpcbt.marketplace.boot.commons.properties.JwtBackendProperties;
import com.bpcbt.marketplace.boot.content.api.connector.ContentServiceConnector;
import com.bpcbt.marketplace.boot.events_log.aspect.RestTemplateAuditHeadersProxyInterceptor;
import com.bpcbt.marketplace.boot.user.api.auth.BackendHeaderThreadLocalServletExchangeFilterFunction;
import com.bpcbt.marketplace.boot.user.api.auth.RestTemplateBackendHeaderUserAuthorizedModifierInterceptor;
import com.bpcbt.marketplace.boot.user.api.connector.UserServiceConnector;
import com.bpcbt.marketplace.boot.user.api.global.security.JwtConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.reactive.DeferringLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableScheduling
@EnableMethodSecurity
@Import({ExceptionDetailsConfiguration.class})
public class AppConfig {

    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate(RestTemplateBuilder builder,
                                                 @Autowired(required = false) RestTemplateAuditHeadersProxyInterceptor restTemplateAuditHeadersProxyInterceptor,
                                                 @Value("${spring.application.name}") String appName,
                                                 ObjectMapper objectMapper,
                                                 JwtBackendProperties jwtBackendProperties) {

        List<ClientHttpRequestInterceptor> clientHttpRequestInterceptors = new ArrayList<>();
        clientHttpRequestInterceptors.add(new RestTemplateBackendHeaderUserAuthorizedModifierInterceptor("Bearer " + Jwts.builder()
                .issuer(appName)
                .claim(JwtConstants.AUTHORITIES, JwtConstants.BACKEND_AUTHORITY)
                .signWith(Keys.hmacShaKeyFor(jwtBackendProperties.getBack2BackKey().getBytes()))
                .compact(),
                objectMapper,
                appName, Keys.hmacShaKeyFor(jwtBackendProperties.getBack2BackKey().getBytes())));
        if (restTemplateAuditHeadersProxyInterceptor != null) {
            clientHttpRequestInterceptors.add(restTemplateAuditHeadersProxyInterceptor);
        }

        return builder.additionalInterceptors(clientHttpRequestInterceptors)
                .build();
    }

    @Bean
    public BackendHeaderThreadLocalServletExchangeFilterFunction backendHeaderFilterFunction(ObjectMapper objectMapper,
                                                                                             @Value("${spring.application.name}") String appName,
                                                                                             JwtBackendProperties jwtBackendProperties) {
        return new BackendHeaderThreadLocalServletExchangeFilterFunction(objectMapper, appName, Keys.hmacShaKeyFor(jwtBackendProperties.getBack2BackKey().getBytes()));
    }

    @Bean
    public WebClient loadBalancedWebClient(DeferringLoadBalancerExchangeFilterFunction<?> function,
                                           BackendHeaderThreadLocalServletExchangeFilterFunction filterFunction,
                                           @Value("${spring.application.name}") String appName, WebClient.Builder builder,
                                           JwtBackendProperties jwtBackendProperties) {
        return builder
                .filter(function)
                .filter(filterFunction)
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(Math.toIntExact(DataSize.ofMegabytes(2).toBytes())))
                .defaultHeader(JwtConstants.HEADER_BACKEND_AUTHORIZATION, "Bearer " + Jwts.builder()
                        .issuer(appName)
                        .claim(JwtConstants.AUTHORITIES, JwtConstants.BACKEND_AUTHORITY)
                        .signWith(Keys.hmacShaKeyFor(jwtBackendProperties.getBack2BackKey().getBytes()))
                        .compact())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
