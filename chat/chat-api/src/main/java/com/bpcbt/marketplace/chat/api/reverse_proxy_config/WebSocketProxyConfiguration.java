package com.bpcbt.marketplace.chat.api.reverse_proxy_config;

import com.bpcbt.marketplace.boot.commons.load_balanced_web_socket.ConfigureLoadBalancedWebSocketBeanPostProcessor;
import com.bpcbt.marketplace.boot.commons.load_balanced_web_socket.LoadBalancedWebSocketClient;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Configuration
public class WebSocketProxyConfiguration {

    @Bean
    @LoadBalancedWebSocketClient
    public WebSocketClient webSocketClient() {
        return new StandardWebSocketClient();
    }

    @Bean
    public BeanPostProcessor configureLoadBalancedWebSocketPostProcessor(ApplicationContext applicationContext, LoadBalancerClient client) {
        return new ConfigureLoadBalancedWebSocketBeanPostProcessor(applicationContext, client);
    }
}
