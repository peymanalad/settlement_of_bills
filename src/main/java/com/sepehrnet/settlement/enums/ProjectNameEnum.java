package com.sepehrnet.settlement.enums;

public enum ProjectNameEnum {

    NASIM("NASIM"),
    GAS("GAS"),
    DAY("DAY");

    private String value;

    ProjectNameEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ProjectNameEnum parse(String value) {
        ProjectNameEnum[] arr$ = values();
        for (ProjectNameEnum val : arr$) {
            if (val.getValue().equalsIgnoreCase(value)) {
                return val;
            }
        }
        return ProjectNameEnum.NASIM;
    }
}
