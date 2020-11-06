package me.abdou.reflector.utils;

public enum OnReferenceChange {
    CASCADE("CASCADE"), NO_ACTION("NO ACTION"), RESTRICT("RESTRICT"), SET_NULL("SET NULL"), SET_DEFAULT("SET DEFAULT");

    private final String value;

    OnReferenceChange(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
