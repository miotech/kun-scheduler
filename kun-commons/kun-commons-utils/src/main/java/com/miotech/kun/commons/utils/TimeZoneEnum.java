package com.miotech.kun.commons.utils;

public enum TimeZoneEnum {

    UTC("UTC"),
    ACT("Australia/Darwin"),
    AET("Australia/Sydney"),
    AGT("America/Argentina/Buenos_Aires"),
    ART("Africa/Cairo"),
    AST("America/Anchorage"),
    BET("America/Sao_Paulo"),
    BST("Asia/Dhaka"),
    CAT("Africa/Harare"),
    CNT("America/St_Johns"),
    CST("America/Chicago"),
    CTT("Asia/Shanghai"),
    EAT("Africa/Addis_Ababa"),
    ECT("Europe/Paris"),
    IET("America/Indiana/Indianapolis"),
    IST("Asia/Kolkata"),
    JST("Asia/Tokyo"),
    MIT("Pacific/Apia"),
    NET("Asia/Yerevan"),
    NST("Pacific/Auckland"),
    PLT("Asia/Karachi"),
    PNT("America/Phoenix"),
    PRT("America/Puerto_Rico"),
    PST("America/Los_Angeles"),
    SST("Pacific/Guadalcanal"),
    VST("Asia/Ho_Chi_Minh"),
    EST("-05:00"),
    MST("-07:00"),
    HST("-10:00");

    private String zoneId;

    TimeZoneEnum(String zoneId){
        this.zoneId = zoneId;
    }

    public String getZoneId() {
        return zoneId;
    }
}
