create table mandatory_members(
    id bigserial primary key,
    chat_type varchar not null,
    profile_id bigint not null,
    profile_type varchar not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create trigger set_updated_at_to_now_trigger
    before update
    on mandatory_members
    for each row
EXECUTE procedure set_updated_at_to_now();