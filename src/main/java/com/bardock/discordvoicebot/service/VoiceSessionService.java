package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.repository.UserRepository;
import com.bardock.discordvoicebot.repository.VoiceSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class VoiceSessionService {

    private final UserRepository userRepository;
    private final VoiceSessionRepository voiceSessionRepository;

    private final Map<Long, Instant> activeSessionsCache = new ConcurrentHashMap<>();

    @Transactional
    public void handleJoin(Long userId, String username, String avatarUrl) {

    }

    @Transactional
    public void handleLeave(Long userId, String username, String avatarUrl) {

    }



}
