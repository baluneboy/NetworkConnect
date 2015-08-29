package com.example.android.common.devices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

// TODO need base utility project for utils like this one

/**
 * Created by pims on 7/13/15.
 */
public class MyDateUtils {

    public static void main(String[] args) {
        Calendar b = new GregorianCalendar();
        b.set(Calendar.HOUR_OF_DAY, 12);
        b.set(Calendar.MINUTE, 59);
        b.set(Calendar.SECOND, 13);


        Date d1 = b.getTime();
        b.add(Calendar.HOUR, 1);
        Date d2 = b.getTime();
        System.out.println("dateDiff is " + getDateDiff(d1, d2, TimeUnit.SECONDS));

        b.add(Calendar.SECOND, 15);
        showRounding(b);

        b.add(Calendar.SECOND, 29);
        showRounding(b);

        for(int i=51; i<62; i++){
            b.add(Calendar.MINUTE, i);
            showRounding(b);
        }

        System.out.println(getDateFromString("2015-07-16 17:29:33.456").getTime());
        System.out.println(getDateFromString(getCurrentTimeString()).getTime());
        System.out.println(getDateFromString(getCurrentTimeString()));

    }

    public static void showRounding(Calendar b) {
        System.out.println("-----------------------");
        System.out.println(b.getTime() + " is start point");
        System.out.println(toNextWholeHour(b).getTime() + " is next whole hour");
        System.out.println(toNextWholeMinute(b).getTime() + " is next whole minute");
/*        System.out.println(toNearestWholeMinute(b).getTime() + " is nearest whole minute");
        System.out.println(toNearestWholeHour(b).getTime() + " is nearest whole hour");*/
    }

    public static Date getDateFromString(String inputString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = sdf.parse(inputString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getCurrentTimeString() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    /**
     * Get a diff between two dates
     * @param date1 the older date
     * @param date2 the newer date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillis = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }

    static Calendar toNearestWholeMinute(Calendar d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d.getTime());

        if (c.get(Calendar.SECOND) >= 30)
            c.add(Calendar.MINUTE, 1);

        c.set(Calendar.SECOND, 0);

        return c;
    }

    static Calendar toNearestWholeHour(Calendar d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d.getTime());

        if (c.get(Calendar.MINUTE) >= 30)
            c.add(Calendar.HOUR, 1);

        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        return c;
    }

    static Calendar toNextWholeMinute(Calendar d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d.getTime());
        c.add(Calendar.MINUTE, 1);
        c.set(Calendar.SECOND, 0);
        return c;
    }

    static Calendar toNextWholeHour(Calendar d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d.getTime());
        c.add(Calendar.HOUR, 1);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c;
    }

}
