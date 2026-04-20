package com.bardock.discordvoicebot.repository;

import com.bardock.discordvoicebot.entity.VoiceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoiceSessionRepository extends JpaRepository<VoiceSession, UUID> {

    List<VoiceSession> findByUserId(Long userId);

    List<VoiceSession> findByUserIdAndEndedAtIsNull(Long userId);
}