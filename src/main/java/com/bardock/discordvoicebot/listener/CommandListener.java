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
        if (event.getGuild() == null) {
            event.reply("❌ Este comando só pode ser usado dentro de um servidor!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Long userId = event.getUser().getIdLong();
        Long guildId = event.getGuild().getIdLong();

        switch (event.getName()) {
            case "perfil" -> {
                event.deferReply().queue();
                var embed = profileService.buildProfileMessage(userId, guildId);
                event.getHook().sendMessageEmbeds(embed).queue();
            }
            case "ranking" -> {
                event.deferReply().queue();
                var embed = profileService.buildRankingMessage(guildId);
                event.getHook().sendMessageEmbeds(embed).queue();
            }
        }

    }
}
