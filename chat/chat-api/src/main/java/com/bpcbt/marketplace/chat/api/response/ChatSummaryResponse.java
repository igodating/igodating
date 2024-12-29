package com.bpcbt.marketplace.chat.api.response;

import com.bpcbt.marketplace.chat.api.ChatType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatSummaryResponse {

    private Map<Long, ChatInfoByUserShared> chatId2ChatInfo;
    private Map<ChatType, Long> chatType2totalUnreadMessages;
}
