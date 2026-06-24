package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.entity.User;
import com.bardock.discordvoicebot.entity.VoiceSession;
import com.bardock.discordvoicebot.repository.VoiceSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
        Long userId = 123L;
        Long guildId = 456L;
        String username = "Bardock";
        String avatarUrl = "url-da-foto";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername(username);

        // Quando o UserService for chamado, devolva o mockUser
        when(userService.getOrCreateAndUpdateUser(userId, username, avatarUrl)).thenReturn(mockUser);

        // Quando procurar por uma sessão órfã, finja que o banco não achou nada (Optional.empty)
        when(voiceSessionRepository.findFirstByUserIdAndGuildIdAndEndedAtIsNull(userId, guildId))
                .thenReturn(Optional.empty());

        // Ação
        voiceSessionService.handleJoin(userId, guildId, username, avatarUrl);

        // --- ASSERT (Verificação) ---
        // Verificamos se o repositório foi chamado pra salvar a nova sessão EXATAMENTE 1 vez
        verify(voiceSessionRepository, times(1)).save(any(VoiceSession.class));

        // Verificamos se, como não havia sessão órfã, o delete NUNCA foi chamado
        verify(voiceSessionRepository, never()).delete(any(VoiceSession.class));

    }

}
