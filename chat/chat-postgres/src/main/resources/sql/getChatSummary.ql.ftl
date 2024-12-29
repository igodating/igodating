select
    c.id,
    c.title,
    c.type,
    lm.id as message_id,
    lm.message,
    lm.chat_member_id as message_creator_id,
    lm.creator_profile_type as message_creator_profile_type,
    lm.creator_profile_id as message_creator_profile_id,
    lm.creator_name as creator_name,
    lm.creator_kicked as creator_kicked,
    lm.attachments as attachments,
    lm.created_at as message_created_at,
    lm.updated_at as message_updated_at,
    cm.member_ids,
    cm.member_profile_ids,
    cm.member_profile_types,
    cm.member_names,
    cm.member_kicked,
    um.count_messages as unread_messages
from chats c
    join (
        select distinct on (chat_id) count(*) as count_messages, m.chat_id
        from unread_messages um
        join messages m on um.message_id = m.id
        join chat_members cm on cm.id = um.member_id
        where cm.profile_id = ${param(profileId)}
        group by m.chat_id
        ) um on um.chat_id = c.id
    join (select distinct on (m.chat_id)
                        m.id,
                        m.chat_id,
                        m.message,
                        m.chat_member_id,
                        cm.profile_type as creator_profile_type,
                        cm.profile_id as creator_profile_id,
                        cm.user_name as creator_name,
                        cm.kicked as creator_kicked,
                        m.attachments,
                        m.created_at,
                        m.updated_at
                   from messages m
                   left join chat_members cm on cm.id = m.chat_member_id
                   order by m.chat_id, id desc) lm on lm.chat_id = c.id
    join (select cm.chat_id,
                    array_agg(cm.id) as member_ids,
                    array_agg(cm.profile_id) as member_profile_ids,
                    array_agg(cm.profile_type) as member_profile_types,
                    array_agg(cm.user_name) as member_names,
                    array_agg(cm.kicked) as member_kicked
               from chat_members cm
               group by cm.chat_id
    ) cm on cm.chat_id = c.id