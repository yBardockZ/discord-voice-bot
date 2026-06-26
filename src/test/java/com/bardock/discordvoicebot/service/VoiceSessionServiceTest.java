package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.entity.User;
import com.bardock.discordvoicebot.entity.VoiceSession;
import com.bardock.discordvoicebot.factory.UserTestData;
import com.bardock.discordvoicebot.repository.VoiceSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoiceSessionServiceTest {

    @Mock
    private VoiceSessionRepository voiceSessionRepository;

    @Mock
    private GuildStatsService guildStatsService;

    @Mock
    private UserService userService;

    @InjectMocks
    private VoiceSessionService voiceSessionService;

    @Test
    @DisplayName("Must register new database session when user enters call")
    void handleJoin_sucess() {

        Long guildId = 456L;
        User mockUser = UserTestData.createValidUser();

        // Quando o UserService for chamado, devolva o mockUser
        when(userService.getOrCreateAndUpdateUser(UserTestData.DEFAULT_ID, UserTestData.DEFAULT_USERNAME,
                UserTestData.DEFAULT_AVATAR)).thenReturn(mockUser);

        // Quando procurar por uma sessão órfã, finja que o banco não achou nada (Optional.empty)
        when(voiceSessionRepository.findFirstByUserIdAndGuildIdAndEndedAtIsNull(UserTestData.DEFAULT_ID, guildId))
                .thenReturn(Optional.empty());

        // ACTION
        voiceSessionService.handleJoin(UserTestData.DEFAULT_ID, guildId, UserTestData.DEFAULT_USERNAME,
                UserTestData.DEFAULT_AVATAR);

        // --- ASSERT (Verificação) ---
        // Verificamos se o repositório foi chamado pra salvar a nova sessão EXATAMENTE 1 vez
        verify(voiceSessionRepository, times(1)).save(any(VoiceSession.class));

        // Verificamos se, como não havia sessão órfã, o delete NUNCA foi chamado
        verify(voiceSessionRepository, never()).delete(any(VoiceSession.class));

    }

    @Test
    @DisplayName("Must delete orphaned session before create a new one when entering call")
    void handleJoin_WithOrphanSession() {
        // -- ARRANGE --
        Long guildId = 456L;

        User mockUser = UserTestData.createValidUser();

        // Criamos uma sessão "falsa" representando a órfã presa no banco
        VoiceSession orphanSession = new VoiceSession();
        orphanSession.setId(UUID.randomUUID());
        orphanSession.setUser(mockUser);
        orphanSession.setGuildId(guildId);

        // "Quando pedir o usuário, devolva o mockUser"
        when(userService.getOrCreateAndUpdateUser(123L, UserTestData.DEFAULT_USERNAME,
                UserTestData.DEFAULT_AVATAR)).thenReturn(mockUser);

        // "Quando procurar por sessão órfã, finja que o banco achou essa sessão antiga"
        when(voiceSessionRepository.findFirstByUserIdAndGuildIdAndEndedAtIsNull(UserTestData.DEFAULT_ID, guildId))
                .thenReturn(Optional.of(orphanSession));

        // -- ACTION --
        voiceSessionService.handleJoin(UserTestData.DEFAULT_ID, guildId, UserTestData.DEFAULT_USERNAME,
                UserTestData.DEFAULT_AVATAR);

        // -- ASSERT --
        // 1. Verificamos se o serviço deletou a sessão órfã que estava presa
        verify(voiceSessionRepository, times(1)).delete(orphanSession);

        // 2. Verificamos se o serviço salvou a nova sessão da chamada atual
        verify(voiceSessionRepository, times(1)).save(any(VoiceSession.class));

    }

    @Test
    @DisplayName("Most calculate duration, delegate to GuildStats and finish session")
    void handleLeave_Sucess() {
        // --- ARRANGE ---
        Long guildId = 456L;
        Long userId = UserTestData.DEFAULT_ID;

        Instant tenMinutesAgo = Instant.now().minusSeconds(600);

        Map<Long, Instant> fakeCache = new ConcurrentHashMap<>();
        fakeCache.put(userId, tenMinutesAgo);
        ReflectionTestUtils.setField(voiceSessionService, "activeSessionsCache", fakeCache);

        VoiceSession openSession = new VoiceSession();
        openSession.setId(UUID.randomUUID());
        openSession.setEndedAt(null);

        when(voiceSessionRepository.findFirstByUserIdAndGuildIdAndEndedAtIsNull(userId, guildId))
                .thenReturn(Optional.of(openSession));

        // --- ACTION ---
        voiceSessionService.handleLeave(userId, guildId);

        // --- ASSERT ---
        verify(guildStatsService, times(1)).addTimeToUser(
                eq(userId),
                eq(guildId),
                longThat(duration -> duration >= 600L && duration <= 605L) // evitar falsos negativos caso o processador atrase 1ms a mais
        );

        verify(voiceSessionRepository, times(1)).save(openSession);

        // garante que o usuário foi removido do cache falso
        assert fakeCache.isEmpty();
    }



}
