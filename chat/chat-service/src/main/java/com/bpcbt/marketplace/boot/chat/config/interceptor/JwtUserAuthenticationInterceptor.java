package com.bpcbt.marketplace.boot.chat.config.interceptor;

import com.bpcbt.marketplace.boot.user.api.auth.JwtAuthenticationToken;
import com.bpcbt.marketplace.boot.user.api.global.security.JwtConstants;
import com.bpcbt.marketplace.boot.user.api.global.security.jwt.JwtSecurityUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.StringUtils;

import java.security.PublicKey;

@RequiredArgsConstructor
public class JwtUserAuthenticationInterceptor implements ChannelInterceptor {

    private final PublicKey publicKey;
    private final ObjectMapper mapper;

    @SneakyThrows
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            final String header = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            if (!StringUtils.startsWithIgnoreCase(header, "bearer ")) {
                return message;
            }
            final String tokenValue = header.substring(7);
            final Claims claims = Jwts.parser()
                    .verifyWith(this.publicKey)
                    .clockSkewSeconds(45L)
                    .build()
                    .parseSignedClaims(tokenValue)
                    .getPayload();
            final JwtSecurityUser user = this.mapper.readValue(claims.get(JwtConstants.USER, String.class), JwtSecurityUser.class);
            if (user.isBlocked()) {
                return message;
            }
            final JwtAuthenticationToken authentication = new JwtAuthenticationToken(user, user.getAuthorities());
            accessor.setUser(authentication);
        }
        return message;
    }
}
