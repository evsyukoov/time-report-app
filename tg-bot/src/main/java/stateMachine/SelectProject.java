package stateMachine;

import bot.BotContext;
import handlers.MainCommandsHandler;
import hibernate.access.ClientDao;
import hibernate.access.NotificationDao;
import hibernate.access.ProjectsDao;
import hibernate.access.ReportDaysDao;
import hibernate.entities.Client;
import messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import utils.SendHelper;
import utils.Utils;

import java.time.LocalDateTime;
import java.util.Collections;

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
        if ((sm = handler.handleBackButton()) != null
                || (sm = handler.handleProjectsChoice()) != null) {
            ClientDao.updateState(context.getClient(), State.MENU_CHOICE.ordinal());
            ReportDaysDao.saveOrUpdate(context.getClient(), context.getMessage());
            NotificationDao.updateFireTime(context.getClient().getUid());
            ClientDao.clearClient(context.getClient());
            question();
        }
    }
}
