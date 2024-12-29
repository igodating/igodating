package com.bpcbt.marketplace.chat.db.api.dto;

import com.bpcbt.marketplace.chat.api.ChatType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ChatInfoByUserDto {

    private long id;
    private int unreadMessages;
    private String chatTitle;
    private ChatType chatType;
    private MessageDto lastMessage;
    private List<ChatMemberDto> chatMembers;
}
