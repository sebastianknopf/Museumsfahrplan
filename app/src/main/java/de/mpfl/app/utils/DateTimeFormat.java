package de.mpfl.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateTimeFormat {

    public final static String DDMMYYYY = "dd.MM.yyyy";
    public final static String YYYYMMDD = "yyyyMMdd";
    public final static String DDMMYYYY_HHMM = "dd.MM.yyyy HH:mm";
    public final static String HHMM = "HH:mm";
    public final static String HHMMSS = "HH:mm:ss";

    private Date internalDate = null;

    public static DateTimeFormat from(Date date) {
        return new DateTimeFormat(date);
    }

    public static DateTimeFormat from(String input, String format) {
        DateTimeFormat result = null;

        try {
            if(format.equals(HHMM) || format.equals(HHMMSS)) {
                String currentDate = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
                Date sourceDate = new SimpleDateFormat("dd.MM.yyyy " + format).parse(currentDate + " " + input);
                result = new DateTimeFormat(sourceDate);
            } else {
                Date sourceDate = new SimpleDateFormat(format).parse(input);
                result = new DateTimeFormat(sourceDate);
            }
        } catch(ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    public DateTimeFormat(Date source) {
        this.internalDate = source;
    }

    public String to(String format) {
        return new SimpleDateFormat(format).format(this.internalDate);
    }

}
