package com.bpcbt.marketplace.chat.api.reverse_proxy_config;

import com.bpcbt.marketplace.boot.user.api.global.security.jwt.JwtSecurityUser;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.crypto.SecretKey;
import java.security.Principal;
import java.util.function.Function;

public class ProxyWebSocketConfigurer implements WebSocketConfigurer {

    private final String wsPath;
    private final WebSocketClient webSocketClient;
    private final String wsPathToProxyServer;
    private final Function<Principal, JwtSecurityUser> funcGetJwtTokenByPrincipal;
    private final SecretKey secretKey;

    public ProxyWebSocketConfigurer(String path, Function<Principal, JwtSecurityUser> funcGetJwtSecurityUser,
                                    WebSocketClient webSocketClient, String wsPathToProxyServer, SecretKey secretKey) {
        this.wsPath = path;
        this.funcGetJwtTokenByPrincipal = funcGetJwtSecurityUser;
        this.webSocketClient = webSocketClient;
        this.wsPathToProxyServer = wsPathToProxyServer;
        this.secretKey = secretKey;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        final WebSocketProxyServerHandler handler = new WebSocketProxyServerHandler(webSocketClient, funcGetJwtTokenByPrincipal, wsPathToProxyServer, secretKey);
        registry.addHandler(handler, wsPath).setAllowedOrigins("*");
    }
}
