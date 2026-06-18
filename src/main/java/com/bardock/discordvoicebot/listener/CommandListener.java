package com.bardock.discordvoicebot.listener;

import com.bardock.discordvoicebot.service.ProfileService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandListener extends ListenerAdapter {

    private final ProfileService profileService;

    @Override
    public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("perfil")) {
            return;
        }

        Long userId = event.getUser().getIdLong();
        Long guildId = event.getGuild().getIdLong();

        MessageEmbed response = profileService.buildProfileMessage(userId, guildId);

        event.replyEmbeds(response)
                .queue();
    }
}
