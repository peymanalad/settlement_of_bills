package com.sepehrnet.settlement.enums;

public enum PersianCalendarLocale {

    LOCALE_FA("fa_IR@calendar=persian"),
    LOCALE_EN("en_US@calendar=persian");

    private String value;

    PersianCalendarLocale(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PersianCalendarLocale parse(String value) {
        PersianCalendarLocale[] arr$ = values();
        for (PersianCalendarLocale val : arr$) {
            if (val.getValue().equalsIgnoreCase(value)) {
                return val;
            }
        }
        return PersianCalendarLocale.LOCALE_EN;
    }
}
