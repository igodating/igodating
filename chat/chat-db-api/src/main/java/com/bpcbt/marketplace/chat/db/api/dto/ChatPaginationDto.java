package com.bpcbt.marketplace.chat.db.api.dto;

import com.bpcbt.marketplace.chat.api.ChatType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ChatPaginationDto {

    private long id;
    private String title;
    private ChatType type;
    private MessageDto lastMessage;
    private List<ChatMemberDto> chatMembers;
}
