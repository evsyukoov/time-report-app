package utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeUtils {

    public static java.sql.Date convertDate(LocalDateTime dateTime) {
        return java.sql.Date.valueOf(dateTime.toLocalDate());
    }

    public static java.sql.Date convertDate(Date date) {
        return new java.sql.Date(date.getTime());
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date fromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static boolean isBetween(Date start, Date end, Date current) {
        LocalDate startLocal = toLocalDate(start);
        LocalDate endLocal = toLocalDate(end);
        LocalDate currLocal = toLocalDate(current);

        return (currLocal.isAfter(startLocal) || currLocal.equals(startLocal)) &&
                (currLocal.isBefore(endLocal) || currLocal.equals(endLocal));
    }

    public static boolean isBetweenStrict(Date start, Date end, Date current) {
        LocalDate startLocal = toLocalDate(start);
        LocalDate endLocal = toLocalDate(end);
        LocalDate currLocal = toLocalDate(current);

        return (currLocal.isAfter(startLocal) || currLocal.equals(startLocal)) &&
                (currLocal.isBefore(endLocal));
    }


    public static boolean isGreater(Date date1, Date date2) {
        LocalDate local1 = toLocalDate(date1);
        LocalDate local2 = toLocalDate(date2);

        return local2.isAfter(local1);
    }

    public static boolean isWeekend(Date date) {
        DayOfWeek dayOfWeek = DayOfWeek.from(DateTimeUtils.toLocalDate(date));
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = DayOfWeek.from(date);
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    //применять для дат из БД, со временем 00:00:00
    //TODO заменить все даты на LocalDate во избежание ошибок с equals!
    public static boolean greaterOrEquals(Date current, Date border) {
        return current.after(border) || current.equals(border);
    }

    //TODO еще одна причина для полного перехода на LocalDate
    public static Date getNextDay(Date date) {
        return new Date(date.getTime() + 24 * 60 * 60 * 1000);
    }
}
