package com.example.clientmod.module;

public enum Category {
    COMBAT("Combat"), MOVEMENT("Movement"), RENDER("Render"), MISC("Misc");

    public final String label;
    Category(String label) { this.label = label; }
}
