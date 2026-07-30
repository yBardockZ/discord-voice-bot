package com.bardock.discordvoicebot.documentation;

import com.bardock.discordvoicebot.dto.UserProfileResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User Profile", description = "Endpoints to search individual statistics")
public interface UserProfileControllerOpenApi {

    @Operation(
            summary = "Search for voice times",
            description = "Returns the total time the user spent in calls on a specific server."
    )
    ResponseEntity<UserProfileResponseDTO> getUserProfile(Long userId, Long guildId);

}
