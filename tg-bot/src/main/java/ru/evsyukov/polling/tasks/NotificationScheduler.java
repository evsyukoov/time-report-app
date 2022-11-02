package ru.evsyukov.polling.tasks;

import ru.evsyukov.app.data.repository.ReportDayRepository;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.ReportingBot;
import lombok.extern.slf4j.Slf4j;
import ru.evsyukov.polling.data.BotDataService;
import ru.evsyukov.polling.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.polling.properties.ButtonsProperties;
import ru.evsyukov.polling.utils.DateTimeUtils;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.polling.utils.Utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@EnableScheduling
@Component
@Slf4j
public class NotificationScheduler {

    private final ReportingBot bot;

    private final BotDataService botDataService;

    private final static int PERIOD = 30 * 1000;

    private final static int DEPTH = 30;

    private final static DateTimeFormatter localDateFormatter;

    private final ButtonsProperties buttonsProperties;

    static {
        localDateFormatter = DateTimeFormatter.ofPattern("dd.MM");
    }

    @Autowired
    public NotificationScheduler(ReportingBot bot,
                                 BotDataService botDataService, ButtonsProperties buttonsProperties) {
        this.bot = bot;
        this.botDataService = botDataService;
        this.buttonsProperties = buttonsProperties;
    }

    @Scheduled(fixedRate = PERIOD)
    public void notificate() {
        List<Client> clients = botDataService.getNotificationClients();
        for (Client client : clients) {
            log.info("Time to send notification for client: {}", client);
            botDataService.incrementNotificationTime(client);

            DayOfWeek dayOfWeek = DayOfWeek.from(LocalDateTime.now());
            if (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY || client.isOnVacation()) {
                continue;
            }
            SendMessage sm = new SendMessage();
            sm.setChatId(String.valueOf(client.getUid()));
            if (client.getState() == State.MENU_CHOICE) {
                SendHelper.setInlineKeyboard(sm,
                        buttonsProperties.getActionsMenu(), null, 3);
            }
            String extraNotification = generateDatesIntervalsMessage(botDataService
                    .findFullReportDaysInterval(client.getUid(), DEPTH));
            String resultMessage = Message.NOTIFICATION + (extraNotification != null ?
                    String.format("\nТакже за последний месяц вы не отчитывались за следующие даты:\n %s", extraNotification) : "");
            sm.setText(resultMessage);
            try {
                bot.execute(sm);
            } catch (TelegramApiException e) {
                log.error("Error send message to client {}, err: {}", client, e);
            }
            log.info("Succesfully send notification message {} to client {}", resultMessage, client);
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
