package com.sepehrnet.settlement.enums;

public enum Language {

    ENGLISH("en"),
    PERSIAN("fa");

    private String value;

    Language(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Language parse(String value) {
        Language[] arr$ = values();
        for (Language val : arr$) {
            if (val.getValue().equalsIgnoreCase(value)) {
                return val;
            }
        }
        return Language.ENGLISH;
    }

}
