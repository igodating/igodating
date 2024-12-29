package com.bpcbt.marketplace.chat.api.connector;

import com.bpcbt.marketplace.boot.commons.RequestWrapper;
import com.bpcbt.marketplace.boot.commons.util.MultipartInputStreamFileResource;
import com.bpcbt.marketplace.boot.commons.web.connector.BaseConnector;
import com.bpcbt.marketplace.chat.api.ChatRoutes;
import com.bpcbt.marketplace.chat.api.request.ChatCreateRequest;
import com.bpcbt.marketplace.chat.api.request.GetChatRequest;
import com.bpcbt.marketplace.chat.api.request.GetChatSummaryRequest;
import com.bpcbt.marketplace.chat.api.request.GetChatsByPageRequest;
import com.bpcbt.marketplace.chat.api.request.GetMessagesByPageRequest;
import com.bpcbt.marketplace.chat.api.response.ChatPaginationShared;
import com.bpcbt.marketplace.chat.api.response.ChatShared;
import com.bpcbt.marketplace.chat.api.response.ChatSummaryResponse;
import com.bpcbt.marketplace.chat.api.response.MessageShared;
import com.bpcbt.marketplace.commons.ActionResult;
import com.bpcbt.marketplace.commons.model.FileAttachment;
import com.bpcbt.marketplace.commons.model.page.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ChatServiceConnector extends BaseConnector {

    public ChatServiceConnector(RestTemplate restTemplate) {
        super(restTemplate, ChatRoutes.LB_URL);
    }

    public ActionResult<Long> createChat(ChatCreateRequest request) {
        return this.postAction(ChatRoutes.External.CHATS, Long.class, RequestWrapper.builder()
                .requestBody(request)
                .build());
    }

    public ActionResult<ChatShared> getChatById(long chatId, GetChatRequest request) {
        return this.getAction(ChatRoutes.External.CHAT_BY_ID, new TypeReference<>() {
        }, RequestWrapper.builder().requestParamsObject(request).build(), chatId);
    }

    public ActionResult<Page<ChatPaginationShared>> getChatByPage(GetChatsByPageRequest request) {
        return this.getAction(ChatRoutes.External.CHATS, new TypeReference<>() {
                },
                RequestWrapper.builder().requestParamsObject(request).build());
    }

    public ActionResult<Page<MessageShared>> getMessageByPage(GetMessagesByPageRequest request) {
        return this.getAction(ChatRoutes.External.CHAT_MESSAGES, new TypeReference<>() {
                },
                RequestWrapper.builder().requestParamsObject(request).build(), request.getChatId());
    }

    public ActionResult<Boolean> leaveChat(long chatId) {
        return this.deleteAction(ChatRoutes.External.LEAVE_CHAT, Boolean.class,
                RequestWrapper.builder().build(), chatId);
    }

    public ActionResult<Boolean> addChatMembers(long chatId, List<Long> memberIds) {
        return this.postAction(ChatRoutes.External.CHAT_MEMBERS, Boolean.class,
                RequestWrapper.builder().requestBody(memberIds).build(), chatId);
    }

    public ActionResult<ChatSummaryResponse> getChatSummaryByUser(GetChatSummaryRequest request) {
        return this.getAction(ChatRoutes.External.CHAT_SUMMARY, new TypeReference<>() {
                },
                RequestWrapper.builder().requestParamsObject(request).build());
    }

    @SneakyThrows
    public ActionResult<FileAttachment> uploadAttachment(MultipartFile file) {
        final LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return this.postAction(ChatRoutes.External.CHAT_ATTACHMENTS, FileAttachment.class, RequestWrapper.builder()
                .requestBody(map)
                .httpHeaders(headers)
                .build());
    }


}
