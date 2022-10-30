package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.entity.Notification;
import ru.evsyukov.app.data.entity.Project;
import ru.evsyukov.app.data.entity.ReportDay;
import ru.evsyukov.app.data.repository.ClientRepository;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.app.data.repository.NotificationRepository;
import ru.evsyukov.app.data.repository.ProjectsRepository;
import ru.evsyukov.app.data.repository.ReportDayRepository;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.messages.Message;
import org.springframework.stereotype.Service;
import ru.evsyukov.polling.utils.DateTimeUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SelectProject implements BotState {

    private ClientRepository clientRepository;

    private NotificationRepository notificationRepository;

    private ReportDayRepository reportDayRepository;

    private EmployeeRepository employeeRepository;

    private ProjectsRepository projectsRepository;

    private MainCommandsHandler mainHandler;

    @Autowired
    public SelectProject(ClientRepository clientRepository,
                         NotificationRepository notificationRepository,
                         ReportDayRepository reportDayRepository,
                         EmployeeRepository employeeRepository,
                         ProjectsRepository projectsRepository,
                         MainCommandsHandler mainHandler) {
        this.clientRepository = clientRepository;
        this.notificationRepository = notificationRepository;
        this.reportDayRepository = reportDayRepository;
        this.employeeRepository = employeeRepository;
        this.projectsRepository = projectsRepository;
        this.mainHandler = mainHandler;
    }

    @Override
    public State getState() {
        return State.SELECT_PROJECT;
    }

    @Override
    public void handleMessage(BotContext context) {
        Client client = context.getClient();
        SendMessage sm;
        // приходим на этот стейт с разных мест, по наличию даты понимаем откуда пришли
        if (client.getDateTime() == null) {
            sm = mainHandler.handleBackButton(context, Message.CHOOSE_REPORT_TYPE, State.CHOOSE_DAY);
        } else {
            sm = mainHandler.handleBackButton(context, Message.SELECT_DATE, State.PARSE_DATE);
        }
        if ((sm != null)) {
            question(sm, context);
        } else if ((sm = mainHandler.handleProjectsChoice(context)) != null) {
            saveOrUpdateReportDays(client);
            updateClientNotification(client);
            clearClient(client, State.MENU_CHOICE);
            question(sm, context);
        }
    }

    private void saveOrUpdateReportDays(Client client) {
        Date reportDate = DateTimeUtils.fromLocalDate(client.getDateTime().toLocalDate());
        ReportDay reportDay = reportDayRepository.findReportDayByUidAndDate(client.getUid(), reportDate);
        if (reportDay == null) {
            reportDay = new ReportDay();
            reportDay.setEmployee(employeeRepository.getEmployeeByName(client.getName()));
            reportDay.setProjects(getFinalProjects(client));
            reportDay.setUid(client.getUid());
            // время ставим по МСК
            reportDay.setDate(reportDate);
            reportDayRepository.save(reportDay);
            log.info("Save report day for client id {} day {}", reportDay.getUid(), reportDate);
        } else {
            reportDay.setProjects(getFinalProjects(client));
            reportDayRepository.save(reportDay);
            log.info("Update report day for client id {} day {}", reportDay.getUid(), reportDate);
        }
    }

    private String getFinalProjects(Client client) {
        Set<Project> projects = new HashSet<>();
        projects.add(projectsRepository.getProjectById(Long.parseLong(client.getProject())));
        Set<Project> extraProjects = null;
        if (client.getExtraProjects() != null) {
            extraProjects = getProjectsFromIds(client.getExtraProjects());
        }
        if (extraProjects != null && !extraProjects.isEmpty()) {
            projects.addAll(extraProjects);
        }
        return projects.stream().map(Project::getProjectName)
                .collect(Collectors.joining(Message.DELIMETR));
    }

    private Set<Project> getProjectsFromIds(String extraProjects) {
        return Arrays.stream(extraProjects.split(Message.DELIMETR))
                .map(proj -> projectsRepository.getProjectById(
                        Long.parseLong(proj)))
                .collect(Collectors.toSet());
    }

    private void clearClient(Client client, State state) {
        client.setState(State.MENU_CHOICE.ordinal());
        client.setProject(null);
        client.setDateTime(null);
        client.setExtraProjects(null);

        clientRepository.save(client);
        log.info("Clear client and move him to start state {}", client);
    }

    private void updateClientNotification(Client client) {
        Notification notification = notificationRepository.getAllByUid(client.getUid());
        if (notification == null) {
            log.info("Notification wasn't set by client {}", client);
            return;
        }
        LocalDateTime nextFireTime = notification.getNextFireTime();
        if (nextFireTime == null || nextFireTime.toLocalDate().
                isAfter(LocalDateTime.now().toLocalDate())) {
            log.info("Not nesseccary to send notification to client {}", client);
            return;
        }
        notification.setNextFireTime(nextFireTime.plusHours(24));
        notificationRepository.save(notification);
        log.info("Set client {} notification {}", client, notification);
    }
}
