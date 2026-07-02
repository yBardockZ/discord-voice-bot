package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.entity.User;
import com.bardock.discordvoicebot.entity.VoiceSession;
import com.bardock.discordvoicebot.repository.VoiceSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class VoiceSessionService {

    private final VoiceSessionRepository voiceSessionRepository;
    private final GuildStatsService guildStatsService;
    private final UserService userService;

    private final Map<Long, Instant> activeSessionsCache = new ConcurrentHashMap<>();

    @Transactional
    public void handleJoin(Long userId, Long guildId, String username, String avatarUrl) {
        // 1. Garante que o usuário existe no banco de dados (se não existir, cria)
        User user = userService.getOrCreateAndUpdateUser(userId, username, avatarUrl);

        // Prevenção : Se o usuário já tinha uma sessão aberta presa no banco, nós a deletamos agora
        voiceSessionRepository.findFirstByUserIdAndGuildIdAndEndedAtIsNull(userId, guildId)
                .ifPresent((orphanSession) -> {
                    activeSessionsCache.remove(userId); // Limpa do cache caso o discord falhe em enviar o evento leave
                    voiceSessionRepository.delete(orphanSession);
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

        // 2. Adiciona o tempo ao usuário
        guildStatsService.addTimeToUser(userId, guildId, durationSeconds);

        // 3. Finaliza a sessão correspondente no banco
        voiceSessionRepository.findFirstByUserIdAndGuildIdAndEndedAtIsNull(userId, guildId)
                .ifPresent((session) -> {
            session.setEndedAt(OffsetDateTime.now());
            voiceSessionRepository.save(session);
        });

        System.out.println("LOG: Usuário ID " + userId + " saiu da call na guilda de ID: " + guildId +
                " Tempo acumulado: " + durationSeconds + "s");
    }

    public void syncSessionsWithDiscord(JDA jda) {
        restoreExistingSessions(jda);
        discoverNewSessions(jda);

    }

    private void restoreExistingSessions(JDA jda) {
        List<VoiceSession> openSessions = voiceSessionRepository.findByEndedAtIsNull();

        for (VoiceSession session : openSessions) {
            var guild = jda.getGuildById(session.getGuildId());
            boolean stillInCall = false;

            if (guild != null) {
                var member = guild.getMemberById(session.getUser().getId());
                // Verifica na API do Discord se o usuário ainda está em algum canal de voz
                if (member != null && member.getVoiceState() != null && member.getVoiceState().inAudioChannel()) {
                    stillInCall = true;
                }
            }

            if (stillInCall) {
                // Usuário continuou na call. Restaura no cache de memória.
                activeSessionsCache.put(session.getUser().getId(), session.getStartedAt().toInstant());
            } else {
                // Usuário saiu enquanto o bot estava off.
                // Deleta a sessão inválida para não registrar um histórico irreal de dias/meses no banco
                voiceSessionRepository.delete(session);
            }
        }
    }

    private void discoverNewSessions(JDA jda) {
        jda.getGuilds().stream()
                .flatMap(guild -> guild.getVoiceChannels().stream())
                .flatMap(channel -> channel.getMembers().stream())
                .filter(member -> !member.getUser().isBot())
                .filter(member -> !activeSessionsCache.containsKey(member.getIdLong()))
                .forEach(member -> {
                    handleJoin(member.getIdLong(), member.getGuild().getIdLong(), member.getUser().getName(),
                            member.getUser().getEffectiveAvatarUrl());
                });
    }

}
