package com.bardock.discordvoicebot.dto;

import com.bardock.discordvoicebot.entity.GuildStats;

public record UserProfileResponseDTO(
        Long userId,
        String username,
        String avatarUrl,
        Long guildId,
        Long totalSeconds,
        String formattedTime
) {
    public static UserProfileResponseDTO fromEntity (GuildStats guildStats, String formattedTime) {
        return new UserProfileResponseDTO(
                guildStats.getUser().getId(),
                guildStats.getUser().getUsername(),
                guildStats.getUser().getUserPicture(),
                guildStats.getGuildId(),
                guildStats.getTotalTime(),
                formattedTime
        );
    }
}
