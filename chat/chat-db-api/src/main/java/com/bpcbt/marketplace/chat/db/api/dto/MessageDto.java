package com.bpcbt.marketplace.chat.db.api.dto;

import com.bpcbt.marketplace.commons.model.FileAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MessageDto {

    private long id;
    private ChatMemberDto creator;
    private String messageText;
    private List<FileAttachment> attachments;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
