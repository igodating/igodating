package com.bpcbt.marketplace.chat.db.postgres;

import com.bpcbt.marketplace.basic.db.postgres.AbstractDbRepository;
import com.bpcbt.marketplace.basic.db.postgres.ArraySqlValue;
import com.bpcbt.marketplace.chat.api.ChatType;
import com.bpcbt.marketplace.chat.api.request.ChatCreateRequest;
import com.bpcbt.marketplace.chat.api.request.CreateMessageRequest;
import com.bpcbt.marketplace.chat.api.request.GetChatRequest;
import com.bpcbt.marketplace.chat.api.request.GetChatsByPageRequest;
import com.bpcbt.marketplace.chat.api.request.GetMessagesByPageRequest;
import com.bpcbt.marketplace.chat.db.api.ChatRepository;
import com.bpcbt.marketplace.chat.db.api.FileOperation;
import com.bpcbt.marketplace.chat.db.api.dto.ChatDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatInfoByUserDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatMemberCreateDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatMemberDto;
import com.bpcbt.marketplace.chat.db.api.dto.ChatPaginationDto;
import com.bpcbt.marketplace.chat.db.api.dto.MessageDto;
import com.bpcbt.marketplace.commons.global.ProfileType;
import com.bpcbt.marketplace.commons.model.page.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.pe.kwonnam.freemarkerdynamicqlbuilder.DynamicQuery;
import kr.pe.kwonnam.freemarkerdynamicqlbuilder.FreemarkerDynamicQlBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public class ChatPostgresRepository extends AbstractDbRepository implements ChatRepository {

    private final FreemarkerDynamicQlBuilder freemarkerDynamicQlBuilder;

    public ChatPostgresRepository(NamedParameterJdbcTemplate jdbcTemplate, ObjectMapper objectMapper, FreemarkerDynamicQlBuilder freemarkerDynamicQlBuilder) {
        super(jdbcTemplate, objectMapper);
        this.freemarkerDynamicQlBuilder = freemarkerDynamicQlBuilder;
    }

    private final RowMapper<ChatDto> MAPPER_CHAT_DTO = (rs, j) -> {
        final long chatId = rs.getLong("id");

        List<Long> messageIds = getListArray(rs, "message_ids", Long.class);
        List<String> messageTexts = getListArray(rs, "message_texts", String.class);
        List<Long> creatorIds = getListArray(rs, "creator_ids", Long.class);
        List<Long> creatorProfileIds = getListArray(rs, "creator_profile_ids", Long.class);
        List<String> creatorProfileTypes = getListArray(rs, "creator_profile_types", String.class);
        List<String> creatorNames = getListArray(rs, "creator_names", String.class);
        List<Boolean> creatorKicked = getListArray(rs, "creator_kicked", Boolean.class);
        List<PGobject> attachments = getListArray(rs, "message_attachments", PGobject.class);
        List<Boolean> readMessages = getListArray(rs, "message_read", Boolean.class);
        List<OffsetDateTime> messagesCreatedAt = getListArray(rs, "messages_created_at", OffsetDateTime.class);
        List<OffsetDateTime> messagesUpdatedAt = getListArray(rs, "messages_updated_at", OffsetDateTime.class);

        List<MessageDto> messages = new ArrayList<>();
        List<MessageDto> unreadMessages = new ArrayList<>();

        for (int i = 0; i < messageIds.size(); i++) {
            final ChatMemberDto creator = ChatMemberDto.builder()
                    .id(creatorIds.get(i))
                    .fullName(creatorNames.get(i))
                    .chatId(chatId)
                    .kicked(creatorKicked.get(i))
                    .profileType(ProfileType.valueOf(creatorProfileTypes.get(i)))
                    .profileId(creatorProfileIds.get(i))
                    .build();
            final MessageDto messageDto = MessageDto.builder()
                    .id(messageIds.get(i))
                    .messageText(messageTexts.get(i))
                    .creator(creator)
                    .attachments(this.fromJson(attachments.get(i).getValue(), new TypeReference<>() {
                    }))
                    .createdAt(messagesCreatedAt.get(i))
                    .updatedAt(messagesUpdatedAt.get(i))
                    .build();
            if (Boolean.TRUE.equals(readMessages.get(i))) {
                messages.add(messageDto);
            } else unreadMessages.add(messageDto);
        }

        return ChatDto.builder()
                .id(chatId)
                .title(rs.getString("title"))
                .type(ChatType.valueOf(rs.getString("type")))
                .messages(messages)
                .unreadMessages(unreadMessages)
                .build();
    };

    private final RowMapper<ChatPaginationDto> MAPPER_CHAT_PAGINATION_DTO = (rs, j) -> {
        final long chatId = rs.getLong("id");

        List<Long> creatorIds = getListArray(rs, "member_ids", Long.class);
        List<Long> creatorProfileIds = getListArray(rs, "member_profile_ids", Long.class);
        List<String> creatorProfileTypes = getListArray(rs, "member_profile_types", String.class);
        List<String> creatorNames = getListArray(rs, "member_names", String.class);
        List<Boolean> creatorKicked = getListArray(rs, "member_kicked", Boolean.class);

        List<ChatMemberDto> chatMembers = new ArrayList<>(creatorIds.size());

        for (int i = 0; i < creatorIds.size(); i++) {
            final ChatMemberDto chatMember = ChatMemberDto.builder()
                    .id(creatorIds.get(i))
                    .fullName(creatorNames.get(i))
                    .chatId(chatId)
                    .kicked(creatorKicked.get(i))
                    .profileType(ProfileType.valueOf(creatorProfileTypes.get(i)))
                    .profileId(creatorProfileIds.get(i))
                    .build();
            chatMembers.add(chatMember);
        }

        MessageDto lastMessage = null;
        final Long lastMessageId = rs.getObject("message_id", Long.class);
        if (lastMessageId != null) {
            final ChatMemberDto creator = ChatMemberDto.builder()
                    .id(rs.getLong("message_creator_id"))
                    .fullName(rs.getString("creator_name"))
                    .chatId(chatId)
                    .kicked(rs.getBoolean("creator_kicked"))
                    .profileType(ProfileType.valueOf(rs.getString("message_creator_profile_type")))
                    .profileId(rs.getLong("message_creator_profile_id"))
                    .build();

            lastMessage = MessageDto.builder()
                    .id(lastMessageId)
                    .messageText(rs.getString("message"))
                    .creator(creator)
                    .attachments(this.fromJson(rs.getString("attachments"), new TypeReference<>() {
                    }))
                    .createdAt(rs.getObject("message_created_at", OffsetDateTime.class))
                    .updatedAt(rs.getObject("message_updated_at", OffsetDateTime.class))
                    .build();
        }

        return ChatPaginationDto.builder()
                .id(chatId)
                .title(rs.getString("title"))
                .type(ChatType.valueOf(rs.getString("type")))
                .lastMessage(lastMessage)
                .chatMembers(chatMembers)
                .build();
    };

    private final RowMapper<MessageDto> MAPPER_MESSAGE_DTO = (rs, j) -> {
        final ChatMemberDto creator = ChatMemberDto.builder()
                .id(rs.getLong("creator_id"))
                .fullName(rs.getString("creator_name"))
                .chatId(rs.getLong("chat_id"))
                .kicked(rs.getBoolean("creator_kicked"))
                .profileType(ProfileType.valueOf(rs.getString("creator_profile_type")))
                .profileId(rs.getLong("creator_profile_id"))
                .build();

        return MessageDto.builder()
                .id(rs.getLong("id"))
                .messageText(rs.getString("message"))
                .creator(creator)
                .attachments(this.fromJson(rs.getString("attachments"), new TypeReference<>() {
                }))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                .build();
    };

    @Override
    public Set<String> getAllowedFilesExtensions(FileOperation operationName) {
        return new HashSet<>(this.jdbcTemplate.queryForList("""
                        select extension
                        from allowed_file_extensions
                        where operation_name = :operationName""",
                new MapSqlParameterSource("operationName", operationName.name()),
                String.class));
    }

    @Override
    @Transactional
    @SuppressWarnings("all")
    public Long createChat(ChatCreateRequest request) {
        final MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("title", request.getTitle())
                .addValue("type", request.getType().name());
        final Long chatId = this.jdbcTemplate.queryForObject("""
                insert into chats (title, type)
                values (:title, :type)
                returning id
                """, parameterSource, Long.class);
        return chatId;
    }

    @Override
    public boolean updateCreator(long chatId, long chatMemberId) {
        return this.jdbcTemplate.update("""
                update chats
                set creator_id = :chatMemberId
                where id = :chatId
                """, new MapSqlParameterSource("chatMemberId", chatMemberId)
                .addValue("chatId", chatId)) == 1;
    }

    @Override
    public ChatDto getById(long chatId, GetChatRequest request, long messagesLimit) {
        return this.jdbcTemplate.queryForObject("""
                with chat_member as (
                select id from chat_members cm
                where cm.profile_id = :profileId and cm.chat_id = :chatId),
                                
                unread_messages_from_chat as (
                select * from messages m
                join unread_messages um on um.message_id = m.id and um.member_id = (select id from chat_member)
                where m.chat_id = :chatId
                order by m.id desc),
                                
                read_messages_from_chat as (
                select * from messages m
                left join unread_messages um on um.message_id = m.id and um.member_id = (select id from chat_member)
                where m.chat_id = :chatId and um.message_id is null
                order by m.id desc
                limit :messagesLimit)
                                
                select
                c.id,
                c.title,
                c.type,
                c.creator_id,
                m.message_ids,
                m.message_texts,
                m.creator_ids,
                m.creator_profile_types,
                m.creator_profile_ids,
                m.creator_names,
                m.creator_kicked,
                m.message_attachments,
                m.message_read,
                m.messages_created_at,
                m.messages_updated_at
                from chats c
                    left join(select m.chat_id,
                              array_agg(m.id order by m.id) as message_ids,
                              array_agg(m.message order by m.id) as message_texts,
                              array_agg(m.chat_member_id order by m.id) as creator_ids,
                              array_agg(m.attachments order by m.id) as message_attachments,
                              array_agg(m.read order by m.id) as message_read,
                              array_agg(m.created_at order by m.id) as messages_created_at,
                              array_agg(m.updated_at order by m.id) as messages_updated_at,
                              array_agg(cm.profile_id order by cm.chat_id) as creator_profile_ids,
                              array_agg(cm.profile_type order by cm.chat_id) as creator_profile_types,
                              array_agg(cm.user_name order by cm.chat_id) as creator_names,
                              array_agg(cm.kicked order by cm.chat_id) as creator_kicked
                              from (select *, (false) as read from unread_messages_from_chat um
                                    union
                                    select *, (true) as read from read_messages_from_chat rm) m
                              left join chat_members cm on cm.id = m.chat_member_id
                              group by m.chat_id) m on m.chat_id = c.id
                where id = :chatId
                """, new MapSqlParameterSource("chatId", chatId)
                .addValue("profileId", request.getProfileId())
                .addValue("messagesLimit", messagesLimit), MAPPER_CHAT_DTO);
    }

    @Override
    public Page<ChatPaginationDto> getByPage(GetChatsByPageRequest request) {
        final MapSqlParameterSource parameterSource = new MapSqlParameterSource(this.paginationToValueParam(request.getPaginationRequest()))
                .addValue("messagesLimit", request.getMessageLimit())
                .addValue("type", Optional.ofNullable(request.getType())
                        .map(Enum::name)
                        .orElse(null))
                .addValue("profileId", request.getProfileId());
        return pageQuery(MAPPER_CHAT_PAGINATION_DTO, parameterSource, this.freemarkerDynamicQlBuilder, "getChatsByPage");
    }

    @Override
    @Transactional
    public MessageDto createMessage(long chatId, CreateMessageRequest request) {
        final MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("message", request.getMessageText())
                .addValue("chatId", chatId)
                .addValue("profileId", request.getProfileId())
                .addValue("attachments", this.toJson(request.getAttachments()), Types.OTHER);
        return this.jdbcTemplate.queryForObject("""
                with existed_chat_member as(
                    select cm.id,
                           cm.profile_type,
                           cm.user_name,
                           cm.profile_id,
                           cm.kicked
                    from chat_members cm
                    where cm.profile_id = :profileId and cm.chat_id = :chatId
                )
                insert into messages (message, chat_id, chat_member_id, attachments)
                values (:message, :chatId, (select id from existed_chat_member), :attachments)
                returning id, message, chat_id, (select id from existed_chat_member) as creator_id,
                    (select profile_type from existed_chat_member) as creator_profile_type,
                    (select user_name from existed_chat_member) as creator_name,
                    (select profile_id from existed_chat_member) as creator_profile_id,
                    (select kicked from existed_chat_member) as creator_kicked, attachments, created_at, updated_at
                """, parameterSource, MAPPER_MESSAGE_DTO);
    }

    @Override
    public Page<MessageDto> getMessagesByPage(GetMessagesByPageRequest request) {
        final MapSqlParameterSource parameterSource = new MapSqlParameterSource(this.paginationToValueParam(request.getPaginationRequest()))
                .addValue("chatId", request.getChatId());
        return pageQuery(MAPPER_MESSAGE_DTO, parameterSource, this.freemarkerDynamicQlBuilder, "getMessagesByPage");
    }

    @Override
    public Long addChatMember(long chatId, ChatMemberCreateDto dto) {
        final MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("chatId", chatId)
                .addValue("profileId", dto.getProfileId())
                .addValue("profileType", dto.getProfileType().name())
                .addValue("userName", dto.getUserName());
        return this.jdbcTemplate.queryForObject("""
                insert into chat_members (profile_id, chat_id, profile_type, user_name)
                values (:profileId, :chatId, :profileType, :userName)
                returning id""", sqlParameterSource, Long.class);
    }

    @Override
    @Transactional
    public boolean addChatMembers(long chatId, Collection<ChatMemberCreateDto> createDtoList) {
        final MapSqlParameterSource[] sqlParameterSources = createDtoList.stream()
                .map(createDto -> new MapSqlParameterSource("chatId", chatId)
                        .addValue("profileId", createDto.getProfileId())
                        .addValue("profileType", createDto.getProfileType().name())
                        .addValue("userName", createDto.getUserName()))
                .toArray(MapSqlParameterSource[]::new);
        return this.jdbcTemplate.batchUpdate("""
                insert into chat_members (profile_id, chat_id, profile_type, user_name)
                values (:profileId, :chatId, :profileType, :userName)""", sqlParameterSources).length == createDtoList.size();
    }

    @Override
    public Set<Long> getMembersByChatId(long chatId) {
        return this.jdbcTemplate.query("""
                select profile_id
                from chat_members
                where chat_id = :chatId
                """, new MapSqlParameterSource("chatId", chatId), rs -> {
            Set<Long> memberIds = new HashSet<>();
            while (rs.next()) {
                memberIds.add(rs.getLong("profile_id"));
            }
            return memberIds;
        });
    }

    @Override
    @Transactional
    public boolean leaveChat(long chatId, long profileId) {
        return this.jdbcTemplate.update("""
                delete
                from chat_members
                where profile_id = :profileId
                """, new MapSqlParameterSource("profileId", profileId)) == 1;
    }

    @Override
    @Transactional
    public boolean readMessages(long chatId, long profileId, Collection<Long> messageIds) {
        return this.jdbcTemplate.update("""
                delete from unread_messages um
                where um.member_id = (select id from chat_members where profile_id = :profileId and chat_id = :chatId) and message_id = any(:messageIds)
                """, new MapSqlParameterSource("profileId", profileId)
                .addValue("messageIds", ArraySqlValue.create(messageIds, Long.class))
                .addValue("chatId", chatId)) == messageIds.size();
    }

    public Map<ChatType, Long> getAllUnreadMessageCountGroupedByChatType(Long profileId) {
        return this.jdbcTemplate.query("""
                select distinct c.type, count(*) over(partition by c.type) as total_by_type
                from unread_messages um
                join messages m on m.id = um.message_id
                join chats c on c.id = m.chat_id
                join chat_members cm on cm.profile_id = :profileId
                where um.member_id = cm.id
                """, new MapSqlParameterSource("profileId", profileId), rs -> {
            final Map<ChatType, Long> type2Count = new EnumMap<>(ChatType.class);
            while (rs.next()) {
                type2Count.put(ChatType.valueOf(rs.getString("type")), rs.getLong("total_by_type"));
            }
            return type2Count;
        });
    }

    @Override
    public List<ChatInfoByUserDto> getChatSummaryByUser(Long profileId) {
        final MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("profileId", profileId);
        final DynamicQuery dynamicQuery = this.freemarkerDynamicQlBuilder.buildQuery("getChatSummary", sqlParameterSource.getValues());
        return this.jdbcTemplate.getJdbcOperations().query(dynamicQuery.getQueryString(), (rs, i) -> {
            final long chatId = rs.getLong("id");

            List<Long> creatorIds = getListArray(rs, "member_ids", Long.class);
            List<Long> creatorProfileIds = getListArray(rs, "member_profile_ids", Long.class);
            List<String> creatorProfileTypes = getListArray(rs, "member_profile_types", String.class);
            List<String> creatorNames = getListArray(rs, "member_names", String.class);
            List<Boolean> creatorKicked = getListArray(rs, "member_kicked", Boolean.class);

            List<ChatMemberDto> chatMembers = new ArrayList<>(creatorIds.size());

            for (int j = 0; j < creatorIds.size(); j++) {
                final ChatMemberDto chatMember = ChatMemberDto.builder()
                        .id(creatorIds.get(j))
                        .fullName(creatorNames.get(j))
                        .chatId(chatId)
                        .kicked(creatorKicked.get(j))
                        .profileType(ProfileType.valueOf(creatorProfileTypes.get(j)))
                        .profileId(creatorProfileIds.get(j))
                        .build();
                chatMembers.add(chatMember);
            }

            final ChatMemberDto messageCreator = ChatMemberDto.builder()
                    .id(rs.getLong("message_creator_id"))
                    .fullName(rs.getString("creator_name"))
                    .chatId(chatId)
                    .kicked(rs.getBoolean("creator_kicked"))
                    .profileType(ProfileType.valueOf(rs.getString("message_creator_profile_type")))
                    .profileId(rs.getLong("message_creator_profile_id"))
                    .build();

            final MessageDto lastMessage = MessageDto.builder()
                    .id(rs.getLong("message_id"))
                    .messageText(rs.getString("message"))
                    .creator(messageCreator)
                    .attachments(this.fromJson(rs.getString("attachments"), new TypeReference<>() {
                    }))
                    .createdAt(rs.getObject("message_created_at", OffsetDateTime.class))
                    .updatedAt(rs.getObject("message_updated_at", OffsetDateTime.class))
                    .build();

            return ChatInfoByUserDto.builder()
                    .chatTitle(rs.getString("title"))
                    .chatType(ChatType.valueOf(rs.getString("type")))
                    .id(chatId)
                    .lastMessage(lastMessage)
                    .chatMembers(chatMembers)
                    .unreadMessages(rs.getInt("unread_messages"))
                    .build();
        }, dynamicQuery.getQueryParameterArray());
    }

    @Override
    public Set<Pair<Long, ProfileType>> getMandatoryMembers(ChatType chatType) {
        return this.jdbcTemplate.query("""
                select profile_id, profile_type
                from mandatory_members
                where chat_type = :chatType
                """, new MapSqlParameterSource("chatType", chatType.name()), (rs) -> {
            final Set<Pair<Long, ProfileType>> profileId2ProfileType = new HashSet<>();
            while (rs.next()) {
                profileId2ProfileType.add(Pair.of(rs.getLong("profile_id"), ProfileType.valueOf(rs.getString("profile_type"))));
            }
            return profileId2ProfileType;
        });
    }
}
