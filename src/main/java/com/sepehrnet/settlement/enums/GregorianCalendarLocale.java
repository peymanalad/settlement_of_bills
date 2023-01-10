package com.sepehrnet.settlement.enums;

public enum GregorianCalendarLocale {

    LOCALE_FA("fa_IR@calendar=gregorian"),
    LOCALE_EN("en_US@calendar=gregorian");

    private String value;

    GregorianCalendarLocale(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GregorianCalendarLocale parse(String value) {
        GregorianCalendarLocale[] arr$ = values();
        for (GregorianCalendarLocale val : arr$) {
            if (val.getValue().equalsIgnoreCase(value)) {
                return val;
            }
        }
        return GregorianCalendarLocale.LOCALE_EN;
    }
}
