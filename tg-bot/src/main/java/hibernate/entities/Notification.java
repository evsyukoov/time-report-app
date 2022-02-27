package hibernate.entities;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @Column(name = "uid")
    private long uid;

    @Column(name = "nextFireTime")
    private LocalDateTime nextFireTime;

    @OneToOne
    @PrimaryKeyJoinColumn(name = "uid", referencedColumnName = "uid")
    public Client client;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public LocalDateTime getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(LocalDateTime nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
