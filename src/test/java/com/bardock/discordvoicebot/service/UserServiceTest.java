package com.bardock.discordvoicebot.service;

import com.bardock.discordvoicebot.entity.User;
import com.bardock.discordvoicebot.factory.UserTestData;
import com.bardock.discordvoicebot.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    @DisplayName("Must create and save a new user if one does not exist in the database")
    void getOrCreateAndUpdateUser_NewUser() {
        // --- ARRANGE ---
        when(userRepository.findById(UserTestData.DEFAULT_ID)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // --- ACT ---
        User result = userService.getOrCreateAndUpdateUser(UserTestData.DEFAULT_ID, UserTestData.DEFAULT_USERNAME,
                UserTestData.DEFAULT_AVATAR);

        // --- ASSERT ---
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertThat(result.getId()).isEqualTo(UserTestData.DEFAULT_ID);
        assertThat(userCaptor.getValue().getUsername()).isEqualTo(UserTestData.DEFAULT_USERNAME);

    }

    @Test
    @DisplayName("It should just return the user without calling 'save' if the data is identical")
    void getOrCreateAndUpdateUser_ExistingUser_NoChanges() {
        // --- ARRANGE ---
        User existingUser = UserTestData.createValidUser();
        when(userRepository.findById(UserTestData.DEFAULT_ID)).thenReturn(Optional.of(existingUser));

        // --- ACT ---
        User result = userService.getOrCreateAndUpdateUser(UserTestData.DEFAULT_ID, UserTestData.DEFAULT_USERNAME,
                UserTestData.DEFAULT_AVATAR);

        // --- ASSERT ---
        verify(userRepository, never()).save(any(User.class));
        assertThat(result).isEqualTo(existingUser);
    }

    @Test
    @DisplayName("It should update and save the user if the name or photo changes in Discord")
    void getOrCreateAndUpdateUser_ExistingUser_WithChanges() {
        // --- ARRANGE ---
        User existingUser = UserTestData.createValidUser();
        String newDiscordName = "Bardock_Super_Saiyan";

        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- ACT ---
        User result = userService.getOrCreateAndUpdateUser(existingUser.getId(), newDiscordName, existingUser.getUserPicture());

        // --- ASSERT ---
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(newDiscordName);
        assertThat(result.getUsername()).isEqualTo(newDiscordName);
    }

}
