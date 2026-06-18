package com.bardock.discordvoicebot.config;

import com.bardock.discordvoicebot.listener.CommandListener;
import com.bardock.discordvoicebot.listener.ReadyEventListener;
import com.bardock.discordvoicebot.listener.VoiceEventListener;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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

    private final VoiceEventListener voiceEventListener;
    private final CommandListener commandListener;
    private final ReadyEventListener readyEventListener;

    @Bean
    public JDA jda() throws InterruptedException {
        return JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(readyEventListener)
                .addEventListeners(voiceEventListener)
                .addEventListeners(commandListener)
                .build()
                .awaitReady();
    }

}
