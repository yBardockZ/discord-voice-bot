package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.entity.User;
import com.bardock.discordvoicebot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User getOrCreateAndUpdateUser(Long userId, String username, String avatarURl) {
        return userRepository.findById(userId).map(existingUser -> {
            boolean needsUpdate = false;

            if (!existingUser.getUsername().equals(username)) {
                existingUser.setUsername(username);
                needsUpdate = true;
            }

            if (!Objects.equals(existingUser.getUserPicture(), avatarURl)) {
                needsUpdate = true;
                existingUser.setUserPicture(avatarURl);
            }

            return needsUpdate ? userRepository.save(existingUser) : existingUser;
        }).orElseGet(() -> {
            User newUser = User.builder()
                    .id(userId)
                    .username(username)
                    .userPicture(avatarURl)
                    .build();
            return userRepository.save(newUser);
        });
    }

}
