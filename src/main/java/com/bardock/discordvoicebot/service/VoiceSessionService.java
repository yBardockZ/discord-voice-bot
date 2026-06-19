package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.entity.GuildStats;
import com.bardock.discordvoicebot.entity.User;
import com.bardock.discordvoicebot.entity.VoiceSession;
import com.bardock.discordvoicebot.repository.GuildStatsRepository;
import com.bardock.discordvoicebot.repository.UserRepository;
import com.bardock.discordvoicebot.repository.VoiceSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class VoiceSessionService {

    private final UserRepository userRepository;
    private final VoiceSessionRepository voiceSessionRepository;
    private final GuildStatsRepository guildStatsRepository;

    private final Map<Long, Instant> activeSessionsCache = new ConcurrentHashMap<>();

    @Transactional
    public void handleJoin(Long userId, Long guildId, String username, String avatarUrl) {
        // 1. Garante que o usuário existe no banco de dados (se não existir, cria)
        User user = userRepository.findById(userId).orElseGet(() -> {
            User newUser = User.builder()
                    .id(userId)
                    .username(username)
                    .userPicture(avatarUrl)
                    .build();
            return userRepository.save(newUser);
        });

        // 2. Registra o momento da entrada no cache em memória (RAM)
        Instant now = Instant.now();
        activeSessionsCache.put(userId, now);

        // 3. Salva uma nova sessão com ended_at nulo no PostgreSQL
        VoiceSession session = VoiceSession.builder()
                .user(user)
                .guildId(guildId)
                .startedAt(OffsetDateTime.now())
                .build();

        voiceSessionRepository.save(session);
        System.out.println("LOG: " + username + " entrou no canal de voz na guilda de ID: " + guildId);
    }

    @Transactional
    public void handleLeave(Long userId, Long guildId) {
        Instant leaveInstant = Instant.now();
        Instant joinInstant = activeSessionsCache.remove(userId);

        // Proteção preventiva: se o cache sumir ou o método for chamado duplicado
        if (joinInstant == null) {
            return;
        }

        // 1. Calcula a duração da sessão atual em segundos
        long durationSeconds = Duration.between(joinInstant, leaveInstant).toSeconds();

        // 2. Busca o usuário
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found in the database upon exiting the call."));

        // 3. Atualiza ou CRIA o registro na GuildStats
        GuildStats guildStats = guildStatsRepository.findByUserIdAndGuildId(userId, guildId).orElseGet(() -> {
            return GuildStats.builder()
                    .user(user)
                    .guildId(guildId)
                    .totalTime(0L)
                    .build();
        });

        guildStats.setTotalTime(guildStats.getTotalTime() + durationSeconds);
        guildStatsRepository.save(guildStats);

        // 3. Finaliza a sessão correspondente no banco
        voiceSessionRepository.findFirstByUserIdAndGuildIdAndEndedAtIsNull(userId, guildId)
                .ifPresent((session) -> {
            session.setEndedAt(OffsetDateTime.now());
            voiceSessionRepository.save(session);
        });

        System.out.println("LOG: " + user.getUsername() + " saiu da call na guilda de ID: " + guildStats.getGuildId() +
                " Tempo acumulado: " + durationSeconds + "s");
    }

}
