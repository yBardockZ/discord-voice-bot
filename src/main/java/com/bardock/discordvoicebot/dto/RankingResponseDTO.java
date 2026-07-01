package com.bardock.discordvoicebot.dto;

import com.bardock.discordvoicebot.entity.GuildStats;

public record RankingResponseDTO(
        Long userId,
        String username,
        String avatarUrl,
        Long totalSeconds,
        String formattedTime
) {
    public static RankingResponseDTO fromEntity(GuildStats stats, String formattedTime) {
        return new RankingResponseDTO(
                stats.getUser().getId(),
                stats.getUser().getUsername(),
                stats.getUser().getUserPicture(),
                stats.getTotalTime(),
                formattedTime
        );
    }
}
