package co.com.portalup.dto;

import co.com.portalup.security.UserIdentity;
import java.util.List;

/**
 * GET /api/v1/me/identity: { containerPrincipal, ceUser, groups }. Los datos son los de WebSphere (no los del Content
 * Engine: id es el identificador de WebSphere, no el SID); el nombre ceUser se conserva por el contrato acordado.
 */
public class IdentityResponse {

    private final String containerPrincipal;
    private final UserIdentity.Entry ceUser;
    private final List<UserIdentity.Entry> groups;

    public IdentityResponse(String containerPrincipal, UserIdentity identity) {
        this.containerPrincipal = containerPrincipal;
        this.ceUser = identity.getUser();
        this.groups = identity.getGroups();
    }

    public String getContainerPrincipal() {
        return containerPrincipal;
    }

    public UserIdentity.Entry getCeUser() {
        return ceUser;
    }

    public List<UserIdentity.Entry> getGroups() {
        return groups;
    }
}
