package com.bardock.discordvoicebot.documentation;

import com.bardock.discordvoicebot.dto.RankingResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Guild Statistics", description = "Endpoints for retrieving voice channel statistics from Discord guilds")
public interface GuildStatsControllerOpenApi {

    @Operation(
            summary = "Get guild voice ranking",
            description = "Retrieves a paginated ranking of users with the highest voice channel activity inside a specific guild, ordered by total time descending."
    )
    ResponseEntity<Page<RankingResponseDTO>> getGuildRanking(Long guildId, Pageable pageable);

}
