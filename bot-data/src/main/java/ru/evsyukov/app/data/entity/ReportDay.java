package ru.evsyukov.app.data.entity;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "report_days")
public class ReportDay implements Cloneable {

    private static final SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd");
    }

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

    public ReportDay(Employee employee, Date date) {
        this.employee = employee;
        this.date = date;
    }

    public ReportDay() {
    }

    private String projects;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getUid() {
        return uid;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    @Override
    public String toString() {
        return "ReportDay{" +
                "id=" + id +
                ", employee=" + employee +
                ", date=" + sdf.format(date) +
                ", uid=" + uid +
                ", projects='" + projects + '\'' +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ReportDay rd = new ReportDay();
        rd.setProjects(this.projects);
        rd.setId(this.id);
        rd.setDate(this.date);
        rd.setUid(this.uid);
        rd.setEmployee((Employee) this.employee.clone());
        return rd;
    }
}
