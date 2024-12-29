package com.bpcbt.marketplace.chat.api;

public interface ChatRoutes {

    String LB_URL = "http://chat-service";
    String WS_REGISTRY = "/ws";
    String LB_WS_URL = "ws://chat-service" + WS_REGISTRY;
    String ROOT = "/api";
    String ID = "/{id:\\d+}";

    interface External {
        String ROOT = ChatRoutes.ROOT + "/external";
        String CHATS = ROOT + "/chats";
        String CHAT_BY_ID = CHATS + ID;
        String LEAVE_CHAT = CHAT_BY_ID + "/leave";
        String CHAT_MESSAGES = CHAT_BY_ID + "/messages";
        String CHAT_MEMBERS = CHAT_BY_ID + "/members";
        String CHAT_SUMMARY = CHATS + "/summary";
        String CHAT_ATTACHMENTS = CHATS + "/attachments";
    }
}
