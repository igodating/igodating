package com.bpcbt.marketplace.chat.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;

//@Configuration
public class ChatWsConfig {

    @Value("${spring.rabbit.host}")
    private String rabbitHost;

    //   @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay("/queue/", "/topic/")
                .setUserDestinationBroadcast("/topic/unresolved.user.dest")
                .setUserRegistryBroadcast("/topic/registry.broadcast")
                .setRelayHost(rabbitHost)
                .setRelayPort(15672);
        registry.setApplicationDestinationPrefixes("/chats");
    }
}
