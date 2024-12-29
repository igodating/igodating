package com.bpcbt.marketplace.chat.api.request;

import com.bpcbt.marketplace.commons.model.FileAttachment;
import com.bpcbt.marketplace.commons.validation.NotAllNullFields;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NotAllNullFields(fields = {"attachments", "messageText"})
public class CreateMessageRequest {

    private Long profileId;
    private String guid;
    private List<FileAttachment> attachments;
    private String messageText;
}
