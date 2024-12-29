select
<#if onlyCount>
    count(*) as total_count
<#else>
    c.id,
    c.title,
    c.type,
    c.creator_id,
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
    cm.member_kicked
</#if>
from chats c
<#if profileId??>
    join chat_members exist_cm on exist_cm.chat_id = c.id and exist_cm.profile_id = ${param(profileId)}
</#if>
<#if !onlyCount>
    left join (select distinct on (m.chat_id)
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
</#if>
<@ql.where>
    <#if type??>
        and c.type = ${param(type)}
    </#if>
</@ql.where>

<#if !onlyCount>
    order by lm.created_at, id desc
    LIMIT ${param(limit)} OFFSET ${param(offset)}
</#if>
