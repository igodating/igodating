package com.bpcbt.marketplace.boot.chat.controller;

import com.bpcbt.marketplace.boot.chat.service.ChatService;
import com.bpcbt.marketplace.boot.chat.service.ChatWsRoutes;
import com.bpcbt.marketplace.chat.api.ChatRoutes;
import com.bpcbt.marketplace.chat.api.request.*;
import com.bpcbt.marketplace.chat.api.response.ChatPaginationShared;
import com.bpcbt.marketplace.chat.api.response.ChatShared;
import com.bpcbt.marketplace.chat.api.response.ChatSummaryResponse;
import com.bpcbt.marketplace.chat.api.response.MessageShared;
import com.igodating.commons.model.page.Page;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {
    ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(ChatRoutes.External.CHATS)
    public ResponseEntity<Long> createChat(@RequestBody @Validated ChatCreateRequest request) {
        return ResponseEntity.ok(this.chatService.createChat(request));
    }

    @GetMapping(ChatRoutes.External.CHATS)
    public ResponseEntity<Page<ChatPaginationShared>> getByPage(@ParameterObject @Validated GetChatsByPageRequest request) {
        return ResponseEntity.ok(this.chatService.getByPage(request));
    }

    @GetMapping(ChatRoutes.External.CHAT_BY_ID)
    public ResponseEntity<ChatShared> getById(@PathVariable("id") long chatId, @ParameterObject @Validated GetChatRequest request) {
        return ResponseEntity.ok(this.chatService.getById(chatId, request));
    }

    @DeleteMapping(ChatRoutes.External.LEAVE_CHAT)
    public ResponseEntity<Boolean> leaveChat(@AuthenticationPrincipal JwtSecurityUser principal,
                                           @PathVariable("id") long chatId) {
        return ResponseEntity.ok(this.chatService.leaveChat(chatId, principal));
    }

    @PostMapping(ChatRoutes.External.CHAT_MEMBERS)
    public ResponseEntity<Boolean> addChatMember(@PathVariable("id") long chatId, @RequestBody List<Long> memberIds) {
        return ResponseEntity.ok(this.chatService.addChatMember(chatId, memberIds));
    }

    @MessageMapping(ChatWsRoutes.CREATE_MESSAGE)
    public ResponseEntity<MessageShared> createMessage(@DestinationVariable("chat_id") @Min(1) long chatId,
                                                     @Payload @Validated CreateMessageRequest request,
                                                     @AuthenticationPrincipal JwtUserWrapper principal) {
        return ResponseEntity.ok(this.chatService.createMessage(chatId, request, principal));
    }

    @GetMapping(ChatRoutes.External.CHAT_MESSAGES)
    public ResponseEntity<Page<MessageShared>> getMessagesByPage(@ParameterObject @Validated GetMessagesByPageRequest request) {
        return ResponseEntity.ok(this.chatService.getMessagesByPage(request));
    }

    @MessageMapping(ChatWsRoutes.READ_MESSAGES)
    public ResponseEntity<Boolean> readMessages(@DestinationVariable("chat_id") @Min(1) long chatId,
                                              @AuthenticationPrincipal JwtUserWrapper principal,
                                              @Payload List<Long> messageIds) {
        return ResponseEntity.ok(this.chatService.readMessage(principal, chatId, messageIds));
    }

    @GetMapping(ChatRoutes.External.CHAT_SUMMARY)
    public ResponseEntity<ChatSummaryResponse> chatSummaryByUser(@ParameterObject @Validated GetChatSummaryRequest request) {
        return ResponseEntity.ok(this.chatService.getChatSummaryByUser(request));
    }
}
