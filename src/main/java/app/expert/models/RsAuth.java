package app.expert.models;

import app.base.exceptions.GException;
import app.expert.db.manager.Manager;
import app.expert.db.statics.managerRole.ManagerRoleCache;
import app.expert.db.station_info.StationInfo;
import app.expert.models.manager.RSManager;
import app.expert.models.manager_role.RSRole;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RsAuth {

    private String token;
    private RSManager manager;
    private StationInfo stationInfo;

    public static RsAuth get(Manager manager, ManagerRoleCache roleCache, String token) throws GException {
        return builder()
                .manager(RSManager.get(manager, RSRole.get(roleCache.find(manager.getRole()))))
                .stationInfo(manager.getStationInfo())
                .token(token)
                .build();
    }
}
