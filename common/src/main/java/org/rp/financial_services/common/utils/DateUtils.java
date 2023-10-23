package org.rp.financial_services.common.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private DateUtils(){}


    /**
     * @param date Date as starting point for the calculation, cannot be null
     * @return The previous working day
     */
    public static LocalDate adjustDate(LocalDate date) {
        if (date.equals(LocalDate.now()))
            return adjustDate(date.minus(1, ChronoUnit.DAYS));

        DayOfWeek dayOfWeek = DayOfWeek.of(date.get(ChronoField.DAY_OF_WEEK));
        return switch (dayOfWeek) {
            case SATURDAY -> date.minus(1, ChronoUnit.DAYS);
            case SUNDAY -> date.minus(2, ChronoUnit.DAYS);
            default -> date;
        };

    }

    public static Calendar convertLocalDateToCalendar(LocalDate localDate) {
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}
