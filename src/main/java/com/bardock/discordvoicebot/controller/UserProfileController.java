package com.bardock.discordvoicebot.controller;

import com.bardock.discordvoicebot.dto.UserProfileResponseDTO;
import com.bardock.discordvoicebot.repository.GuildStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.bardock.discordvoicebot.util.TimeFormatterUtil.formatTime;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final GuildStatsRepository guildStatsRepository;

    @GetMapping("/{userId}/guilds/{guildId}/profile")
    public ResponseEntity<UserProfileResponseDTO> getUserProfile(
            @PathVariable Long userId,
            @PathVariable Long guildId) {

        return guildStatsRepository.findByUserIdAndGuildId(userId, guildId)
                .map(guildStats -> {
                    String formattedTime = formatTime(guildStats.getTotalTime());
                    return ResponseEntity.ok(UserProfileResponseDTO.fromEntity(guildStats, formattedTime));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());

    }


}
