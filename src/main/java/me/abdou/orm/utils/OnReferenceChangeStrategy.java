package me.abdou.orm.utils;

public enum OnReferenceChangeStrategy {
    CASCADE("CASCADE"), NO_ACTION("NO ACTION"), RESTRICT("RESTRICT"), SET_NULL("SET NULL"), SET_DEFAULT("SET DEFAULT"),;

    private String value;

    OnReferenceChangeStrategy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
