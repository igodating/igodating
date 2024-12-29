package com.bpcbt.marketplace.boot.chat.service;

public interface ChatWsRoutes {
    String CREATE_MESSAGE = "chats/{chat_id}/message/create";
    String CREATE_MESSAGE_EVENT = "/topic/chats/{chat_id}/message/create/event";
    String LEAVE_CHAT = "/topic/chats/{chat_id}/member/leave/event";
    String ADD_MEMBER_TO_CHAT = "/topic/chats/{chat_id}/member/add/event";
    String READ_MESSAGES = "chats/{chat_id}/message/read";
}
