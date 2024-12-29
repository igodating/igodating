package com.bpcbt.marketplace.chat.api.reverse_proxy_config;

import com.bpcbt.marketplace.boot.user.api.global.security.JwtConstants;
import com.bpcbt.marketplace.boot.user.api.global.security.jwt.JwtSecurityUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Log4j2
public class ServerProxy {
    private final WebSocketSession webSocketClientSession;
    private final WebSocketClient webSocketClient;
    private final ObjectMapper objectMapper;
    private final SecretKey secretKey;

    public ServerProxy(WebSocketSession webSocketServerSession,
                       WebSocketProxyClientHandler.ClientOfflineListener listener,
                       WebSocketClient webSocketClient,
                       String urlToConnect,
                       Function<Principal, JwtSecurityUser> funcGetJwtTokenByPrincipal,
                       SecretKey secretKey) {
        this.webSocketClient = webSocketClient;
        this.objectMapper = new ObjectMapper();
        this.secretKey = secretKey;
        this.webSocketClientSession = createWebSocketClientSession(webSocketServerSession, listener, urlToConnect, funcGetJwtTokenByPrincipal);
    }

    @SneakyThrows
    private WebSocketHttpHeaders getWebSocketHttpHeaders(WebSocketSession userAgentSession, Function<Principal, JwtSecurityUser> funcGetJwtTokenByPrincipal) {
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        final JwtSecurityUser jwtSecurityUser = funcGetJwtTokenByPrincipal.apply(userAgentSession.getPrincipal());
        final String token = Jwts.builder()
                .subject(String.valueOf(jwtSecurityUser.getId()))
                .claim(JwtConstants.USER, objectMapper.writeValueAsString(jwtSecurityUser))
                .claim(JwtConstants.AUTHORITIES, JwtConstants.BACKEND_AUTHORITY)
                .signWith(this.secretKey)
                .compact();
        headers.put(JwtConstants.HEADER_BACKEND_AUTHORIZATION, Collections.singletonList("Bearer " + token));
        return headers;
    }

    private WebSocketSession createWebSocketClientSession(WebSocketSession webSocketServerSession,
                                                          WebSocketProxyClientHandler.ClientOfflineListener listener,
                                                          String urlToConnect,
                                                          Function<Principal, JwtSecurityUser> funcGetJwtTokenByPrincipal) {
        try {
            WebSocketHttpHeaders headers = getWebSocketHttpHeaders(webSocketServerSession, funcGetJwtTokenByPrincipal);
            return webSocketClient
                    .execute(new WebSocketProxyClientHandler(webSocketServerSession, listener), headers, new URI(urlToConnect))
                    .get(100000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessageToNextServer(WebSocketMessage<?> webSocketMessage) throws IOException {
        webSocketClientSession.sendMessage(webSocketMessage);
    }

    public void close() throws IOException {
        webSocketClientSession.close();
    }
}
