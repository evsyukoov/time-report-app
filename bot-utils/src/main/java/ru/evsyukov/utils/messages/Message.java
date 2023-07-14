package ru.evsyukov.utils.messages;

public class Message {

    public static final String EMPTY_SYMBOL = "🔳 ";

    public static final String CONFIRM_SYMBOL = "✅ ";

    public static final String NO_PROJECT = "🚫 Нет такого проекта";

    public static final String NO_REPORT = "🚫 Вы не внесли ни одного проекта";

    public static final String PROJECT_ALREADY_EXISTS = "🚫 Вы уже вносили этот проект";

    public static final String PROJECT_MAX_COUNT_ATTEMPT = "🚫 Вы пытаетесь внести 9-ый объект в ведомость отчетности. Закончите отчет";

    public static final String EXTRA_CONFIRM_SYMBOL = "📌 ";

    public static final String APPROVE = "📝 Подтвердить";

    public static final String DELIMETR = "&";

    public static final String MENU = "Выберите действие";

    public static final String DISCHARGE_ACTION_ENABLED  = "Я больше не буду беспокоить тебя своими оповещениями";

    public static final String APPROVE_NOTIFICATION_ENABLED = "Теперь, если ты забыл отчитаться до %s, тебе придет оповещение";

    public static final String DISCHARGE_NOTIFICATION = "Отменить оповещения";

    public static final String APPROVE_NOTIFICATION = "Установить оповещения";

    public static final String NOTIFICATION_CHOICE = "Выберите время оповещений по МСК";

    public static final String ERROR_SEND_MESSAGE = "Ошибка при отправке сообщения пользователю";

    public static final String STOP = "/stop";

    public static final String START = "/start";

    public static final String BACK = " ⬆️️ Назад";

    public static final String REGISTER_DEPARTMENT = "Выберите отдел";

    public static final String REGISTER_NAME = "Выберите свое имя";

    public static final String CHOOSE_DAY = "Выберите день";

    public static final String ERROR_EMPTY_FIO = "Некорректное ФИО";

    public static final String ERROR_DEPARTMENT = "Неизвестный отдел";

    public static final String ERROR_INCORRECT_FIO = "Нужно ввести фамилию и имя";

    public static final String ERROR_DATE_FORMAT = "Некорректный формат даты";

    public static final String ERROR_DATE_AFTER_TODAY = "Слишком рано отчитываться за эту дату";

    public static final String REGISTER_IS_FINISHED = " ✅ Регистрация успешно завершена";

    public static final String CHOOSE_REPORT_TYPE = "Выберите за какой день отчитываться";

    public static final String SELECT_PROJECT = "Введите объекты";

    public static final String INFO_ABOUT_JOB = "Напишите краткий отчет о проделанной работе";

    public static final String FINISH = " ✅ Принято!";

    public static final String SELECT_DATE = "Введите день в формате dd.mm.yyyy";

    public static final String NOTIFICATION = "Не забудьте отчитаться за сегодняшний день";

    public static final String VACATION = "Введите дату начала и окончания отпуска(дата первого рабочего дня) через пробел в формате dd.mm.yyyy\n" +
            "Пример: 1.1.2021  15.1.2021";

    public static final String VACATION_DATES_VALIDATION_ERROR = "Неверный разделитель начала и конца отпуска";

    public static final String VACATION_ERROR_END_DATE = "Дата конца отпуска должна быть после даты начала";

    public static final String YOU_ARE_IN_VACATION_MODE = "C этого момента вы находитесь в отпуске и вам недоступны какие-либо действия\n" +
            "Вы получите уведомление по окончании отпуска";

    public static final String FIX_VACATION_DAYS = "Исправить даты отпуска";

    public static final String VACATION_DATES_SET = "Даты отпуска установлены";

    public static final String CLEAR_VACATION = "🚫 Отменить отпуск";

    public static final String VACATION_IS_CLEAR = "Отпуск отменен!";

    public static final String NOT_ALLOWED_INIT_VACATION = "Вы уже зарегистрировали отпуск, для задания других дат его нужно сначала отменить";

    public static final String YOU_ARE_IN_VACATION = "Сейчас вы находитесь в отпуске";

    public static final String YOUR_VACATION_IS_OVER = "Ваш отпуск закончился";

    public static final String VACATION_TO_SAVE = "Отпуск";

    public static final String TOO_LONG_INTERVAL = "Выбран период отпуска, превышающий 30 дней";

    public static String NAME_CHOSEN = "Вы собираетесь зарегистрироваться в системе под именем %s\nУбедитесь, что вы не ошиблись";

    public static String WRONG_NAME_CHOSEN = "Пользователь под таким именем уже зарегистрирован в системе!\nВыберите другое имя";

    public static String INLINE_QUERY_PROMPT = "Начните вводить название объекта";

    public static final String INLINE_BUTTON_NAME = "Ввод";

    public static final String INLINE_BUTTON_PROMPT = "Введите до 8 объектов которыми занимались";

    public static final String APPROVE_INLINE = "📝 Закончить отчет";

    public static final String RESTART_BUTTON = "🔄 Перезапуск";

    public static final String PREVIOUS_DAY = "📝 Повторить предыдущий";

}
