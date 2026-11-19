package co.com.portalup.controller;

import co.com.portalup.dto.IdentityResponse;
import co.com.portalup.security.UserGroupsProvider;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Diagnóstico: la identidad del propio usuario tal como la ve la API, para registrar en tbl_icn_designer_groups los
 * nombres y DN exactos. Solo expone los datos de quien llama.
 */
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final UserGroupsProvider userGroupsProvider;

    public MeController(UserGroupsProvider userGroupsProvider) {
        this.userGroupsProvider = userGroupsProvider;
    }

    @GetMapping("/identity")
    public IdentityResponse identity(HttpServletRequest request) throws Exception {
        return new IdentityResponse(request.getRemoteUser(), userGroupsProvider.currentIdentity());
    }
}
