# Project Context: Discord Voice Tracker Bot

## 1. Objective
A Discord bot built in Java/Spring Boot to track and persist the total time each Discord user spends in voice channels, scoped per Discord guild/server.

## 2. Technical Stack
- **Language:** Java 21 (Maven)
- **Framework:** Spring Boot 4.0.5
- **Discord Library:** JDA 6.4.1
- **Database:** PostgreSQL 15
- **Persistence:** Spring Data JPA / Hibernate
- **Migrations:** Flyway
- **Infrastructure:** Docker & Docker Compose (multi-stage build)
- **Utilities:** Lombok
- **Testing:** JUnit 5, Mockito, AssertJ, Spring Boot Test

## 3. Architecture & Data Model
The project uses a guild-scoped data model: Discord user identity is stored globally in `users`, while accumulated voice time is stored per guild in `guild_stats`.

### Database Schema

**users table:**
- `user_id` (BIGINT, PK) - Discord Snowflake ID
- `username` (VARCHAR(100), NOT NULL)
- `user_picture` (TEXT, nullable) - Avatar CDN URL
- `created_at` (TIMESTAMPTZ)
- `updated_at` (TIMESTAMPTZ)

**guild_stats table:**
- `id` (BIGSERIAL, PK)
- `user_id` (BIGINT, FK -> users.user_id)
- `guild_id` (BIGINT, NOT NULL) - Discord Guild Snowflake ID
- `total_time` (BIGINT, NOT NULL, DEFAULT 0) - Cumulative voice time in seconds for this user in this guild
- `created_at` (TIMESTAMPTZ)
- `updated_at` (TIMESTAMPTZ)
- Unique constraint on (`user_id`, `guild_id`)
- Index on (`guild_id`, `total_time DESC`) to support `/ranking`

**voice_sessions table:**
- `session_id` (UUID, PK) - Auto-generated
- `user_id` (BIGINT, FK -> users.user_id)
- `guild_id` (BIGINT, NOT NULL) - Discord Guild Snowflake ID where the session occurred
- `started_at` (TIMESTAMPTZ, NOT NULL)
- `ended_at` (TIMESTAMPTZ, nullable) - NULL while session is active
- `created_at` (TIMESTAMPTZ)

### Flyway Migrations
- `V001__create_tables.sql` creates the original `users` and `voice_sessions` schema.
- `V002__refactor_guild_stats.sql` removes `users.total_time`, creates `guild_stats`, adds `voice_sessions.guild_id`, and creates indexes for guild-scoped session/ranking queries.

### JPA Entities
- `User.java` - Entity mapped to `users`, containing only global Discord profile data and timestamp lifecycle hooks.
- `GuildStats.java` - Entity mapped to `guild_stats`, with a `ManyToOne` relationship to `User` and per-guild accumulated voice time.
- `VoiceSession.java` - Entity mapped to `voice_sessions`, with a `ManyToOne` relationship to `User` and a required `guildId`.

### Repositories
- `UserRepository` - extends `JpaRepository<User, Long>`.
- `GuildStatsRepository` - extends `JpaRepository<GuildStats, Long>` and exposes:
  - `findByUserIdAndGuildId(Long userId, Long guildId)` for `/perfil`
  - `findTop10ByGuildIdOrderByTotalTimeDesc(Long guildId)` for `/ranking`
- `VoiceSessionRepository` - extends `JpaRepository<VoiceSession, UUID>` and exposes active-session lookup by `userId` and `guildId`.

## 4. Runtime Flow

### JDA Configuration
- `JDAConfig.java` creates the JDA bean using `BOT_TOKEN`.
- Enables `GUILD_VOICE_STATES` intent and `VOICE_STATE` cache.
- Registers three listeners:
  - `ReadyEventListener`
  - `VoiceEventListener`
  - `CommandListener`

### Command Registration
- `ReadyEventListener.java` logs bot startup and registers slash commands.
- Registers `/perfil` and `/ranking` in the configured test guild when `discord.guild.test-id` is present.
- Registers `/perfil` and `/ranking` globally when no test guild is configured.

### Voice Tracking
- `VoiceEventListener.java` listens to `GuildVoiceUpdateEvent`.
- On voice join, it extracts `userId`, `guildId`, username, and avatar URL, then calls `VoiceSessionService.handleJoin(userId, guildId, username, avatarUrl)`.
- On voice leave, it calls `VoiceSessionService.handleLeave(userId, guildId)`.
- Channel moves are currently ignored because the listener only handles pure join and pure leave events.

### Service Layer
- `VoiceSessionService.java`
  - Coordinates voice-session lifecycle for join/leave events.
  - Depends directly only on `VoiceSessionRepository` for `voice_sessions` persistence.
  - Delegates user creation/profile updates to `UserService`.
  - Delegates guild-scoped accumulated time updates to `GuildStatsService`.
  - Stores the join instant in an in-memory `ConcurrentHashMap`.
  - Creates a `voice_sessions` row with `guild_id` and `ended_at = null`.
  - On leave, calculates duration from the in-memory cache.
  - Closes the first active `voice_sessions` row matching both `userId` and `guildId`.
  - On startup, `syncSessionsWithDiscord(JDA)` checks persisted open sessions against Discord:
    - Restores still-active sessions into the in-memory cache.
    - Deletes stale open sessions for users no longer in voice.
  - Current limitation: sessions closed while the bot was offline are discarded rather than credited, to avoid recording unrealistic durations.
