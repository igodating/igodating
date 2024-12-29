select
<#if onlyCount>
    count(*) as total_count
<#else>
    m.id,
    m.message,
    m.chat_id,
    m.chat_member_id as creator_id,
    cm.profile_type as creator_profile_type,
    cm.profile_id as creator_profile_id,
    cm.kicked as creator_kicked,
    cm.user_name as creator_name,
    m.attachments,
    m.created_at,
    m.updated_at
</#if>
from messages m
join chat_members cm on cm.id = m.chat_member_id
<@ql.where>
    <#if chatId??>
        and m.chat_id = ${param(chatId)}
    </#if>
</@ql.where>

<#if !onlyCount>
    order by m.id desc
    LIMIT ${param(limit)} OFFSET ${param(offset)}
</#if>