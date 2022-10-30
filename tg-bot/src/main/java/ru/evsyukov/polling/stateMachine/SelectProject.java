package ru.evsyukov.polling.stateMachine;

import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.hibernate.access.ClientDao;
import ru.evsyukov.polling.hibernate.access.NotificationDao;
import ru.evsyukov.polling.hibernate.access.ReportDaysDao;
import ru.evsyukov.polling.hibernate.entities.Client;
import ru.evsyukov.polling.messages.Message;
import org.springframework.stereotype.Service;

@Service
public class SelectProject extends AbstractBotState {

    public SelectProject(BotContext context) {
        super(context);
    }

    @Override
    public void handleMessage() {
        Client client = context.getClient();
        MainCommandsHandler handler;
        // приходим на этот стейт с разных мест, по наличию даты понимаем откуда пришли
        if (client.getDateTime() == null) {
            handler = new MainCommandsHandler(context,
                    State.CHOOSE_DAY, Message.CHOOSE_REPORT_TYPE);
        } else {
            handler = new MainCommandsHandler(context,
                    State.PARSE_DATE, Message.SELECT_DATE);
        }
        if ((sm = handler.handleBackButton()) != null) {
            question();
        } else if ((sm = handler.handleProjectsChoice()) != null) {
            ClientDao.updateState(context.getClient(), State.MENU_CHOICE.ordinal());
            ReportDaysDao.saveOrUpdate(context.getClient(), context.getMessage());
            NotificationDao.updateFireTime(context.getClient().getUid());
            ClientDao.clearClient(context.getClient());
            question();
        }
    }
}
