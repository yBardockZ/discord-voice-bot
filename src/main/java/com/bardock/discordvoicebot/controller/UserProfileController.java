package com.bardock.discordvoicebot.controller;

import com.bardock.discordvoicebot.dto.UserProfileResponseDTO;
import com.bardock.discordvoicebot.entity.GuildStats;
import com.bardock.discordvoicebot.exception.ResourceNotFoundException;
import com.bardock.discordvoicebot.repository.GuildStatsRepository;
import com.bardock.discordvoicebot.util.TimeFormatterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final GuildStatsRepository guildStatsRepository;

    @GetMapping("/{userId}/guilds/{guildId}/profile")
    public ResponseEntity<UserProfileResponseDTO> getUserProfile(
            @PathVariable Long userId,
            @PathVariable Long guildId) {

        GuildStats guildStats = guildStatsRepository.findByUserIdAndGuildId(userId, guildId)
                .orElseThrow(() -> new ResourceNotFoundException("User ID: " + userId + " have no hours logged on the server: " +
                        guildId + "."));

        String formattedTime = TimeFormatterUtil.formatTime(guildStats.getTotalTime());
        UserProfileResponseDTO responseDTO = UserProfileResponseDTO.fromEntity(guildStats, formattedTime);

        return ResponseEntity.ok(responseDTO);
    }


}
