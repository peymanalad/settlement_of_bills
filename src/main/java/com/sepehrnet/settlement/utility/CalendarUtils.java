package com.sepehrnet.settlement.utility;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import com.sepehrnet.settlement.enums.GregorianCalendarLocale;
import com.sepehrnet.settlement.enums.PersianCalendarLocale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public final class CalendarUtils {

    private static final Logger logger = LoggerFactory.getLogger(CalendarUtils.class);
    private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(GlobalUtils.CALENDAR_DATE_TIME_PATTERN);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HHmmssSSS");
    private static String calendarLocale;

    private CalendarUtils() {
    }

    @Autowired
    private CalendarUtils(@Value("${cfg.calendar.locale}") String calendarLocale) {
        CalendarUtils.calendarLocale = calendarLocale;
        logger.debug("Calendar Locale: " + CalendarUtils.calendarLocale);
    }

    public static long getUnixTimestamp(Date date) {
        return date.getTime() / 1000;
    }

    public static long getUnixTimestamp(Calendar calendar) {
        return calendar.getTime().getTime() / 1000;
    }

    public static String getDateTime(Date date) {
        return dateTimeFormatter.format(date);
    }

    public static String getDate(Date date) {
        return dateFormatter.format(date);
    }

    public static String getTime(Date date) {
        return timeFormatter.format(date);
    }

    private static ULocale getPersianCalendarULocale() {
        if (calendarLocale.equalsIgnoreCase(GlobalUtils.EN)) {
            return new ULocale(GlobalUtils.CALENDAR_PERSIAN_TYPE_WITH_LOCALE_EN_US);
        } else if (calendarLocale.equalsIgnoreCase(GlobalUtils.FA)) {
            return new ULocale(GlobalUtils.CALENDAR_PERSIAN_TYPE_WITH_LOCALE_FA_IR);
        }
        return new ULocale(GlobalUtils.CALENDAR_PERSIAN_TYPE_WITH_LOCALE_EN_US);
    }

    private static ULocale getGregorianCalendarULocale() {
        if (calendarLocale.equalsIgnoreCase(GlobalUtils.EN)) {
            return new ULocale(GlobalUtils.CALENDAR_GREGORIAN_TYPE_WITH_LOCALE_EN_US);
        } else if (calendarLocale.equalsIgnoreCase(GlobalUtils.FA)) {
            return new ULocale(GlobalUtils.CALENDAR_GREGORIAN_TYPE_WITH_LOCALE_FA_IR);
        }
        return new ULocale(GlobalUtils.CALENDAR_GREGORIAN_TYPE_WITH_LOCALE_EN_US);
    }

    private static String getCurrentGregorianDateTimeImpl(GregorianCalendarLocale calendarLocale) {
        ULocale locale;
        if (calendarLocale == null) {
            locale = getGregorianCalendarULocale();
        } else {
            locale = new ULocale(calendarLocale.getValue());
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(GlobalUtils.CALENDAR_DATE_TIME_PATTERN, locale);
        return simpleDateFormat.format(calendar);
    }

    private static String getCurrentGregorianDateTimeImpl(GregorianCalendarLocale calendarLocale, String pattern) {
        ULocale locale;
        if (calendarLocale == null) {
            locale = getGregorianCalendarULocale();
        } else {
            locale = new ULocale(calendarLocale.getValue());
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat;
        if (pattern != null && pattern.length() > 0) {
            simpleDateFormat = new SimpleDateFormat(pattern, locale);
        } else {
            simpleDateFormat = new SimpleDateFormat(GlobalUtils.CALENDAR_DATE_TIME_PATTERN, locale);
        }
        return simpleDateFormat.format(calendar);
    }

    private static String getCurrentGregorianDateImpl(GregorianCalendarLocale calendarLocale) {
        ULocale locale;
        if (calendarLocale == null) {
            locale = getGregorianCalendarULocale();
        } else {
            locale = new ULocale(calendarLocale.getValue());
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(GlobalUtils.CALENDAR_DATE_PATTERN, locale);
        return simpleDateFormat.format(calendar);
    }

    private static String getCurrentPersianDateTimeImpl(PersianCalendarLocale calendarLocale, String pattern) {
        ULocale locale;
        if (calendarLocale == null) {
            locale = getPersianCalendarULocale();
        } else {
            locale = new ULocale(calendarLocale.getValue());
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat;
        if (pattern != null && pattern.length() > 0) {
            simpleDateFormat = new SimpleDateFormat(pattern, locale);
        } else {
            simpleDateFormat = new SimpleDateFormat(GlobalUtils.CALENDAR_DATE_TIME_PATTERN, locale);
        }
        return simpleDateFormat.format(calendar);
    }

    private static String getCurrentPersianDateTimeImpl(PersianCalendarLocale calendarLocale, String pattern, Calendar calendar) {
        ULocale locale;
        if (calendarLocale == null) {
            locale = getPersianCalendarULocale();
        } else {
            locale = new ULocale(calendarLocale.getValue());
        }
        SimpleDateFormat simpleDateFormat;
        if (pattern != null && pattern.length() > 0) {
            simpleDateFormat = new SimpleDateFormat(pattern, locale);
        } else {
            simpleDateFormat = new SimpleDateFormat(GlobalUtils.CALENDAR_DATE_TIME_PATTERN, locale);
        }
        return simpleDateFormat.format(calendar);
    }

    private static String getCurrentPersianDateImpl(PersianCalendarLocale calendarLocale) {
        ULocale locale;
        if (calendarLocale == null) {
            locale = getPersianCalendarULocale();
        } else {
            locale = new ULocale(calendarLocale.getValue());
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(GlobalUtils.CALENDAR_DATE_PATTERN, locale);
        return simpleDateFormat.format(calendar);
    }

    public static String getCurrentGregorianDateTime() {
        return getCurrentGregorianDateTimeImpl(null);
    }

    public static String getCurrentGregorianDateTime(GregorianCalendarLocale calendarLocale) {
        return getCurrentGregorianDateTimeImpl(calendarLocale);
    }

    public static String getCurrentGregorianDateTime(String pattern) {
        return getCurrentGregorianDateTimeImpl(null, pattern);
    }

    public static String getCurrentGregorianDate() {
        return getCurrentGregorianDateImpl(null);
    }

    public static String getCurrentGregorianDate(GregorianCalendarLocale calendarLocale) {
        return getCurrentGregorianDateImpl(calendarLocale);
    }

    public static String getCurrentPersianDateTime() {
        return getCurrentPersianDateTimeImpl(null, "");
    }

    public static String getCurrentPersianDateTime(String pattern) {
        return getCurrentPersianDateTimeImpl(null, pattern);
    }

    public static String getCurrentPersianDateTime(String pattern, Calendar calendar) {
        return getCurrentPersianDateTimeImpl(null, pattern, calendar);
    }

    public static String getCurrentPersianDateTime(PersianCalendarLocale calendarLocale) {
        return getCurrentPersianDateTimeImpl(calendarLocale, "");
    }

    public static String getCurrentPersianDateTime(PersianCalendarLocale calendarLocale, String pattern) {
        return getCurrentPersianDateTimeImpl(calendarLocale, pattern);
    }

    public static String getCurrentPersianDate() {
        return getCurrentPersianDateImpl(null);
    }

    public static String getCurrentPersianDate(PersianCalendarLocale calendarLocale) {
        return getCurrentPersianDateImpl(calendarLocale);
    }

    /**
     * @param month is zero based. (e.g. Farvardin = 0, Ordibehesht = 1, etc.)
     */
    private static long fromPersianDate(int year, int month, int day, int hour, int minutes, int seconds) {
        Calendar persianCalendar = Calendar.getInstance(getPersianCalendarULocale());
        persianCalendar.clear();
        persianCalendar.set(year, month, day, hour, minutes, seconds);
        return persianCalendar.getTimeInMillis();
    }

    /**
     * @param month is zero based. (e.g. Farvardin = 0, Ordibehesht = 1, etc.)
     */
    private static Date fromPersianDateToDate(int year, int month, int day, int hour, int minutes, int seconds) {
        return new Date(fromPersianDate(year, month - 1, day, hour, minutes, seconds));
    }

    public static Date convertPersianStringToDate(String date) {
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4, 6));
        int day = Integer.parseInt(date.substring(6));
        return fromPersianDateToDate(year, month, day, 0, 0, 0);
    }

}
