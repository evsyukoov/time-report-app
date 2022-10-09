package data.repository;

import hibernate.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Client findClientByUid(long uid);

    List<Client> findAll();

    List<Client> getAllByStartVacationIsNotNullAndEndVacationIsNotNull();


}
