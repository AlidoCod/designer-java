package com.example.base.bean.entity.base;

public enum Role {
    /**
     *
     */
    User,
    Admin;

    public static Role search(int value) {
        for (final Role role : values()) {
            if (role.ordinal() == value) {
                return role;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(search(1));
    }
}
