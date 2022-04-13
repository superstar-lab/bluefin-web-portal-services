package com.mcmcg.ico.bluefin.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {

    @Test
    void datetimeToUTC() {
        String timeZone = "America/Los_Angeles";
        String dateTime = "2022/04/01 12:00:00";
        String return1 = DateTimeUtil.datetimeToUTC(dateTime, timeZone);
        assertEquals("2022/04/01 19:00:00", return1);
    }

    @Test
    void datetimeToUTC_OmitLeadingZeros() {
        String timeZone = "Europe/Moscow";
        String dateTime = "2022/4/1 12:00:00";
        String return1 = DateTimeUtil.datetimeToUTC(dateTime, timeZone);
        assertEquals("2022/04/01 19:00:00", return1);
    }

    @Test
    void datetimeToUTC_FromGMT_add13() {
        String timeZone = "Pacific/Tongatapu";
        String dateTime = "2022/4/1 12:55:15";
        String return1 = DateTimeUtil.datetimeToUTC(dateTime, timeZone);
        assertEquals("2022/03/31 23:55:15", return1);
    }
}