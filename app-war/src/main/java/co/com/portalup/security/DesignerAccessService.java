package co.com.portalup.security;

import co.com.portalup.entity.DesignerGroupStatus;
import co.com.portalup.repository.DesignerGroupRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Decide si el usuario actual puede usar el diseñador: pertenece a alguno de los grupos ACTIVE de
 * tbl_icn_designer_groups (sin distinguir mayúsculas, por nombre corto o DN). La tabla se lee en cada consulta, así
 * que los cambios aplican de inmediato; sin grupos activos nadie tiene acceso.
 */
@Service
public class DesignerAccessService {

    private final DesignerGroupRepository designerGroupRepository;
    private final UserGroupsProvider userGroupsProvider;

    public DesignerAccessService(DesignerGroupRepository designerGroupRepository,
                                 UserGroupsProvider userGroupsProvider) {
        this.designerGroupRepository = designerGroupRepository;
        this.userGroupsProvider = userGroupsProvider;
    }

    public boolean canDesign() throws Exception {
        List<String> allowedGroups = designerGroupRepository.findNamesByStatus(DesignerGroupStatus.ACTIVE);
        if (allowedGroups.isEmpty()) {
            return false;
        }
        return isMemberOfAny(userGroupsProvider.currentUserGroups(), allowedGroups);
    }

    static boolean isMemberOfAny(List<String> userGroups, List<String> allowedGroups) {
        for (String userGroup : userGroups) {
            for (String allowedGroup : allowedGroups) {
                if (allowedGroup != null && userGroup.trim().equalsIgnoreCase(allowedGroup.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
