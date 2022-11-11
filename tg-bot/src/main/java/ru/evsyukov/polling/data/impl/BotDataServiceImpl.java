package ru.evsyukov.polling.data.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.entity.Notification;
import ru.evsyukov.app.data.entity.Project;
import ru.evsyukov.app.data.entity.ReportDay;
import ru.evsyukov.app.data.repository.ClientRepository;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.app.data.repository.NotificationRepository;
import ru.evsyukov.app.data.repository.ProjectsRepository;
import ru.evsyukov.app.data.repository.ReportDayRepository;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.data.BotDataService;
import ru.evsyukov.utils.helpers.DateTimeUtils;
import ru.evsyukov.utils.messages.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BotDataServiceImpl implements BotDataService {

    private final ClientRepository clientRepository;

    private final NotificationRepository notificationRepository;

    private final ReportDayRepository reportDayRepository;

    private final EmployeeRepository employeeRepository;

    private final ProjectsRepository projectsRepository;

    @Autowired
    public BotDataServiceImpl(ClientRepository clientRepository,
                              NotificationRepository notificationRepository,
                              ReportDayRepository reportDayRepository,
                              EmployeeRepository employeeRepository,
                              ProjectsRepository projectsRepository) {
        this.clientRepository = clientRepository;
        this.notificationRepository = notificationRepository;
        this.reportDayRepository = reportDayRepository;
        this.employeeRepository = employeeRepository;
        this.projectsRepository = projectsRepository;
    }

    @Override
    public void updateClientState(Client client, State state) {
        client.setState(state);
        clientRepository.save(client);
        log.info("Update client state {}", client);
    }

    public void updateClientStateAndName(Client client, State state, String name, boolean isRegistered) {
        client.setState(state);
        client.setName(name);
        client.setRegistered(isRegistered);
        clientRepository.save(client);
        log.info("Successfully update client {} at database", client);
    }

    @Override
    public void updateClientVacation(Client client, State state, Date start, Date end, boolean isOnVacation) {
        client.setState(state);
        client.setStartVacation(start);
        client.setEndVacation(end);
        client.setOnVacation(isOnVacation);
        clientRepository.save(client);
        log.info("Set vacation on client {}", client);
    }

    @Override
    public void moveClientToVacation(Client client) {
        client.setOnVacation(true);
        clientRepository.save(client);
        log.info("Update client vacation info {}", client);
    }

    @Override
    public void clearClientVacation(Client client) {
        log.info("Clear vacation on client {}", client);
        updateClientVacation(client, State.MENU_CHOICE, null, null, false);
    }

    @Override
    public LocalDateTime getClientChosenTime(Client client) {
        return notificationRepository.findById(client.getUid())
                .map(Notification::getNextFireTime)
                .orElse(null);
    }

    @Override
    public void updateNotification(Client client, LocalDateTime time) {
        Notification notification = new Notification();
        notification.setUid(client.getUid());
        notification.setNextFireTime(time);
        notificationRepository.save(notification);
        log.info("Update client notification {}", notification);
    }

    @Override
    public void incrementNotificationTime(Client client) {
        Notification notification = client.getNotification();
        notification.setNextFireTime(notification.getNextFireTime().plusDays(1));
        notificationRepository.save(notification);
        log.info("Update client notification {}", notification);
    }

    @Override
    public boolean isReportToday(Client client) {
        ReportDay reportDay = reportDayRepository.findReportDayByUidAndDate(client.getUid(), new Date());
        return reportDay != null;
    }

    @Override
    public void updateClientDateAndState(Client client, State state, LocalDateTime date) {
        client.setState(state);
        client.setDateTime(date);
        clientRepository.save(client);
        log.info("Update client state and date {}", client);
    }

    @Override
    public void updateClientProjects(Client client, State state, List<String> projects) {
        client.setState(state);
        client.setDateTime(client.getDateTime() == null ? LocalDateTime.now() : client.getDateTime());
        client.setProject(projects.get(0));
        if (projects.size() > 1) {
            client.setExtraProjects(String.join(Message.DELIMETR,
                    projects.subList(1, projects.size())));
        }
        clientRepository.save(client);
        log.info("Update client projects {}, proj: {}", client, projects);
    }

    @Override
    public List<String> getAllEmployeeNamesSorted() {
        return employeeRepository.getAllEmployeeNames();
    }

    @Override
    public List<Project> getAllProjectsSorted() {
        return projectsRepository.findByOrderByProjectNameAsc();
    }

    @Override
    public List<Client> getNotificationClients() {
        return notificationRepository.getAllByNextFireTimeBefore(LocalDateTime.now().plusHours(3))
                .stream().map(Notification::getClient)
                .collect(Collectors.toList());
    }

    @Override
    public List<LocalDate> findFullReportDaysInterval(long clientId, int interval) {
        Date start = DateTimeUtils.fromLocalDate(LocalDate.now().minusDays(interval));
        Date finished = DateTimeUtils.fromLocalDate(LocalDate.now().minusDays(1));
        List<ReportDay> reportDays = reportDayRepository.findReportDayByDateGreaterThanEqualAndDateLessThanEqualAndUidEquals(
                start, finished, clientId);
        return reportDays.stream().map(ReportDay::getDate)
                .map(DateTimeUtils::toLocalDate).collect(Collectors.toList());
    }

    public List<String> getAllRegisteredClientNames() {
        return clientRepository.findAll()
                .stream()
                .map(Client::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isRegisteredClient(Client client) {
        return clientRepository
                .findById(client.getUid())
                .filter(Client::isRegistered)
                .isPresent();
    }

    @Override
    public void clearClient(Client client, State state) {
        client.setState(state);
        client.setProject(null);
        client.setDateTime(null);
        client.setExtraProjects(null);

        clientRepository.save(client);
        log.info("Clear client and move him to start state {}", client);
    }

    public void saveOrUpdateReportDays(Client client, String finalDailyReportProjects) {
        Date reportDate = DateTimeUtils.fromLocalDate(client.getDateTime().toLocalDate());
        ReportDay reportDay = reportDayRepository.findReportDayByUidAndDate(client.getUid(), reportDate);
        if (reportDay == null) {
            reportDay = new ReportDay();
            reportDay.setEmployee(employeeRepository.getEmployeeByName(client.getName()));
            reportDay.setProjects(finalDailyReportProjects);
            reportDay.setUid(client.getUid());
            // время ставим по МСК
            reportDay.setDate(reportDate);
            reportDayRepository.save(reportDay);
            log.info("Save report day for client id {} day {}", reportDay.getUid(), reportDate);
        } else {
            reportDay.setProjects(finalDailyReportProjects);
            reportDayRepository.save(reportDay);
            log.info("Update report day for client id {} day {}", reportDay.getUid(), reportDate);
        }
    }

    // получить Проекты из сохранненых в строку ключей, разделенных разделителем
    @Override
    public Set<Project> getExtraProjectsFromIds(String extraProjects) {
        return Arrays.stream(extraProjects.split(Message.DELIMETR))
                .map(proj -> projectsRepository.getProjectById(
                        Long.parseLong(proj)))
                .collect(Collectors.toSet());
    }

    @Override
    public Project getMainProjectById(Client client) {
        return projectsRepository.getProjectById(Long.parseLong(client.getProject()));
    }

    @Override
    public List<Client> getClientsOnVacation() {
        return clientRepository.getAllByStartVacationIsNotNullAndEndVacationIsNotNull();
    }

    public Optional<Client> getClientById(long uid) {
        return clientRepository.findById(uid);
    }

    public Client saveNewClient(long id) {
        Client client = new Client();
        State current = State.REGISTER_NAME;
        client.setState(current);
        client.setUid(id);
        clientRepository.save(client);
        log.info("Create client with id {}", client);
        return client;
    }
}
