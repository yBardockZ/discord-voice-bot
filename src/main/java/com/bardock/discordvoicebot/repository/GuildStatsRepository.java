package com.bardock.discordvoicebot.repository;

import com.bardock.discordvoicebot.entity.GuildStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuildStatsRepository extends JpaRepository<GuildStats, Long> {

    Optional<GuildStats> findByUserIdAndGuildId(Long userId, Long guildId);

    List<GuildStats> findTop10ByGuildIdOrderByTotalTimeDesc(Long guildId);

    Page<GuildStats> findByGuildIdOrderByTotalTimeDesc(Long guildId, Pageable pageable);

}
