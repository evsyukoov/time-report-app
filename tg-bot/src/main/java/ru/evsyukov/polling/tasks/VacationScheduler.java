package ru.evsyukov.polling.tasks;

import ru.evsyukov.polling.bot.ReportingBot;
import ru.evsyukov.polling.hibernate.access.ClientDao;
import ru.evsyukov.polling.hibernate.access.ReportDaysDao;
import ru.evsyukov.polling.hibernate.entities.Client;
import lombok.extern.slf4j.Slf4j;
import ru.evsyukov.polling.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evsyukov.polling.stateMachine.State;
import ru.evsyukov.polling.utils.DateTimeUtils;
import ru.evsyukov.polling.utils.SendHelper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@EnableScheduling
@Component
@Slf4j
public class VacationScheduler {

    ReportingBot bot;

    @Autowired
    public VacationScheduler(ReportingBot bot) {
        this.bot = bot;
    }

    @Scheduled(cron = "0 00 01 * * *")
    public void schedule() {
        log.info("Start vacation scheduler...");
        Date date = Date.from(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant(ZoneOffset.ofHours(3)));
        List<Client> clients = ClientDao.getClientsWithVacations();
        for (Client client : clients) {
            log.info("Clients on vacation: {}", clients);
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
                    log.error("Error when send message to client {}", client);
                }
            }
        }
    }

    private void updateReportDaysInfo(Client client) {
        Date start = client.getStartVacation();
        while (start.before(client.getEndVacation())) {
            ReportDaysDao.saveOrUpdate(client, start);
            start = DateTimeUtils.getNextDay(start);
        }
    }
}
