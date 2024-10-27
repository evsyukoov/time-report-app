package ru.evsyukov.app.data.repository;

import org.springframework.data.jpa.repository.Query;
import ru.evsyukov.app.data.entity.ReportDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportDayRepository extends JpaRepository<ReportDay, Long> {

    List<ReportDay> findAll();

    List<ReportDay> findReportDayByDateGreaterThanEqualAndDateLessThanEqualAndUidEquals(Date start, Date end, Long clientUid);

    List<ReportDay> findReportDayByEmployeeName(String name);

    ReportDay findReportDayByUidAndDate(Long uid, Date date);

    List<ReportDay> findReportDayByDateGreaterThanEqualAndDateLessThanEqual(Date start, Date end);

    List<ReportDay> findReportDayByDateGreaterThanEqual(Date start);

    List<ReportDay> findReportDayByDateLessThanEqual(Date end);

    List<ReportDay> findReportDayByDateGreaterThanEqualAndDateLessThanEqualAndEmployeeName(Date start, Date end, String name);

    List<ReportDay> findReportDayByDateGreaterThanEqualAndEmployeeName(Date start, String name);

    List<ReportDay> findReportDayByDateLessThanEqualAndEmployeeName(Date end, String name);

    List<ReportDay> findReportDayByUidAndDateBetween(long uid, Date start, Date end);

    ReportDay findFirstByUidOrderByDateDesc(long uid);

    List<ReportDay> findReportDaysByProjectsContains(String projectName);

    @Query("SELECT new ReportDay(rd.employee, MAX(rd.date)) FROM ReportDay rd WHERE rd.projects != :project " +
            "GROUP BY rd.employee ORDER BY rd.employee.department, rd.employee.name")
    List<ReportDay> findLastReportDays(String project);
}
