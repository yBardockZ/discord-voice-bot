# Project Context: Discord Voice Tracker Bot

## 1. Objective
A Discord bot built in Java/Spring Boot to track and persist the total time each user spends in voice channels.

## 2. Technical Stack
- **Language:** Java 21 (Maven)
- **Framework:** Spring Boot 4.0.5
- **Discord Library:** JDA 6.4.1
- **Database:** PostgreSQL 15
- **Migrations:** Flyway
- **Infrastructure:** Docker & Docker Compose (Multi-stage build)

## 3. Architecture & Data Model
The project uses a 1:N relationship between Users and Voice Sessions.

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
- `user_id` (BIGINT, FK → users.user_id)
- `started_at` (TIMESTAMPTZ, NOT NULL)
- `ended_at` (TIMESTAMPTZ, nullable) - NULL while session is active
- `created_at` (TIMESTAMPTZ)

### JPA Entities
- `User.java` - Entity with `@PrePersist` / `@PreUpdate` for timestamps, `OneToMany` to VoiceSession
- `VoiceSession.java` - Entity with `ManyToOne` to User

### Repositories
- `UserRepository` - extends `JpaRepository<User, Long>`
- `VoiceSessionRepository` - extends `JpaRepository<VoiceSession, UUID>` with custom queries for finding sessions by user

## 4. Environment Configuration
- **Application Properties:** Uses environment variables with defaults for DB connection
- **Docker Compose:** PostgreSQL service with healthcheck, app service depends on DB
- **Discord Portal:**
    - Permissions: View Channels, Send Messages, Use Slash Commands, Connect
    - Intents: `GUILD_VOICE_STATES`, `GUILD_MESSAGES`, `MESSAGE_CONTENT`

## 5. Current State
- `pom.xml` configured with JDA, Spring Data JPA, Flyway, Lombok
- `docker-compose.yml` and `Dockerfile` ready
- `application.properties` configured with environment variables
- Database migrations created (`V001__create_tables.sql`)
- **Entities implemented:** `User.java`, `VoiceSession.java`
- **Repositories implemented:** `UserRepository.java`, `VoiceSessionRepository.java`
- **Bot service placeholder:** `DiscordBotService.java` (starts JDA connection, no event listeners yet)

## 6. Next Steps
- Implement `VoiceEventListener.java` to handle Discord voice events
- Create services to manage users and sessions
- Implement logic to track session duration and update `total_time`