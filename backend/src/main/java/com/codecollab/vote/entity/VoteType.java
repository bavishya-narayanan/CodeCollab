package com.codecollab.vote.entity;

public enum VoteType {
    UP(1),
    DOWN(-1);

    private final int value;

    VoteType(int v) {
        this.value = v;
    }

    public int value() {
        return value;
    }

    public static VoteType fromValue(int v) {
        return v == 1 ? UP : DOWN;
    }
}
