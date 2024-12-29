package com.bpcbt.marketplace.chat.db.api.dto;

import com.bpcbt.marketplace.commons.global.ProfileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatMemberDto {

    private long id;
    private long profileId;
    private String fullName;
    private ProfileType profileType;
    private boolean kicked;
    private long chatId;
}
