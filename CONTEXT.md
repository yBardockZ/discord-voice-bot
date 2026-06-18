# Project Context: Discord Voice Tracker Bot

## 1. Objective
A Discord bot built in Java/Spring Boot to track and persist the total time each Discord user spends in voice channels.

## 2. Technical Stack
- **Language:** Java 21 (Maven)
- **Framework:** Spring Boot 4.0.5
- **Discord Library:** JDA 6.4.1
- **Database:** PostgreSQL 15
- **Persistence:** Spring Data JPA / Hibernate
- **Migrations:** Flyway
- **Infrastructure:** Docker & Docker Compose (multi-stage build)
- **Utilities:** Lombok

## 3. Architecture & Data Model
The project uses a 1:N relationship between `users` and `voice_sessions`.

### Database Schema (see `V001__create_tables.sql`)

**users table:**
- `user_id` (BIGINT, PK) - Discord Snowflake ID
- `username` (VARCHAR(100), NOT NULL)
- `user_picture` (TEXT, nullable) - Avatar CDN URL
- `total_time` (BIGINT, NOT NULL, DEFAULT 0) - Cumulative voice time in seconds
- `created_at` (TIMESTAMPTZ)
- `updated_at` (TIMESTAMPTZ)

**voice_sessions table:**
- `session_id` (UUID, PK) - Auto-generated
- `user_id` (BIGINT, FK -> users.user_id)
- `started_at` (TIMESTAMPTZ, NOT NULL)
- `ended_at` (TIMESTAMPTZ, nullable) - NULL while session is active
- `created_at` (TIMESTAMPTZ)

### JPA Entities
- `User.java` - Entity mapped to `users`, with timestamp lifecycle hooks and a `OneToMany` relationship to `VoiceSession`.
- `VoiceSession.java` - Entity mapped to `voice_sessions`, with a `ManyToOne` relationship to `User`.

### Repositories
- `UserRepository` - extends `JpaRepository<User, Long>`.
- `VoiceSessionRepository` - extends `JpaRepository<VoiceSession, UUID>` and exposes queries for user sessions and active sessions.

## 4. Runtime Flow

### JDA Configuration
- `JDAConfig.java` creates the JDA bean using `BOT_TOKEN`.
- Enables `GUILD_VOICE_STATES` intent and `VOICE_STATE` cache.
- Registers:
  - `VoiceEventListener`
  - `CommandListener`
  - an inline ready listener that logs bot startup and registers slash commands.
- Registers `/perfil` in the configured test guild when `discord.guild.test-id` is present; otherwise registers it globally.

### Voice Tracking
- `VoiceEventListener.java` listens to `GuildVoiceUpdateEvent`.
- On voice join, it calls `VoiceSessionService.handleJoin(userId, username, avatarUrl)`.
- On voice leave, it calls `VoiceSessionService.handleLeave(userId)`.
- Channel moves are currently ignored because the listener only handles pure join and pure leave events.

### Service Layer
- `VoiceSessionService.java`
  - Ensures the user exists on join.
  - Stores the join instant in an in-memory `ConcurrentHashMap`.
  - Creates a `voice_sessions` row with `ended_at = null`.
  - On leave, calculates duration from the in-memory cache, increments `users.total_time`, and closes the first active session in the database.
  - Current limitation: active session recovery after bot restart is not implemented; the in-memory cache is the source for current duration calculation.
- `ProfileService.java`
  - Builds the `/perfil` response from `users.total_time`.
  - Returns a fallback message when the user has no recorded voice time.

### Slash Commands
- `CommandListener.java` handles slash command interactions.
- Currently implemented command:
  - `/perfil` - replies ephemerally with the caller's accumulated voice time in hours and minutes.

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
- Database migration `V001__create_tables.sql` exists and creates the initial schema.
- Entities and repositories are implemented.
- JDA startup/configuration is implemented in `JDAConfig`.
- Voice join/leave tracking is implemented through `VoiceEventListener` and `VoiceSessionService`.
- Slash command `/perfil` is implemented through `CommandListener` and `ProfileService`.
- Docker Compose and Dockerfile are present for local/container execution.

## 8. Known Limitations / Next Steps
- Handle bot restarts while users are already connected to voice channels.
- Decide how to handle voice channel moves if they should affect sessions.
- Add tests for `VoiceSessionService` and `ProfileService`.
- Improve command responses and formatting, potentially including seconds or days for long durations.
- Add more slash commands, such as a leaderboard or admin/user lookup.
- Review text encoding in source comments/logs, because some Portuguese characters currently appear mojibaked.
