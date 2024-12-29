package com.bpcbt.marketplace.boot.chat.mapper;

import com.bpcbt.marketplace.chat.api.ChatType;
import com.bpcbt.marketplace.chat.api.response.ChatInfoByUserShared;
import com.bpcbt.marketplace.chat.api.response.ChatMemberShared;
import com.bpcbt.marketplace.chat.api.response.ChatPaginationShared;
import com.bpcbt.marketplace.chat.api.response.ChatShared;
import com.bpcbt.marketplace.chat.api.response.MessageShared;
import com.bpcbt.marketplace.chat.db.api.dto.ChatDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatInfoByUserDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatMemberDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatPaginationDto;
import com.bpcbt.marketplace.chat.db.api.dto.MessageDto;
import com.bpcbt.marketplace.commons.model.FileAttachment;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-03-04T11:28:30+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21 (BellSoft)"
)
@Component
public class MessageMapperImpl implements MessageMapper {

    @Override
    public MessageShared mapMessageToShared(MessageDto message) {
        if ( message == null ) {
            return null;
        }

        MessageShared.MessageSharedBuilder messageShared = MessageShared.builder();

        messageShared.id( message.getId() );
        messageShared.creator( chatMemberDtoToChatMemberShared( message.getCreator() ) );
        messageShared.messageText( message.getMessageText() );
        List<FileAttachment> list = message.getAttachments();
        if ( list != null ) {
            messageShared.attachments( new ArrayList<FileAttachment>( list ) );
        }
        messageShared.createdAt( message.getCreatedAt() );
        messageShared.updatedAt( message.getUpdatedAt() );

        return messageShared.build();
    }

    @Override
    public ChatShared mapChatToShared(ChatDto chat) {
        if ( chat == null ) {
            return null;
        }

        long id = 0L;
        String title = null;
        ChatType type = null;
        List<MessageShared> messages = null;
        List<MessageShared> unreadMessages = null;

        id = chat.getId();
        title = chat.getTitle();
        type = chat.getType();
        messages = messageDtoListToMessageSharedList( chat.getMessages() );
        unreadMessages = messageDtoListToMessageSharedList( chat.getUnreadMessages() );

        ChatShared chatShared = new ChatShared( id, title, type, messages, unreadMessages );

        return chatShared;
    }

    @Override
    public ChatPaginationShared mapChatToPaginationShared(ChatPaginationDto chat) {
        if ( chat == null ) {
            return null;
        }

        ChatPaginationShared chatPaginationShared = new ChatPaginationShared();

        chatPaginationShared.setId( chat.getId() );
        chatPaginationShared.setTitle( chat.getTitle() );
        chatPaginationShared.setType( chat.getType() );
        chatPaginationShared.setLastMessage( mapMessageToShared( chat.getLastMessage() ) );
        chatPaginationShared.setChatMembers( chatMemberDtoListToChatMemberSharedList( chat.getChatMembers() ) );

        return chatPaginationShared;
    }

    @Override
    public ChatInfoByUserShared mapToMetaDataShared(ChatInfoByUserDto dto) {
        if ( dto == null ) {
            return null;
        }

        List<ChatMemberShared> chatMembers = null;
        int unreadMessages = 0;
        String chatTitle = null;
        ChatType chatType = null;
        MessageShared lastMessage = null;

        chatMembers = chatMemberDtoListToChatMemberSharedList( dto.getChatMembers() );
        unreadMessages = dto.getUnreadMessages();
        chatTitle = dto.getChatTitle();
        chatType = dto.getChatType();
        lastMessage = mapMessageToShared( dto.getLastMessage() );

        ChatInfoByUserShared chatInfoByUserShared = new ChatInfoByUserShared( unreadMessages, chatTitle, chatType, lastMessage, chatMembers );

        return chatInfoByUserShared;
    }

    protected ChatMemberShared chatMemberDtoToChatMemberShared(ChatMemberDto chatMemberDto) {
        if ( chatMemberDto == null ) {
            return null;
        }

        ChatMemberShared.ChatMemberSharedBuilder chatMemberShared = ChatMemberShared.builder();

        chatMemberShared.id( chatMemberDto.getId() );
        chatMemberShared.profileId( chatMemberDto.getProfileId() );
        chatMemberShared.fullName( chatMemberDto.getFullName() );
        chatMemberShared.profileType( chatMemberDto.getProfileType() );
        chatMemberShared.kicked( chatMemberDto.isKicked() );
        chatMemberShared.chatId( chatMemberDto.getChatId() );

        return chatMemberShared.build();
    }

    protected List<MessageShared> messageDtoListToMessageSharedList(List<MessageDto> list) {
        if ( list == null ) {
            return null;
        }

        List<MessageShared> list1 = new ArrayList<MessageShared>( list.size() );
        for ( MessageDto messageDto : list ) {
            list1.add( mapMessageToShared( messageDto ) );
        }

        return list1;
    }

    protected List<ChatMemberShared> chatMemberDtoListToChatMemberSharedList(List<ChatMemberDto> list) {
        if ( list == null ) {
            return null;
        }

        List<ChatMemberShared> list1 = new ArrayList<ChatMemberShared>( list.size() );
        for ( ChatMemberDto chatMemberDto : list ) {
            list1.add( chatMemberDtoToChatMemberShared( chatMemberDto ) );
        }

        return list1;
    }
}
