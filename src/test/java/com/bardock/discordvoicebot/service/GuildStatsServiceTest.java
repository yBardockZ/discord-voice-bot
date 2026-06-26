package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.entity.GuildStats;
import com.bardock.discordvoicebot.factory.UserTestData;
import com.bardock.discordvoicebot.repository.GuildStatsRepository;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuildStatsServiceTest {

    @Mock
    private GuildStatsRepository guildStatsRepository;

    @InjectMocks
    private GuildStatsService guildStatsService;

    @Captor
    private ArgumentCaptor<GuildStats> statsCaptor;

    @Test
    @DisplayName("Must create a new register with the initial time if the user don`t has no history")
    void addTimeToUser_newRecord() {
        // --- ARRANGE ---
        Long guildId = 456L;
        Long durationToAdd = 300L;

        when(guildStatsRepository.findByUserIdAndGuildId(UserTestData.DEFAULT_ID, guildId))
                .thenReturn(Optional.empty());

        // --- ACT ---
        guildStatsService.addTimeToUser(UserTestData.DEFAULT_ID, guildId, durationToAdd);

        // --- ASSERT ---
        verify(guildStatsRepository, times(1)).save(statsCaptor.capture());

        GuildStats savedStats = statsCaptor.getValue();

        assertThat(savedStats.getTotalTime()).isEqualTo(durationToAdd);
        assertThat(savedStats.getGuildId()).isEqualTo(guildId);
        assertThat(savedStats.getUser().getId()).isEqualTo(UserTestData.DEFAULT_ID);
    }

    @Test
    @DisplayName("It should add the time to the user's existing history in the guild")
    void addTimeToUser_ExistingRecord() {
        // --- ARRANGE ---
        Long guildId = 456L;
        Long newDuration = 200L;

        GuildStats existingStats = new GuildStats();
        existingStats.setId(1L);
        existingStats.setGuildId(guildId);
        existingStats.setTotalTime(1000L);

        when(guildStatsRepository.findByUserIdAndGuildId(UserTestData.DEFAULT_ID, guildId))
                .thenReturn(Optional.of(existingStats));

        // --- ACTION ---
        guildStatsService.addTimeToUser(UserTestData.DEFAULT_ID, guildId, newDuration);

        // --- ASSERT ---
        verify(guildStatsRepository, times(1)).save(statsCaptor.capture());

        GuildStats savedStats = statsCaptor.getValue();

        assertThat(savedStats.getTotalTime()).isEqualTo(1200L);
    }


}
