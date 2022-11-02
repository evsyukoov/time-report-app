package ru.evsyukov.polling.utils;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.exceptions.DateAfterTodayException;
import ru.evsyukov.polling.exceptions.TooLongIntervalException;
import ru.evsyukov.polling.exceptions.ValidationException;
import ru.evsyukov.polling.messages.Message;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Utils {

    public static State getState(int state) {
        return State.values()[state];
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

    public static boolean isCallBackMessage(Update update) {
        return update.getMessage() == null;
    }

    public static Chat getCurrentChat(Update update) {
        return isCallBackMessage(update) ? update.getCallbackQuery().getMessage().getChat() : update.getMessage().getChat();
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
