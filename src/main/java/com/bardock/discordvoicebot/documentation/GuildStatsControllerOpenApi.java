package com.bardock.discordvoicebot.documentation;

import com.bardock.discordvoicebot.dto.RankingResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

@Tag(name = "Guild Statistics", description = "Endpoints for retrieving voice channel statistics from Discord guilds")
public interface GuildStatsControllerOpenApi {

    @Operation(
            summary = "Get guild voice ranking",
            description = "Retrieves a paginated ranking of users with the highest voice channel activity inside a specific guild, ordered by total time descending."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ranking page retrieved successfully. Returns an empty page when the guild has no recorded voice activity.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RankingResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid 'guildId' path parameter (e.g. a non-numeric value).",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<Page<RankingResponseDTO>> getGuildRanking(Long guildId, Pageable pageable);

}
