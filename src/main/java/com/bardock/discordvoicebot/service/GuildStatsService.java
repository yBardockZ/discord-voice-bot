package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.entity.GuildStats;
import com.bardock.discordvoicebot.entity.User;
import com.bardock.discordvoicebot.repository.GuildStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuildStatsService {

    private final GuildStatsRepository guildStatsRepository;

    @Transactional
    public void addTimeToUser(Long userId, Long guildId, Long durationSeconds) {
        User userReference = new User();
        userReference.setId(userId);

        GuildStats guildStats = guildStatsRepository.findByUserIdAndGuildId(userId, guildId)
                .orElseGet(() -> GuildStats.builder()
                        .user(userReference)
                        .guildId(guildId)
                        .totalTime(0L)
                        .build());

        guildStats.setTotalTime(guildStats.getTotalTime() + durationSeconds);
        guildStatsRepository.save(guildStats);
    }

    public List<GuildStats> getTop10Raking(Long guildId) {
        return guildStatsRepository.findTop10ByGuildIdOrderByTotalTimeDesc(guildId);
    }

}
