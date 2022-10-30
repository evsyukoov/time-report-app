package ru.evsyukov.polling.hibernate.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "clients")
//@Cacheable
//@org.ru.evsyukoov.polling.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Client {

    @Id
    @Column(name = "uid")
    private long uid;

    @Column(name = "name")
    private String name;

    @Column(name = "state")
    private int state;

    @Column(name = "current_project")
    private String project;

    @Column(name = "report_date")
    private LocalDateTime dateTime;

    @Column(name = "registered")
    private boolean registered;

    @Column(name = "extra_projects")
    private String extraProjects;

    @Column(name = "on_vacation")
    private boolean onVacation;

    @Column(name = "start_vacation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startVacation;

    @Column(name = "end_vacation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endVacation;

    public boolean isOnVacation() {
        return onVacation;
    }

    public void setOnVacation(boolean onVacation) {
        this.onVacation = onVacation;
    }

    public Date getStartVacation() {
        return startVacation;
    }

    public void setStartVacation(Date startVacation) {
        this.startVacation = startVacation;
    }

    public Date getEndVacation() {
        return endVacation;
    }

    public void setEndVacation(Date endVacation) {
        this.endVacation = endVacation;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getExtraProjects() {
        return extraProjects;
    }

    public void setExtraProjects(String extraProjects) {
        this.extraProjects = extraProjects;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public Client() {
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    @Override
    public String toString() {
        return "Client{" +
                "uid=" + uid +
                ", name='" + name + '\'' +
                ", state=" + state +
                '}';
    }
}
