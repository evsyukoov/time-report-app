package ru.evsyukov.app.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.evsyukov.app.data.entity.ReportDayArchive;

@Repository
public interface ReportDayArchiveRepository extends JpaRepository<ReportDayArchive, Long> {

}
