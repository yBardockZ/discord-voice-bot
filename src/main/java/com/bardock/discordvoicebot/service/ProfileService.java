package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.entity.GuildStats;
import com.bardock.discordvoicebot.entity.User;
import com.bardock.discordvoicebot.repository.GuildStatsRepository;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import java.awt.*;

import static com.bardock.discordvoicebot.util.TimeFormatterUtil.formatTime;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final GuildStatsRepository guildStatsRepository;

    @Transactional(readOnly = true)
    public MessageEmbed buildRankingMessage(Long guildId) {
        List<GuildStats> topStats = guildStatsRepository.findTop10ByGuildIdOrderByTotalTimeDesc(guildId);

        if (topStats.isEmpty()) {
            return new EmbedBuilder()
                    .setColor(Color.decode("#ED4245"))
                    .setTitle("\uD83C\uDFC6 Ranking de Voz")
                    .setDescription("Nenhum usuário possui horas registradas neste servidor ainda. " +
                            "Entre em uma call para inaugurar o placar!")
                    .build();
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.decode("#F1C40F"));
        embed.setTitle("🏆 Ranking de Voz: " + topStats.getFirst().getUser().getUsername() + " e o Top 10");
        embed.setAuthor("Os mais faladores do servidor", null, null);

        StringBuilder rankingBuilder = new StringBuilder();
        rankingBuilder.append("Confira quem são os membros mais ativos em chamadas de voz:\n\n");

        for (int i = 0; i < topStats.size(); i++) {
            GuildStats stats = topStats.get(i);
            User user = stats.getUser();
            int position = i + 1;

            String positionMarker = switch (position) {
                case 1 -> "🥇";
                case 2 -> "\uD83E\uDD48";
                case 3 -> "\uD83E\uDD49";
                default -> "🔹 " + position + "º";
            };

            String formattedTime = formatTime(stats.getTotalTime());

            rankingBuilder.append(positionMarker)
                    .append(" **")
                    .append(user.getUsername())
                    .append("** - `")
                    .append(formattedTime)
                    .append("`\n");
        }

        embed.setDescription(rankingBuilder.toString());
        embed.setTimestamp(Instant.now());

        return embed.build();
    }

    @Transactional(readOnly = true)
    public MessageEmbed buildProfileMessage(Long userId, Long guildId) {
        return guildStatsRepository.findByUserIdAndGuildId(userId, guildId)
                .map(this::formatExistingUserProfile)
                .orElseGet(this::formatNotExistingUserMessage);
    }

    private MessageEmbed formatExistingUserProfile(GuildStats guildStats) {
        User user = guildStats.getUser();

        long totalSeconds = guildStats.getTotalTime();
        String formattedTime = formatTime(totalSeconds);

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

        embed.setColor(Color.decode("#ED4245")); // vermelho de erro/aviso
        embed.setTitle("🎙️ Perfil de Voz");
        embed.setDescription("Você ainda não tem horas registradas em chamadas de voz. " +
                "Entre em uma call para começar a contar!");


        return embed.build();
    }

}
