package ru.evsyukov.app.data.repository;

import ru.evsyukov.app.data.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findAll();

    List<Client> getAllByStartVacationIsNotNullAndEndVacationIsNotNull();

    List<Client> getAllByRegisteredIs(boolean isRegistered);


}
