package com.bardock.discordvoicebot.documentation;

import com.bardock.discordvoicebot.dto.UserProfileResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

@Tag(name = "User Profile", description = "Endpoints to search individual statistics")
public interface UserProfileControllerOpenApi {

    @Operation(
            summary = "Search for voice times",
            description = "Returns the total time the user spent in calls on a specific server."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully, containing the total voice time on the given guild.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid 'userId' or 'guildId' path parameter (e.g. a non-numeric value).",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No voice activity recorded for the given user on the given guild.",
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
    ResponseEntity<UserProfileResponseDTO> getUserProfile(Long userId, Long guildId);

}
