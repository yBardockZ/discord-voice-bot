package com.bardock.discordvoicebot.controller;

import com.bardock.discordvoicebot.dto.RankingResponseDTO;
import com.bardock.discordvoicebot.entity.GuildStats;
import com.bardock.discordvoicebot.repository.GuildStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guilds")
@RequiredArgsConstructor
public class GuildStatsController {

    private final GuildStatsRepository guildStatsRepository;

    @GetMapping("/{guildId}/ranking")
    public ResponseEntity<List<RankingResponseDTO>> getGuildRanking(@PathVariable Long guildId) {
        List<GuildStats> topStats = guildStatsRepository.findTop10ByGuildIdOrderByTotalTimeDesc(guildId);

        if (topStats.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<RankingResponseDTO> responseBody = topStats.stream()
                .map(guildStats -> RankingResponseDTO.fromEntity(guildStats, formatTime(guildStats.getTotalTime())))
                .toList();

        return ResponseEntity.ok(responseBody);

    }

    private String formatTime(Long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        return hours + "h " + minutes + "m";
    }
}