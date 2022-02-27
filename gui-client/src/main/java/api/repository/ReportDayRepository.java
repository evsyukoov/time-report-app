package api.repository;

import hibernate.entities.ReportDay;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportDayRepository extends JpaRepository<ReportDay, Long> {

    List<ReportDay> findAll();

    List<ReportDay> findReportDayByDateAfterAndDateBefore(Date start, Date end);

    List<ReportDay> findReportDayByDateAfterAndDateBeforeAndEmployeeName(Date start, Date end, String name);


}
