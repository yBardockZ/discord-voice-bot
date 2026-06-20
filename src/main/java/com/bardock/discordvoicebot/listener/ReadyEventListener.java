package com.bardock.discordvoicebot.listener;

import com.bardock.discordvoicebot.service.VoiceSessionService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadyEventListener extends ListenerAdapter {

    @Value("${discord.guild.test-id:}")
    private String guildTestId;

    private final VoiceSessionService voiceSessionService;

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("=========================================");
        System.out.println("BOT ESTÁ ONLINE!");
        System.out.println("Conectado como: " + event.getJDA().getSelfUser().getAsTag());
        System.out.println("Servidores: " + event.getGuildAvailableCount());
        System.out.println("=========================================");

        if (guildTestId != null && !guildTestId.isBlank()) {
            var guild = event.getJDA().getGuildById(guildTestId);
            if (guild != null) {
                guild.updateCommands().addCommands(
                        Commands.slash("perfil", "Mostra o seu tempo total acumulado"),
                        Commands.slash("ranking", "Mostra o Top 10 " +
                                "membros mais ativos nas calls do servidor")
                ).queue();
                System.out.println("LOG: Comandos registrados na guilda de testes.");
            }
        } else {
            // E produção, os comandos são registrados globalmente (demora até 1h para propagar)
            event.getJDA().updateCommands().addCommands(
                    Commands.slash("perfil", "Mostra o seu tempo total acumulado"),
                    Commands.slash("ranking", "Mostra o Top 10 " +
                            "membros mais ativos nas calls do servidor")
            ).queue();
            System.out.println("LOG [PROD]: Comandos registrados globalmente.");
        }

        voiceSessionService.syncSessionsWithDiscord(event.getJDA());
        System.out.println("✅ Sessões sincronizadas com o estado atual do Discord!");
    }

}
