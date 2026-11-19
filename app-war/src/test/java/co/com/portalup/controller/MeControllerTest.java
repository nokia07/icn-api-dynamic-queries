package co.com.portalup.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.portalup.security.DesignerAccessService;
import co.com.portalup.security.UserGroupsProvider;
import co.com.portalup.security.UserIdentity;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MeController.class)
class MeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserGroupsProvider userGroupsProvider;

    // DesignerAccessConfig lo necesita; un GET no lo consulta.
    @MockBean
    private DesignerAccessService designerAccessService;

    @Test
    void identityShowsTheCallerAsWebSphereSeesIt() throws Exception {
        when(userGroupsProvider.currentIdentity()).thenReturn(new UserIdentity(
                UserIdentity.Entry.parse("CEADMIN", "user:prueba.local:389/CN=CEADMIN,CN=Users,DC=prueba,DC=local"),
                Collections.singletonList(UserIdentity.Entry.parse(null,
                        "group:prueba.local:389/CN=CEAdmins,OU=Grupos,DC=prueba,DC=local"))));

        mockMvc.perform(get("/api/v1/me/identity").with(request -> {
                    request.setRemoteUser("CEADMIN");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.containerPrincipal").value("CEADMIN"))
                .andExpect(jsonPath("$.ceUser.name").value("CEADMIN"))
                .andExpect(jsonPath("$.ceUser.shortName").value("CEADMIN"))
                .andExpect(jsonPath("$.ceUser.dn").value("CN=CEADMIN,CN=Users,DC=prueba,DC=local"))
                .andExpect(jsonPath("$.ceUser.dnNormalized").value("cn=ceadmin,cn=users,dc=prueba,dc=local"))
                .andExpect(jsonPath("$.ceUser.id").value("user:prueba.local:389/CN=CEADMIN,CN=Users,DC=prueba,DC=local"))
                .andExpect(jsonPath("$.groups.length()").value(1))
                .andExpect(jsonPath("$.groups[0].shortName").value("CEAdmins"))
                .andExpect(jsonPath("$.groups[0].dn").value("CN=CEAdmins,OU=Grupos,DC=prueba,DC=local"))
                .andExpect(jsonPath("$.groups[0].dnNormalized").value("cn=ceadmins,ou=grupos,dc=prueba,dc=local"));
    }
}
