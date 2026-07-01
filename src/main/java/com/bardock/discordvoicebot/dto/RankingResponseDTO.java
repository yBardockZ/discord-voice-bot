package com.bardock.discordvoicebot.dto;

import com.bardock.discordvoicebot.entity.GuildStats;

public record RankingResponseDTO(
        Long userId,
        String username,
        String avatarUrl,
        Long totalSeconds,
        String formattedTime
) {
    public static RankingResponseDTO fromEntity(GuildStats guildStats, String formattedTime) {
        return new RankingResponseDTO(
                guildStats.getUser().getId(),
                guildStats.getUser().getUsername(),
                guildStats.getUser().getUserPicture(),
                guildStats.getTotalTime(),
                formattedTime
        );
    }
}
