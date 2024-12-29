package com.bpcbt.marketplace.chat.db.api;

import com.bpcbt.marketplace.chat.api.ChatType;
import com.bpcbt.marketplace.chat.api.request.ChatCreateRequest;
import com.bpcbt.marketplace.chat.api.request.CreateMessageRequest;
import com.bpcbt.marketplace.chat.api.request.GetChatRequest;
import com.bpcbt.marketplace.chat.api.request.GetChatsByPageRequest;
import com.bpcbt.marketplace.chat.api.request.GetMessagesByPageRequest;
import com.bpcbt.marketplace.chat.db.api.dto.ChatDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatInfoByUserDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatMemberCreateDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatPaginationDto;
import com.bpcbt.marketplace.chat.db.api.dto.MessageDto;
import com.bpcbt.marketplace.commons.global.ProfileType;
import com.bpcbt.marketplace.commons.model.page.Page;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ChatRepository {
    Set<String> getAllowedFilesExtensions(FileOperation operationName);

    Long createChat(ChatCreateRequest request);

    boolean updateCreator(long chatId, long chatMemberId);

    ChatDto getById(long chatId, GetChatRequest request, long messageLimit);

    Page<ChatPaginationDto> getByPage(GetChatsByPageRequest request);

    MessageDto createMessage(long chatId, CreateMessageRequest request);

    Page<MessageDto> getMessagesByPage(GetMessagesByPageRequest request);

    Long addChatMember(long chatId, ChatMemberCreateDto dto);

    boolean addChatMembers(long chatId, Collection<ChatMemberCreateDto> createDtoList);

    Set<Long> getMembersByChatId(long chatId);

    boolean leaveChat(long chatId, long profileId);

    List<ChatInfoByUserDto> getChatSummaryByUser(Long profileId);

    Map<ChatType, Long> getAllUnreadMessageCountGroupedByChatType(Long profileId);

    boolean readMessages(long chatId, long profileId, Collection<Long> messageIds);

    Set<Pair<Long, ProfileType>> getMandatoryMembers(ChatType chatType);
}
