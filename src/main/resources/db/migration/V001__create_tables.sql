-- =============================================================================
-- Migration : V001__create_tables.sql
-- Description: Initial schema – users and voice_sessions tables
-- Context    : Discord bot (JDA) – user IDs are Discord Snowflake IDs (64-bit)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- EXTENSION
-- Used for UUID generation on voice_sessions.session_id.
-- Discord Snowflake IDs (user_id) are externally provided – no UUID needed there.
-- -----------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- -----------------------------------------------------------------------------
-- TABLE: users
--
-- Mirrors Discord users tracked by the bot.
-- • user_id       – Discord Snowflake ID (64-bit integer, provided by Discord/JDA).
--                   Used directly as PK: it is already globally unique and immutable.
--                   Mapped to Long in Java / BIGINT in PostgreSQL.
-- • username      – Discord username at the time of last interaction (denormalised
--                   for convenience; Discord usernames can change).
-- • user_picture  – CDN URL of the user's avatar; nullable (user may have none).
-- • total_time    – Cumulative voice session duration in seconds (≥ 0).
-- • created_at    – Audit: row creation timestamp.
-- • updated_at    – Audit: row last-update timestamp.
-- -----------------------------------------------------------------------------
CREATE TABLE users (
                       user_id       BIGINT       NOT NULL,   -- Discord Snowflake ID (externally assigned)
                       username      VARCHAR(100) NOT NULL,
                       user_picture  TEXT,
                       total_time    BIGINT       NOT NULL DEFAULT 0
                           CONSTRAINT chk_users_total_time_non_negative CHECK (total_time >= 0),
                       created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

                       CONSTRAINT pk_users PRIMARY KEY (user_id)
);

COMMENT ON TABLE  users              IS 'Discord users tracked by the bot';
COMMENT ON COLUMN users.user_id      IS 'Discord Snowflake ID – 64-bit, globally unique, used as natural PK';
COMMENT ON COLUMN users.username     IS 'Discord username at last interaction (may change over time)';
COMMENT ON COLUMN users.user_picture IS 'Discord avatar CDN URL; nullable';
COMMENT ON COLUMN users.total_time   IS 'Cumulative voice channel time in seconds';
COMMENT ON COLUMN users.created_at   IS 'Row creation timestamp (UTC)';
COMMENT ON COLUMN users.updated_at   IS 'Row last-update timestamp (UTC)';

-- -----------------------------------------------------------------------------
-- TABLE: voice_sessions
--
-- Records each voice channel session for a user.
-- Cardinality (from ER diagram): users (0,n) ──Got── (1,1) voice_sessions
-- i.e. each session belongs to exactly one user (mandatory FK),
--      a user may have zero or many sessions.
--
-- • session_id  – surrogate PK (UUID v4). No natural key exists here, so UUID
--                 is the right choice: avoids sequential ID exposure and works
--                 well in distributed / async contexts.
-- • user_id     – FK → users.user_id (Discord Snowflake ID, BIGINT, NOT NULL).
-- • started_at  – When the user joined the voice channel (NOT NULL).
-- • ended_at    – When the user left; NULL while the session is still active.
-- • created_at  – Audit: row creation timestamp.
-- -----------------------------------------------------------------------------
CREATE TABLE voice_sessions (
                                session_id  UUID        NOT NULL DEFAULT gen_random_uuid(),
                                user_id     BIGINT      NOT NULL,
                                started_at  TIMESTAMPTZ NOT NULL,
                                ended_at    TIMESTAMPTZ,
                                created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                CONSTRAINT pk_voice_sessions
                                    PRIMARY KEY (session_id),

                                CONSTRAINT fk_voice_sessions_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users (user_id)
                                        ON UPDATE CASCADE   -- propagates if user_id ever changes (safe default)
                                        ON DELETE RESTRICT, -- prevent deletion of users who have session history

                                CONSTRAINT chk_voice_sessions_dates
                                    CHECK (ended_at IS NULL OR ended_at > started_at)
);

-- Fast lookup of all sessions for a given user (most common query pattern)
CREATE INDEX idx_voice_sessions_user_id
    ON voice_sessions (user_id);

-- Useful for filtering active sessions or querying by time range
CREATE INDEX idx_voice_sessions_started_at
    ON voice_sessions (started_at DESC);

COMMENT ON TABLE  voice_sessions             IS 'Voice channel sessions per Discord user';
COMMENT ON COLUMN voice_sessions.session_id  IS 'Surrogate PK (UUID v4) – no natural key exists for sessions';
COMMENT ON COLUMN voice_sessions.user_id     IS 'Discord Snowflake ID of the session owner (FK → users)';
COMMENT ON COLUMN voice_sessions.started_at  IS 'Timestamp when the user joined the voice channel (UTC)';
COMMENT ON COLUMN voice_sessions.ended_at    IS 'Timestamp when the user left; NULL if session is still active';
COMMENT ON COLUMN voice_sessions.created_at  IS 'Row creation timestamp (UTC)';

-- =============================================================================
-- END OF MIGRATION V001
-- =============================================================================