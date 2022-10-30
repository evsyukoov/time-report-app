package ru.evsyukov.polling.tasks;

import ru.evsyukov.app.data.entity.ReportDay;
import ru.evsyukov.app.data.repository.ReportDayRepository;
import ru.evsyukov.polling.bot.ReportingBot;
import lombok.extern.slf4j.Slf4j;
import ru.evsyukov.polling.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.entity.Notification;
import ru.evsyukov.app.data.repository.NotificationRepository;
import ru.evsyukov.polling.properties.ButtonsProperties;
import ru.evsyukov.polling.stateMachine.State;
import ru.evsyukov.polling.utils.DateTimeUtils;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.polling.utils.Utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@EnableScheduling
@Component
@Slf4j
public class NotificationScheduler {

    private final ReportingBot bot;

    private final NotificationRepository notificationRepository;

    private final ReportDayRepository reportDayRepository;

    private final static int PERIOD = 30 * 1000;

    private final static int DEPTH = 30;

    private final static DateTimeFormatter localDateFormatter;

    private final ButtonsProperties buttonsProperties;

    static {
        localDateFormatter = DateTimeFormatter.ofPattern("dd.MM");
    }

    @Autowired
    public NotificationScheduler(ReportingBot bot,
                                 NotificationRepository notificationRepository,
                                 ReportDayRepository reportDayRepository,
                                 ButtonsProperties buttonsProperties) {
        this.bot = bot;
        this.notificationRepository = notificationRepository;
        this.reportDayRepository = reportDayRepository;
        this.buttonsProperties = buttonsProperties;
    }

    @Scheduled(fixedRate = PERIOD)
    public void notificate() {
        List<Client> clients =
                notificationRepository.getAllByNextFireTimeBefore(LocalDateTime.now().plusHours(3))
                        .stream().map(Notification::getClient)
                        .collect(Collectors.toList());
        for (Client client : clients) {
            log.info("Time to send notification for client: {}", client);
            Notification notification = client.getNotification();
            notification.setNextFireTime(notification.getNextFireTime().plusDays(1));
            notificationRepository.save(notification);

            DayOfWeek dayOfWeek = DayOfWeek.from(LocalDateTime.now());
            if (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY || client.isOnVacation()) {
                continue;
            }
            SendMessage sm = new SendMessage();
            sm.setChatId(String.valueOf(client.getUid()));
            if (client.getState() == State.MENU_CHOICE.ordinal()) {
                SendHelper.setInlineKeyboard(sm,
                        buttonsProperties.getActionsMenu(), null, 3);
            }

            Date start = DateTimeUtils.fromLocalDate(LocalDate.now().minusDays(DEPTH));
            Date finished = DateTimeUtils.fromLocalDate(LocalDate.now().minusDays(1));
            List<ReportDay> reportDays = reportDayRepository.findReportDayByDateGreaterThanEqualAndDateLessThanEqualAndUidEquals(
                    start, finished, client.getUid());
            List<LocalDate> dates = reportDays.stream().map(ReportDay::getDate)
                    .map(DateTimeUtils::toLocalDate).collect(Collectors.toList());
            String extraNotification = generateDatesIntervalsMessage(dates);
            String resultMessage = Message.NOTIFICATION + (extraNotification != null ?
                    String.format("\nТакже за последний месяц вы не отчитывались за следующие даты:\n %s", extraNotification) : "");
            sm.setText(resultMessage);
            try {
                bot.execute(sm);
            } catch (TelegramApiException e) {
                log.error("Error send message to client {}, err: {}", client, e);
            }
            log.info("Succesfully send notification message to client {}", client);
        }
    }

    private List<LocalDate> getNonReportDays(List<LocalDate> reportDays) {
        List<LocalDate> allDatesPeriod = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(DEPTH);
        LocalDate end = LocalDate.now().minusDays(1);
        while (start.isBefore(end) || start.equals(end)) {
            allDatesPeriod.add(start);
            start = start.plusDays(1);
        }
        allDatesPeriod.removeAll(reportDays);
        allDatesPeriod.removeIf(DateTimeUtils::isWeekend);
        return allDatesPeriod;
    }

    private String generateDatesIntervalsMessage(List<LocalDate> reportDays) {
        List<LocalDate> finalMissingDates = getNonReportDays(reportDays);
        if (Utils.isEmpty(finalMissingDates)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(finalMissingDates.get(0).format(localDateFormatter));
        int j = 0;
        for (int i = 0; i < finalMissingDates.size() - 1; i++) {
            if (finalMissingDates.get(i).plusDays(1).equals(finalMissingDates.get(i + 1))) {
                j++;
            } else {
                if (j != 0) {
                    sb.append(" - ").append(finalMissingDates.get(i).format(localDateFormatter));
                }
                if (i != finalMissingDates.size() - 1) {
                    sb.append(";");
                    sb.append(finalMissingDates.get(i + 1).format(localDateFormatter));
                }
                j = 0;
            }
        }
        if (j != 0) {
            sb.append(" - ").append(finalMissingDates.get(finalMissingDates.size() - 1).format(localDateFormatter));
        }
        return sb.toString();
    }
}
