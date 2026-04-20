package com.bardock.discordvoicebot.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JDAConfig {

    @Value("${BOT_TOKEN}")
    private String token;

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
                    }
                })
                .build()
                .awaitReady();
    }

}
