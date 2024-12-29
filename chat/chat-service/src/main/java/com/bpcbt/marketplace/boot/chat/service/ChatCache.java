package com.bpcbt.marketplace.boot.chat.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
//TODO Надо переделывать. Изначально была задумка что бы сделать получение юзеров для чата быстрее,
// но при масштабировании будет полная параша - надо либо редис юзать либо что то менять.
public class ChatCache {

    ChatService chatService;

    public ChatCache(@Lazy ChatService chatService) {
        this.chatService = chatService;
    }

    LoadingCache<Long, Set<Long>> chatId2Members = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofHours(6))
            .build(new CacheLoader<>() {
                @Override
                public Set<Long> load(Long chatId) {
                    return chatService.getMembersByChatId(chatId);
                }
            });

    public Set<Long> getMembersByChat(long chatId) {
        return chatId2Members.getUnchecked(chatId);
    }

    public Set<Long> getMembersByChats(Collection<Long> chatIds) {
        try {
            return chatId2Members.getAll(chatIds).values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        } catch (ExecutionException e) {
            throw new RuntimeException("Cant load members from chat id to chat cache");
        }
    }

    public void invalidate(long chatId) {
        chatId2Members.invalidate(chatId);
    }

}
