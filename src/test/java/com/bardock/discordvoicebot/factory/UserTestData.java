package com.bardock.discordvoicebot.factory;

import com.bardock.discordvoicebot.entity.User;

public class UserTestData {

    public static final long DEFAULT_ID = 123L;
    public static final String DEFAULT_USERNAME = "Bardock";
    public static final String DEFAULT_AVATAR = "url-da-foto";

    public static User createValidUser() {
        return User.builder()
                .id(DEFAULT_ID)
                .username(DEFAULT_USERNAME)
                .userPicture(DEFAULT_AVATAR)
                .build();
    }
}