- `UserService.java`
  - Owns `UserRepository` access.
  - Provides `getOrCreateAndUpdateUser(userId, username, avatarUrl)`.
  - Creates new users when needed.
  - Updates stored username/avatar URL when Discord profile data changes.
- `GuildStatsService.java`
  - Owns `GuildStatsRepository` access for guild-stat mutation/ranking helpers.
  - Provides `addTimeToUser(userId, guildId, durationSeconds)`.
  - Finds or creates the matching `guild_stats` row for (`user_id`, `guild_id`) and increments `total_time`.
  - Provides `getTop10Ranking(guildId)` for ranking queries.
- `ProfileService.java`
  - Builds `/perfil` responses from `guild_stats.total_time` for the current guild.
  - Builds `/ranking` responses from the Top 10 `guild_stats` rows ordered by `total_time DESC` for the current guild.
  - Returns fallback embeds when there is no recorded voice time.

### Slash Commands
- `CommandListener.java` handles slash command interactions.
- Commands only work inside a Discord guild.
- Currently implemented commands:
  - `/perfil` - returns the caller's accumulated voice time for the current guild.
  - `/ranking` - returns an embed with the Top 10 members by accumulated voice time in the current guild.

## 5. Environment Configuration
- `application.properties`
  - Uses `SPRING_PROFILES_ACTIVE`, defaulting to `dev`.
  - Uses environment variables with defaults for PostgreSQL connection.
  - Enables Flyway.
  - Keeps Hibernate DDL generation disabled with `spring.jpa.hibernate.ddl-auto=none`.
- `application-dev.properties`
  - Reads `discord.guild.test-id` from `GUILD_TEST_ID`.
  - Enables SQL logging and formatting.
- `application-prod.properties`
  - Leaves `discord.guild.test-id` empty by default, causing global command registration.
  - Disables SQL logging.
- `.env` / `.env.example`
  - Expected to provide DB credentials, `BOT_TOKEN`, and optionally `GUILD_TEST_ID`.

## 6. Docker
- `docker-compose.yml` defines:
  - `db`: PostgreSQL 15 with a healthcheck and persistent `pgdata` volume.
  - `app`: Spring Boot container that waits for the database healthcheck and runs with `SPRING_PROFILES_ACTIVE=prod`.
- `Dockerfile` is present for building the application image.

## 7. Current State
- Maven project configured with JDA, Spring Data JPA, Flyway, PostgreSQL driver, and Lombok.
- Database migrations `V001__create_tables.sql` and `V002__refactor_guild_stats.sql` exist.
- Entities and repositories are implemented for `User`, `GuildStats`, and `VoiceSession`.
- `UserService` encapsulates user creation/profile refresh logic and owns direct `UserRepository` usage.
- `GuildStatsService` encapsulates guild-scoped time accumulation logic and owns direct `GuildStatsRepository` mutation helpers.
- JDA startup/configuration is implemented in `JDAConfig`.
- Slash command registration is implemented in `ReadyEventListener`.
- Voice join/leave tracking is implemented through `VoiceEventListener` and `VoiceSessionService`, with user/stat responsibilities delegated to `UserService` and `GuildStatsService`.
- Guild-scoped accumulated time is persisted in `guild_stats`.
- Slash commands `/perfil` and `/ranking` are implemented through `CommandListener` and `ProfileService`.
- Unit tests are implemented for `UserService`, `GuildStatsService`, and `VoiceSessionService`.
- A shared test-data factory exists in `UserTestData` for common `User` fixtures.
- A Spring Boot context smoke test exists in `DiscordVoiceBotApplicationTests`.
- Docker Compose and Dockerfile are present for local/container execution.

## 8. Test Coverage
- `UserServiceTest`
  - Verifies new user creation when the Discord user is not persisted yet.
  - Verifies existing users are returned without saving when username/avatar data is unchanged.
  - Verifies username/avatar changes are persisted when Discord profile data changes.
- `GuildStatsServiceTest`
  - Verifies creation of a new `guild_stats` record with initial duration when no guild history exists.
  - Verifies new voice duration is added to an existing guild-scoped total.
- `VoiceSessionServiceTest`
  - Verifies `handleJoin` creates a new open `voice_sessions` row.
  - Verifies `handleJoin` deletes a stale/orphan open session before creating a new one.
  - Verifies `handleLeave` calculates session duration, delegates accumulation to `GuildStatsService`, closes the open session, and clears the in-memory cache.
  - Verifies `handleLeave` exits without repository/stat updates when the user is not present in the in-memory cache.
- `DiscordVoiceBotApplicationTests`
  - Verifies the Spring application context loads.

## 9. Known Limitations / Next Steps
- Consider routing `ProfileService` ranking reads through `GuildStatsService` as well, so `GuildStatsRepository` access is fully centralized.
- Add or restore tests for `ProfileService` embed generation.
- Add tests for command handling in `CommandListener`.
- Add tests for `VoiceSessionService.syncSessionsWithDiscord(JDA)`.
- Consider refactor 'formatTime' from `ProfileService` to a new class `TimeUtils`
- Consider implementing a new feature in the `syncSessionsWithDiscord` that calls `handleJoin` method for each user who is connected to the calls at the moment the bot starts

## Invite URL
https://discord.com/oauth2/authorize?client_id=1495474048224723234&permissions=2148535296&integration_type=0&scope=bot+applications.commands
