package tasks;

import bot.ReportingBot;
import hibernate.access.ClientDao;
import hibernate.access.ReportDaysDao;
import hibernate.entities.Client;
import messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import stateMachine.State;
import utils.DateTimeUtils;
import utils.SendHelper;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class VacationThread {

    ReportingBot bot;

    private final static int PERIOD =  24 * 60 * 60 * 1000;

    public VacationThread(ReportingBot bot) {
        this.bot = bot;
    }

    public void doJob() {
        Timer timer = new Timer();
        Date date = Date.from(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant(ZoneOffset.ofHours(3)));
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                List<Client> clients = ClientDao.getClientsWithVacations();
                for (Client client : clients) {
                    Date current = new Date();
                    SendMessage sm = null;
                    if (!client.isOnVacation()) {
                        if (DateTimeUtils.isBetween(client.getStartVacation(), client.getEndVacation(), current)) {
                            sm = new SendMessage();
                            ClientDao.updateClientVacationInfo(client, true);
                            sm.setText(Message.YOU_ARE_IN_VACATION_MODE);
                            sm.setChatId(String.valueOf(client.getUid()));
                            SendHelper.setInlineKeyboard(sm, Collections.emptyList(), Message.CLEAR_VACATION, 1);
                        }
                        //случай для клиентов проставивших отпуск задним числом
                        else if (DateTimeUtils.greaterOrEquals(date, client.getEndVacation())) {
                            updateReportDaysInfo(client);
                            ClientDao.updateClientVacationInfo(client,
                                    client.getState(), null, null, false);
                        }
                    } else {
                        if (DateTimeUtils.greaterOrEquals(date, client.getEndVacation())) {
                            sm = new SendMessage();
                            updateReportDaysInfo(client);
                            ClientDao.updateClientVacationInfo(client,
                                    State.MENU_CHOICE.ordinal(), null, null, false);
                            sm.setText(Message.YOUR_VACATION_IS_OVER);
                            sm.setChatId(String.valueOf(client.getUid()));
                            SendHelper.setInlineKeyboard(sm, Message.actionsMenu, null, 3);
                        }
                    }
                    if (sm != null) {
                        try {
                            bot.execute(sm);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } , date, PERIOD);
    }

    private void updateReportDaysInfo(Client client) {
        Date start = client.getStartVacation();
        while (start.before(client.getEndVacation())) {
            ReportDaysDao.saveOrUpdate(client, start);
            start = DateTimeUtils.getNextDay(start);
        }
    }
}
