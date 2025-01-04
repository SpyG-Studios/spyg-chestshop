package com.spygstudios.chestshop.enums;

import lombok.Getter;

public enum Permission {
    ADMIN("chestshop.admin"),

    RELOAD("chestshop.reload"),

    CREATE("chestshop.create"),

    REMOVE("chestshop.remove"),

    LIST("chestshop.list");

    @Getter
    private String permission;

    private Permission(String permission) {
        this.permission = permission;
    }
}
