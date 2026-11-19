package co.com.portalup.security;

import co.com.portalup.entity.DesignerGroupStatus;
import co.com.portalup.repository.DesignerGroupRepository;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

/**
 * Decide si el usuario actual puede usar el diseñador: su nombre o alguno de sus grupos está ACTIVE en
 * tbl_icn_designer_groups (UserIdentity.normalize: sin distinguir mayúsculas ni espacios en los DN; nombre de inicio
 * de sesión, nombre corto o DN). La tabla se lee en cada consulta, así que los cambios aplican de inmediato; sin
 * registros activos nadie tiene acceso. Cada decisión queda en el log con el usuario y sus grupos.
 */
@Service
public class DesignerAccessService {

    private static final Log LOG = LogFactory.getLog(DesignerAccessService.class);

    private final DesignerGroupRepository designerGroupRepository;
    private final UserGroupsProvider userGroupsProvider;

    public DesignerAccessService(DesignerGroupRepository designerGroupRepository,
                                 UserGroupsProvider userGroupsProvider) {
        this.designerGroupRepository = designerGroupRepository;
        this.userGroupsProvider = userGroupsProvider;
    }

    public boolean canDesign() throws Exception {
        UserIdentity identity = userGroupsProvider.currentIdentity();
        List<String> userNames = identity.userNames();
        List<String> groupNames = identity.groupNames();
        List<String> allowed = designerGroupRepository.findNamesByStatus(DesignerGroupStatus.ACTIVE);
        boolean canDesign = isMemberOfAny(userNames, allowed) || isMemberOfAny(groupNames, allowed);
        LOG.info("Acceso al diseñador " + (canDesign ? "permitido" : "denegado") + ": usuario=" + userNames
                + " grupos=" + groupNames + (allowed.isEmpty() ? " (tbl_icn_designer_groups sin registros ACTIVE)" : ""));
        return canDesign;
    }

    static boolean isMemberOfAny(List<String> names, List<String> allowedNames) {
        for (String name : names) {
            for (String allowedName : allowedNames) {
                if (allowedName != null && UserIdentity.normalize(name).equals(UserIdentity.normalize(allowedName))) {
                    return true;
                }
            }
        }
        return false;
    }
}
