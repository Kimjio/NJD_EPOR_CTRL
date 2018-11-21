package com.nojunjae.epor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class eventR {
    private String summary, description, startDateTime, endDateTime;

    public eventR(String summary, String description, String startDateTime, String endDateTime) {
        this.summary = summary;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public boolean isEmpty() {
        return summary == null
                && description == null
                && startDateTime == null
                && endDateTime == null;
    }

    public String toString() {
        if (isEmpty()) return "일정이 없습니다.";
        // 2018-06-09T15:30:00.000+09:00
        // yyyy-MM-dd'T'HH:mm:ss.SSSXXX
        SimpleDateFormat parseFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.KOREA);
        SimpleDateFormat dateFormatYear =
                new SimpleDateFormat("yyyy'년' M'월' d'일' H'시' mm'분'", Locale.KOREA);
        SimpleDateFormat dateFormat = new SimpleDateFormat("M'월' d'일' H'시' mm'분'", Locale.KOREA);
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        try {
            startDate.setTime(parseFormat.parse(getStartDateTime()));
            endDate.setTime(parseFormat.parse(getEndDateTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if ((Calendar.getInstance().get(Calendar.YEAR) != startDate.get(Calendar.YEAR))
                || (Calendar.getInstance().get(Calendar.YEAR) != startDate.get(Calendar.YEAR)))
            if (getDescription() != null)
                return dateFormatYear.format(startDate.getTime())
                        + "부터 "
                        + dateFormatYear.format(endDate.getTime())
                        + "까지 "
                        + getSummary()
                        + "가 있습니다."
                        + getDescription();
            else
                return dateFormatYear.format(startDate.getTime())
                        + "부터 "
                        + dateFormatYear.format(endDate.getTime())
                        + "까지 "
                        + getSummary()
                        + "가 있습니다.";
        else {
            if (getDescription() != null)
                return dateFormat.format(startDate.getTime())
                        + "부터 "
                        + dateFormat.format(endDate.getTime())
                        + "까지 "
                        + getSummary()
                        + "가 있습니다."
                        + getDescription();
            else
                return dateFormat.format(startDate.getTime())
                        + "부터 "
                        + dateFormat.format(endDate.getTime())
                        + "까지 "
                        + getSummary()
                        + "가 있습니다.";
        }
    }
}
