package com.bpcbt.marketplace.chat.api.response;

import com.bpcbt.marketplace.chat.api.ChatType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ChatShared {

    private long id;
    private String title;
    private ChatType type;
    private List<MessageShared> messages;
    private List<MessageShared> unreadMessages;
}
