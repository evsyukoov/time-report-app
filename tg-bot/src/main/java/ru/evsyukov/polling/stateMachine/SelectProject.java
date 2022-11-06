package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.entity.Notification;
import ru.evsyukov.app.data.entity.Project;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.data.BotDataService;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import org.springframework.stereotype.Service;
import ru.evsyukov.utils.messages.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SelectProject implements BotState {

    private final BotDataService botDataService;

    private MainCommandsHandler mainHandler;

    private static final DateTimeFormatter dtf;

    static {
        dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    @Autowired
    public SelectProject(BotDataService botDataService,
                         MainCommandsHandler mainHandler) {
        this.botDataService = botDataService;
        this.mainHandler = mainHandler;
    }

    @Override
    public State getState() {
        return State.SELECT_PROJECT;
    }

    @Override
    public void handleMessage(BotContext context) {
        log.info("State {} with client {} start", getState().name(), context.getClient());
        Client client = context.getClient();
        SendMessage sm;
        // приходим на этот стейт с разных мест, по наличию даты понимаем откуда пришли
        if (client.getDateTime() == null) {
            log.info("Client {} want to report for today", context.getClient());
            sm = mainHandler.handleBackButton(context, Message.CHOOSE_REPORT_TYPE, State.CHOOSE_DAY);
        } else {
            log.info("Client {} want to report for day {}", context.getClient(), dtf.format(client.getDateTime()));
            sm = mainHandler.handleBackButton(context, Message.SELECT_DATE, State.PARSE_DATE);
        }
        if ((sm != null)) {
            question(sm, context);
        } else if ((sm = mainHandler.handleProjectsChoice(context)) != null) {
            botDataService.saveOrUpdateReportDays(client, getFinalProjects(client));
            updateClientNotification(client);
            botDataService.clearClient(client, State.MENU_CHOICE);
            question(sm, context);
        }
    }

    private String getFinalProjects(Client client) {
        Set<Project> projects = new HashSet<>();
        projects.add(botDataService.getMainProjectById(client));
        Set<Project> extraProjects = null;
        if (client.getExtraProjects() != null) {
            extraProjects = botDataService.getExtraProjectsFromIds(client.getExtraProjects());
        }
        if (extraProjects != null && !extraProjects.isEmpty()) {
            projects.addAll(extraProjects);
        }
        return projects.stream().map(Project::getProjectName)
                .collect(Collectors.joining(Message.DELIMETR));
    }

    private void updateClientNotification(Client client) {
        Notification notification = client.getNotification();
        if (notification == null) {
            log.info("Notification wasn't set by client {}", client);
            return;
        }
        LocalDateTime nextFireTime = notification.getNextFireTime();
        if (nextFireTime == null || nextFireTime.toLocalDate().
                isAfter(LocalDateTime.now().toLocalDate())) {
            log.info("Not nesseccary to update notification time to client {}", client);
            return;
        }
        botDataService.incrementNotificationTime(client);
    }
}
