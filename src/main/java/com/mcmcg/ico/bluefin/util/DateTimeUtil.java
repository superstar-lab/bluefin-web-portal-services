package com.mcmcg.ico.bluefin.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static String datetimeToUTC(String datetime, String timeZone){
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").withZone(DateTimeZone.forID(timeZone));
        DateTime date = DateTime.parse(datetime, fmt);
        return fmt.withZone(DateTimeZone.forID("UTC")).print(date);
    }

    public static String datetimeToUTC(String datetime, String timeZone, String pattern){
        DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.forID(timeZone));
        DateTime date = DateTime.parse(datetime, fmt);
        return fmt.withZone(DateTimeZone.forID("UTC")).print(date);
    }
}
