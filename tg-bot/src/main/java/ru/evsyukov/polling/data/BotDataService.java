package ru.evsyukov.polling.data;

import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.entity.Project;
import ru.evsyukov.app.state.State;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BotDataService {

    void updateClientState(Client client, State state);

    void updateClientVacation(Client client, State state, Date start, Date end, boolean isOnVacation);

    LocalDateTime getClientChosenTime(Client client);

    void updateNotification(Client client, LocalDateTime time);

    void incrementNotificationTime(Client client);

    boolean isReportToday(Client client);

    void updateClientDateAndState(Client client, State state, LocalDateTime date);

    void updateClientProjects(Client client, State state, List<String> projects);

    void clearClientVacation(Client client);

    List<String> getAllEmployeeNamesSorted();

    List<Project> getAllProjectsSorted();

    List<Client> getNotificationClients();

    List<LocalDate> findFullReportDaysInterval(long clientId, int interval);

    List<String> getAllRegisteredClientNames();

    void updateClientStateAndName(Client client, State state, String name, boolean isRegistered);

    boolean isRegisteredClient(Client client);

    void clearClient(Client client, State state);

    void saveOrUpdateReportDays(Client client, String finalDailyReportProjects);

    Set<Project> getExtraProjectsFromIds(String extraProjects);

    Project getMainProjectById(Client client);

    List<Client> getClientsOnVacation();

    void moveClientToVacation(Client client);

    Optional<Client> getClientById(long uid);

    Client saveNewClient(long id);

}
