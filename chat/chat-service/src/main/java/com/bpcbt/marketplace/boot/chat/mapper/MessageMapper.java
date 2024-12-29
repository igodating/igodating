package com.bpcbt.marketplace.boot.chat.mapper;

import com.bpcbt.marketplace.chat.api.response.ChatInfoByUserShared;
import com.bpcbt.marketplace.chat.api.response.ChatPaginationShared;
import com.bpcbt.marketplace.chat.api.response.ChatShared;
import com.bpcbt.marketplace.chat.api.response.MessageShared;
import com.bpcbt.marketplace.chat.db.api.dto.ChatDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatInfoByUserDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatPaginationDto;
import com.bpcbt.marketplace.chat.db.api.dto.MessageDto;
import org.mapstruct.Mapper;

@Mapper
public interface MessageMapper {

    MessageShared mapMessageToShared(MessageDto message);

    ChatShared mapChatToShared(ChatDto chat);

    ChatPaginationShared mapChatToPaginationShared(ChatPaginationDto chat);

    ChatInfoByUserShared mapToMetaDataShared(ChatInfoByUserDto dto);
}
