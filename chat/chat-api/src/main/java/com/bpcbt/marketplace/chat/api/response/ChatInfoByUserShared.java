package com.bpcbt.marketplace.chat.api.response;

import com.bpcbt.marketplace.chat.api.ChatType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ChatInfoByUserShared {

    private int unreadMessages;
    private String chatTitle;
    private ChatType chatType;
    private MessageShared lastMessage;
    private List<ChatMemberShared> chatMembers;
}
