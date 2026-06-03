package com.bardock.discordvoicebot.listener;

import com.bardock.discordvoicebot.entity.User;
import com.bardock.discordvoicebot.entity.VoiceSession;
import com.bardock.discordvoicebot.repository.UserRepository;
import com.bardock.discordvoicebot.repository.VoiceSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class VoiceEventListener extends ListenerAdapter {

    private final UserRepository userRepository;
    private final VoiceSessionRepository voiceSessionRepository;

    // Cache em memória para rastrear o início da sessão: <UserId, InstantDeEntrada>
    // ConcurrentHashMap é usado por segurança, já que o JDA trabalha com múltiplas Threads assíncronas
    private final Map<Long, Instant> activeSessionsCache = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public void onGuildVoiceUpdate(@NonNull GuildVoiceUpdateEvent event) {
        Long userId = event.getMember().getIdLong();
        String username = event.getMember().getUser().getName();
        String avatarUrl = event.getMember().getUser().getEffectiveAvatarUrl();

        // CENÁRIO 1: Usuário ENTROU em um canal de voz (não estava em nenhum antes)
        if (event.getChannelLeft() == null && event.getChannelJoined() != null) {
            handleJoin(userId, username, avatarUrl);
        }

        // CENÁRIO 2: Usuário SAIU de um canal de voz (e não entrou em outro)
        else if (event.getChannelLeft() != null && event.getChannelJoined() == null) {
            handleLeave(userId);
        }



        super.onGuildVoiceUpdate(event);
    }

    private void handleJoin(Long userId, String username, String avatarUrl) {
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
                .startedAt(OffsetDateTime.now())
                .build();

        voiceSessionRepository.save(session);

        System.out.println("LOG: " + username + " entrou no canal de voz");
    }

    private void handleLeave(Long userId) {
        Instant leaveInstant = Instant.now();
        Instant joinInstant = activeSessionsCache.remove(userId);

        if (joinInstant != null) {
            // 1. Calcula a duração da sessão atual em segundos
            long durationSeconds = Duration.between(joinInstant, leaveInstant).toSeconds();

            // 2. Atualiza o tempo acummulado do usuário no banco
            userRepository.findById(userId).ifPresent(user -> {
                user.setTotalTime(user.getTotalTime() + durationSeconds);
                userRepository.save(user);

                // 3. Finaliza a sessão correspondente no banco
                voiceSessionRepository.findFirstByUserIdAndEndedAtIsNull(userId).ifPresent((session) -> {
                    session.setEndedAt(OffsetDateTime.now());
                    voiceSessionRepository.save(session);
                });

                System.out.println("LOG: " + user.getUsername() + " saiu da call. Tempo acumulado: +" + durationSeconds
                + "s");
            });
        }
    }

}
