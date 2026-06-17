package com.bardock.discordvoicebot.config;

import com.bardock.discordvoicebot.listener.CommandListener;
import com.bardock.discordvoicebot.listener.VoiceEventListener;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JDAConfig {

    @Value("${BOT_TOKEN}")
    private String token;

    @Value("${discord.guild.test-id}")
    private String guildTestId;

    private final VoiceEventListener voiceEventListener;
    private final CommandListener commandListener;

    @Bean
    public JDA jda() throws InterruptedException {
        return JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(new ListenerAdapter() {
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
                                        Commands.slash("perfil", "Mostra o seu tempo total acumulado")
                                ).queue();
                                System.out.println("LOG: Comandos registrados na guilda de testes.");
                            }
                        } else {
                            // E produção, os comandos são registrados globalmente (demora até 1h para propagar)
                            event.getJDA().updateCommands().addCommands(
                                    Commands.slash("perfil", "Mostra o seu tempo total acumulado")
                            ).queue();
                            System.out.println("LOG [PROD]: Comandos registrados globalmente.");
                        }

                    }
                })
                .addEventListeners(voiceEventListener)
                .addEventListeners(commandListener)
                .build()
                .awaitReady();
    }

}
