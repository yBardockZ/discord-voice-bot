package com.bardock.discordvoicebot.controller;

import com.bardock.discordvoicebot.documentation.GuildStatsControllerOpenApi;
import com.bardock.discordvoicebot.dto.RankingResponseDTO;
import com.bardock.discordvoicebot.entity.GuildStats;
import com.bardock.discordvoicebot.repository.GuildStatsRepository;
import com.bardock.discordvoicebot.util.TimeFormatterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/guilds")
@RequiredArgsConstructor
public class GuildStatsController implements GuildStatsControllerOpenApi {

    private final GuildStatsRepository guildStatsRepository;

    @Override
    @GetMapping("/{guildId}/ranking")
    public ResponseEntity<Page<RankingResponseDTO>> getGuildRanking(
            @PathVariable Long guildId,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<GuildStats> statsPage = guildStatsRepository.findByGuildIdOrderByTotalTimeDesc(guildId, pageable);

        Page<RankingResponseDTO> responsePage = statsPage.map(guildStats ->
                RankingResponseDTO.fromEntity(guildStats, TimeFormatterUtil.formatTime(guildStats.getTotalTime()))
        );

        return ResponseEntity.ok(responsePage);

    }

}
