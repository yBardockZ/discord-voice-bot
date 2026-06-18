package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.entity.User;
import com.bardock.discordvoicebot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MessageEmbed buildProfileMessage(Long userId) {
        return userRepository.findById(userId)
                .map(this::formatExistingUserProfile)
                .orElse(formatNotExistingUserMessage());
    }

    private MessageEmbed formatExistingUserProfile(User user) {
        long totalSeconds = user.getTotalTime();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        String formattedTime = hours + " h, " + minutes + " m";

        EmbedBuilder embed = new EmbedBuilder();

        // Define a cor da barra lateral do card
        embed.setColor(Color.decode("#5865F2"));

        // Coloca o avatar do usuário no canto superior direito
        embed.setThumbnail(user.getUserPicture());

        // Título e descrição (usando ** para negrito do Markdown do Discord)
        embed.setTitle("🎙️ Perfil de Voz: " + user.getUsername());
        embed.setDescription("Aqui estão suas estatísticas de tempo em call:\n\n" +
                "**Tempo Total Acumulado:**\n" +
                "⏳ " + formattedTime);

        return embed.build();

    }

    private MessageEmbed formatNotExistingUserMessage() {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(Color.decode("#ED4245"));
        embed.setTitle("🎙️ Perfil de Voz");
        embed.setDescription("Você ainda não tem horas registradas em chamadas de voz. Entre em uma call para começar a contar!");


        return embed.build();
    }
}
