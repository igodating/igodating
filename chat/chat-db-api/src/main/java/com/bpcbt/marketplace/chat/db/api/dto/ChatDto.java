package com.bpcbt.marketplace.chat.db.api.dto;

import com.bpcbt.marketplace.chat.api.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatDto {

    private long id;
    private String title;
    private ChatType type;
    private List<MessageDto> messages;
    private List<MessageDto> unreadMessages;
}
