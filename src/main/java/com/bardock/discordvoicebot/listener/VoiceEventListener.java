package com.bardock.discordvoicebot.listener;

import com.bardock.discordvoicebot.service.VoiceSessionService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoiceEventListener extends ListenerAdapter {

    private final VoiceSessionService voiceSessionService;

    @Override
    public void onGuildVoiceUpdate(@NonNull GuildVoiceUpdateEvent event) {
        Long userId = event.getMember().getIdLong();
        String username = event.getMember().getUser().getName();
        String avatarUrl = event.getMember().getUser().getEffectiveAvatarUrl();

        // CENÁRIO 1: Usuário ENTROU em um canal de voz (não estava em nenhum antes)
        if (event.getChannelLeft() == null && event.getChannelJoined() != null) {
            voiceSessionService.handleJoin(userId, username, avatarUrl);
        }

        // CENÁRIO 2: Usuário SAIU de um canal de voz (e não entrou em outro)
        else if (event.getChannelLeft() != null && event.getChannelJoined() == null) {
            voiceSessionService.handleLeave(userId);
        }



        super.onGuildVoiceUpdate(event);
    }

}
