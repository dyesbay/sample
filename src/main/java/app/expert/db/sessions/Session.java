package app.expert.db.sessions;

import app.base.db.GEntity;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "sessions")
public class Session extends GEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long manager;

    @Column(name = "created_on")
    private Date created;

    @Column(name = "updated_on")
    private Date updated;

    @Column(name = "expired_on")
    private Date expired;

    @Column(name = "disabled_on")
    private Date disabled;

    private Long station;

    @Override
    public boolean isDisabled() {
        return disabled != null;
    }
}
