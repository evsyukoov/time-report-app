package ru.evsyukov.app.data.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Копия сущности ReportDay для сохранения в таблицу-архив
 * TODO рассмотреть как сделать это красиво без дубликации кода
 */
@Entity
@Table(name = "report_days_archive")
@Getter
@Setter
@NoArgsConstructor
public class ReportDayArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    @OneToOne(cascade = CascadeType.MERGE)
    private Employee employee;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "uid")
    private long uid;

    @Column(name = "projects")
    private String projects;

    public ReportDayArchive(ReportDay reportDay) {
        this.id = reportDay.getId();
        this.employee = reportDay.getEmployee();
        this.date = reportDay.getDate();
        this.uid = reportDay.getUid();
        this.projects = reportDay.getProjects();
    }
}
