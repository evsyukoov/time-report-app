package ru.evsyukov.polling.tasks;

import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.repository.ClientRepository;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.ReportingBot;
import lombok.extern.slf4j.Slf4j;
import ru.evsyukov.polling.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evsyukov.polling.properties.ButtonsProperties;
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

    private final ReportingBot bot;

    private final ClientRepository clientRepository;

    private final ButtonsProperties buttonsProperties;

    @Autowired
    public VacationScheduler(ReportingBot bot,
                             ClientRepository clientRepository,
                             ButtonsProperties buttonsProperties) {
        this.bot = bot;
        this.clientRepository = clientRepository;
        this.buttonsProperties = buttonsProperties;
    }

    @Scheduled(cron = "0 00 01 * * *")
    public void schedule() {
        log.info("Start vacation scheduler...");
        Date date = Date.from(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant(ZoneOffset.ofHours(3)));
        List<Client> clients = clientRepository.getAllByStartVacationIsNotNullAndEndVacationIsNotNull();
        for (Client client : clients) {
            log.info("Clients on vacation: {}", clients);
            Date current = new Date();
            SendMessage sm = null;
            if (!client.isOnVacation()) {
                if (DateTimeUtils.isBetween(client.getStartVacation(), client.getEndVacation(), current)) {
                    sm = new SendMessage();
                    updateVacationInfo(client);
                    sm.setText(Message.YOU_ARE_IN_VACATION_MODE);
                    sm.setChatId(String.valueOf(client.getUid()));
                    SendHelper.setInlineKeyboard(sm, Collections.emptyList(), Message.CLEAR_VACATION, 1);
                }
                //случай для клиентов проставивших отпуск задним числом
                else if (DateTimeUtils.greaterOrEquals(date, client.getEndVacation())) {
                    //updateReportDaysInfo(client);
                    updateClientVacationInfo(client, client.getState(), null, null, false);
                }
            } else {
                if (DateTimeUtils.greaterOrEquals(date, client.getEndVacation())) {
                    sm = new SendMessage();
                    updateReportDaysInfo(client);
                    updateClientVacationInfo(client, State.MENU_CHOICE, null, null, false);
                    sm.setText(Message.YOUR_VACATION_IS_OVER);
                    sm.setChatId(String.valueOf(client.getUid()));
                    SendHelper.setInlineKeyboard(sm, buttonsProperties.getActionsMenu(), null, 3);
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

    private void updateVacationInfo(Client client) {
        client.setOnVacation(true);
        clientRepository.save(client);
        log.info("Update client vacation info {}", client);
    }

    public void updateClientVacationInfo(Client client, State state, Date start, Date end, boolean onVacation) {
        client.setOnVacation(onVacation);
        client.setStartVacation(start);
        client.setEndVacation(end);
        client.setState(state);
        clientRepository.save(client);
        log.info("End client vacation {}", client);
    }

    private void updateReportDaysInfo(Client client) {
        Date start = client.getStartVacation();
        while (start.before(client.getEndVacation())) {
            //client.setProject("-");
            //TODO проставлять отпуск по выходу
            start = DateTimeUtils.getNextDay(start);
        }
    }
}
