package tasks;

import bot.ReportingBot;
import hibernate.access.NotificationDao;
import hibernate.access.ReportDaysDao;
import hibernate.entities.Client;
import messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import stateMachine.State;
import utils.DateTimeUtils;
import utils.SendHelper;
import utils.Utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MessageNotificator {

    ReportingBot bot;

    private final static int PERIOD = 30 * 1000;

    private final static int DEPTH = 30;

    private final static DateTimeFormatter localDateFormatter;

    static  {
        localDateFormatter = DateTimeFormatter.ofPattern("dd.MM");
    }

    public MessageNotificator(ReportingBot bot) {
        this.bot = bot;
    }

    public void notificate() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (Client client : NotificationDao.getClients(LocalDateTime.now().plusHours(3))) {
                    DayOfWeek dayOfWeek = DayOfWeek.from(LocalDateTime.now());
                    if (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY || client.isOnVacation()) {
                        continue;
                    }
                    SendMessage sm = new SendMessage();
                    sm.setChatId(String.valueOf(client.getUid()));
                    if (client.getState() == State.MENU_CHOICE.ordinal()) {
                        SendHelper.setInlineKeyboard(sm,
                                Message.actionsMenu, null, 3);
                    }
                    List<LocalDate> dates = ReportDaysDao.getClientsReportDates(client, DEPTH);
                    String extraNotification = generateDatesIntervalsMessage(dates);
                    String resultMessage = Message.NOTIFICATION + (extraNotification != null ?
                            String.format("\nТакже за последний месяц вы не отчитывались за следующие даты:\n %s", extraNotification) : "");
                    sm.setText(resultMessage);
                    try {
                        bot.execute(sm);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, PERIOD);
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

    public void updateMessage() {
        List<Long> uids = Utils.getUidsFromProps("./src/main/resources/property/update.properties");
        for (long uid : uids) {
            SendMessage sm = new SendMessage();
            sm.setChatId(String.valueOf(uid));
            sm.setText("Добавлены справочники, оповещения. Для старта введите /start");
            try {
                bot.execute(sm);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
