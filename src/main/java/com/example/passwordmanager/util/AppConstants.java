package com.example.passwordmanager.util;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String ROLE_USER = "ROLE_USER";

    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_REGISTER = "REGISTER";
    public static final String ACTION_ADD_ENTRY = "ADD_ENTRY";
    public static final String ACTION_UPDATE_ENTRY = "UPDATE_ENTRY";
    public static final String ACTION_DELETE_ENTRY = "DELETE_ENTRY";
    public static final String ACTION_VIEW_ENTRY = "VIEW_ENTRY";

    public static final String TARGET_USER = "USER";
    public static final String TARGET_VAULT_ENTRY = "VAULT_ENTRY";

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int DEFAULT_GENERATED_PASSWORD_LENGTH = 12;
}