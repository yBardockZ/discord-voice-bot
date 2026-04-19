# Project Context: Discord Voice Tracker Bot

## 1. Objective
A Discord bot built in Java/Spring Boot to track and persist the total time each user spends in voice channels.

## 2. Technical Stack
- **Language:** Java 21 (Maven)
- **Framework:** Spring Boot 4.0.5
- **Discord Library:** JDA 6.4.1
- **Database:** PostgreSQL 15
- **Infrastructure:** Docker & Docker Compose (Multi-stage build)

## 3. Architecture & Data Model
The project uses a 1:N relationship between Users and Voice Sessions.
- **Users Table:** `discord_id (PK)`, `username`, `total_voice_time (Long)`.
- **Voice Sessions Table:** `id (PK)`, `user_id (FK)`, `start_time`, `end_time`.
- **In-Memory Cache:** A `HashMap<Long, Instant>` is used to track active sessions (O(1) access) before persisting to the DB on voice leave events.

## 4. Environment Configuration
- **Application Properties:** Uses environment variables with defaults: `${DB_HOST:localhost}`, `${BOT_TOKEN}`.
- **Docker Compose:** Configured with a PostgreSQL service and a `healthcheck` to ensure the DB is ready before the App starts.
- **Discord Portal:** - Permissions: View Channels, Send Messages, Use Slash Commands, Connect.
    - Intents: `GUILD_VOICE_STATES` (Code-level) and `GUILD_MEMBERS` (Privileged).

## 5. Current State
- `pom.xml` is configured.
- `docker-compose.yml` and `Dockerfile` are drafted.
- `application.properties` is configured.
- **Next Step:** Implement JPA Entities (`User.java`, `VoiceSession.java`) and the `VoiceEventListener.java` to handle Discord events.