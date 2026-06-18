package com.bardock.discordvoicebot.repository;

import com.bardock.discordvoicebot.entity.GuildStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuildStatsRepository extends JpaRepository<GuildStats, Long> {

    // Para o comando /perfil (busca o tempo do usuário no servidor específico)
    Optional<GuildStats> findByUserIdAndGuildId(Long userId, Long guildId);

    // Para o comando /ranking (busca o Top 10 utilizando o índice composto)
    List<GuildStats> findTop10ByGuildIdOrderByTotalTimeDesc(Long guildId);

}
