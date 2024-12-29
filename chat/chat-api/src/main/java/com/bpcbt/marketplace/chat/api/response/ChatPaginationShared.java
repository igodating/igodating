package com.bpcbt.marketplace.chat.api.response;

import com.bpcbt.marketplace.chat.api.ChatType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatPaginationShared {

    private long id;
    private String title;
    private ChatType type;
    private MessageShared lastMessage;
    private List<ChatMemberShared> chatMembers;
}
