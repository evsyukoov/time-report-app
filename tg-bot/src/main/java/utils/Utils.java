package utils;

import exceptions.DateAfterTodayException;
import exceptions.TooLongIntervalException;
import exceptions.ValidationException;
import messages.Message;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    public static void validateDepartment(String text) throws ValidationException {
        if (!Message.departments.contains(text))
            throw new ValidationException(Message.ERROR_DEPARTMENT);
    }

    public static void validateFio(String text) throws ValidationException {
        String[] fio = text.split("\\s+");
        if (isBlank(fio)) {
            // наверно нереальный кейс, но пусть будет
            throw new ValidationException(Message.ERROR_EMPTY_FIO);
        }
        if (fio.length < 2) {
            throw new ValidationException(Message.ERROR_INCORRECT_FIO);
        }
    }

    public static <T> boolean isEmpty(Collection<T> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isBlank(String[] arr) {
        return arr == null || arr.length == 0;
    }

    public static String generateResultMessage(String msg1, String msg2) {
        return String.format("%s\n%s", msg1, msg2);
    }

    private static Properties getProperties(String path) {
        Properties properties = null;
        try (InputStream inputStream = new FileInputStream(path);
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
             properties = new Properties();
             properties.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static List<String> getMessagesFromProps(String path) {
        Properties props = Utils.getProperties(path);
        return  props
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(s -> ((String) s.getKey())))
                .map(Map.Entry::getValue)
                .map(String.class::cast)
                .map(Message.EMPTY_SYMBOL::concat)
                .collect(Collectors.toList());
    }

    public static List<Long> getUidsFromProps(String path) {
        Properties props = Utils.getProperties(path);
        return  props
                .values()
                .stream()
                .map(String.class::cast)
                .map(Long::parseLong)
                .sorted()
                .collect(Collectors.toList());
    }

    public static LocalDateTime parseDate(String dateText) throws ParseException, DateAfterTodayException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        //  флаг проверяет дату на наличие в календаре
        dateFormat.setLenient(false);
        Date date = dateFormat.parse(dateText);
        if (date.after(new Date())) {
            throw new DateAfterTodayException();
        }
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .plusHours(3);
    }

    public static Date[] parseVacationsDate(String dates) throws Exception {
        String[] dateArr = dates.split("\\s+");
        if (dateArr.length != 2) {
            throw new ValidationException("Неправильный разделитель начала и конца отпуска");
        }
        Date result[] = new Date[2];
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        dateFormat.setLenient(false);
        int i = 0;
        for (String s : dateArr) {
            result[i++] = dateFormat.parse(s);
        }
        if (!result[1].after(result[0])) {
            throw new DateAfterTodayException();
        }
        if (ChronoUnit.DAYS.between(DateTimeUtils.toLocalDate(result[0]), DateTimeUtils.toLocalDate(result[1])) > 30) {
            throw new TooLongIntervalException();
        }
        return result;
    }

    public static String firstLetterToUpperCase(String s) {
        if (s != null && !s.isEmpty()) {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
        return s;
    }

}
