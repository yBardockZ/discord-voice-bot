package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.entity.User;
import com.bardock.discordvoicebot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public String buildProfileMessage(Long userId) {
        return userRepository.findById(userId)
                .map(this::formatExistingUserProfile)
                .orElse("Voce ainda nao possui tempo registrado em canais de voz.");
    }

    private String formatExistingUserProfile(User user) {
        long totalSeconds = user.getTotalTime();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        return "Perfil de voz de " + user.getUsername() + "\n\n"
                + "Tempo total: " + hours + "h " + minutes + "min";
    }
}
