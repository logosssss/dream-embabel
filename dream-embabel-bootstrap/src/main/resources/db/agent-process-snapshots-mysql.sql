-- Embabel 1.5.3 官方 schema.sql 使用 PostgreSQL bytea。这里改成 MySQL 8 可执行的类型。
-- 在目标库执行一次。表已存在时这条语句不会重建。

create table if not exists agent_process_snapshots (
    process_id   varchar(255)  not null,
    parent_id    varchar(255)  null,
    agent_name   varchar(255)  not null,
    status       varchar(64)   not null,
    version      bigint        not null,
    content_type varchar(255)  not null,
    payload      longblob      not null,
    created_at   datetime(6)   not null,
    updated_at   datetime(6)   not null,
    constraint pk_agent_process_snapshots primary key (process_id),
    key idx_agent_process_snapshots_parent_id (parent_id)
);
