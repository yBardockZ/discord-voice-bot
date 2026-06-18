-- =============================================================================
-- Migration : V002__refactor_guild_stats.sql
-- Description: Refactor voice time tracking to be scoped by Discord guild
-- Context    : Discord bot (JDA) - user and guild IDs are Discord Snowflake IDs
-- =============================================================================

-- -----------------------------------------------------------------------------
-- TABLE: users
--
-- Users now store only global Discord profile data.
-- Accumulated voice time is moved to guild_stats to avoid mixing data from
-- different Discord servers.
-- -----------------------------------------------------------------------------
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS chk_users_total_time_non_negative;

ALTER TABLE users
    DROP COLUMN IF EXISTS total_time;

-- -----------------------------------------------------------------------------
-- TABLE: guild_stats
--
-- Stores accumulated voice time per user per Discord guild.
-- This is the source of truth for guild-scoped commands such as /perfil and
-- /ranking.
--
-- user_id    - FK -> users.user_id.
-- guild_id   - Discord Guild Snowflake ID (64-bit integer, provided by Discord).
-- total_time - Cumulative voice session duration in seconds for this guild.
-- -----------------------------------------------------------------------------
CREATE TABLE guild_stats (
                             id          BIGSERIAL   NOT NULL,
                             user_id     BIGINT      NOT NULL,
                             guild_id    BIGINT      NOT NULL,
                             total_time  BIGINT      NOT NULL DEFAULT 0
                                 CONSTRAINT chk_guild_stats_total_time_non_negative CHECK (total_time >= 0),
                             created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                             CONSTRAINT pk_guild_stats
                                 PRIMARY KEY (id),

                             CONSTRAINT fk_guild_stats_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users (user_id)
                                     ON UPDATE CASCADE
                                     ON DELETE RESTRICT,

                             CONSTRAINT uq_guild_stats_user_guild
                                 UNIQUE (user_id, guild_id)
);

CREATE INDEX idx_guild_stats_guild_id_total_time
    ON guild_stats (guild_id, total_time DESC);

COMMENT ON TABLE  guild_stats             IS 'Accumulated voice time per Discord user per guild';
COMMENT ON COLUMN guild_stats.id          IS 'Surrogate PK for user/guild statistics';
COMMENT ON COLUMN guild_stats.user_id     IS 'Discord Snowflake ID of the tracked user (FK -> users)';
COMMENT ON COLUMN guild_stats.guild_id    IS 'Discord Guild Snowflake ID where the time was accumulated';
COMMENT ON COLUMN guild_stats.total_time  IS 'Cumulative voice channel time in seconds for this user in this guild';
COMMENT ON COLUMN guild_stats.created_at  IS 'Row creation timestamp (UTC)';
COMMENT ON COLUMN guild_stats.updated_at  IS 'Row last-update timestamp (UTC)';

-- -----------------------------------------------------------------------------
-- TABLE: voice_sessions
--
-- Sessions now store the Discord guild where the voice activity happened.
-- -----------------------------------------------------------------------------
ALTER TABLE voice_sessions
    ADD COLUMN guild_id BIGINT;

UPDATE voice_sessions
SET guild_id = 0
WHERE guild_id IS NULL;

ALTER TABLE voice_sessions
    ALTER COLUMN guild_id SET NOT NULL;

CREATE INDEX idx_voice_sessions_user_id_guild_id
    ON voice_sessions (user_id, guild_id);

CREATE INDEX idx_voice_sessions_guild_id_started_at
    ON voice_sessions (guild_id, started_at DESC);

COMMENT ON COLUMN voice_sessions.guild_id IS 'Discord Guild Snowflake ID where the voice session occurred';

-- =============================================================================
-- END OF MIGRATION V002
-- =============================================================================
