package data.repository;

import hibernate.entities.ReportDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportDayRepository extends JpaRepository<ReportDay, Long> {

    List<ReportDay> findAll();

    List<ReportDay> findReportDayByEmployeeName(String name);

    List<ReportDay> findReportDayByDateGreaterThanEqualAndDateLessThanEqual(Date start, Date end);

    List<ReportDay> findReportDayByDateGreaterThanEqual(Date start);

    List<ReportDay> findReportDayByDateLessThanEqual(Date end);

    List<ReportDay> findReportDayByDateGreaterThanEqualAndDateLessThanEqualAndEmployeeName(Date start, Date end, String name);

    List<ReportDay> findReportDayByDateGreaterThanEqualAndEmployeeName(Date start, String name);

    List<ReportDay> findReportDayByDateLessThanEqualAndEmployeeName(Date end, String name);

    List<ReportDay> findReportDayByUidAndDateBetween(long uid, Date start, Date end);

}
