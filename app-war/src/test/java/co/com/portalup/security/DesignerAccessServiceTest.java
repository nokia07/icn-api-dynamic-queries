package co.com.portalup.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.com.portalup.entity.DesignerGroupStatus;
import co.com.portalup.repository.DesignerGroupRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesignerAccessServiceTest {

    private final DesignerGroupRepository repository = mock(DesignerGroupRepository.class);
    private final UserGroupsProvider userGroups = mock(UserGroupsProvider.class);
    private final DesignerAccessService service = new DesignerAccessService(repository, userGroups);

    @Test
    void memberOfAnActiveGroupCanDesignIgnoringCase() throws Exception {
        when(repository.findNamesByStatus(DesignerGroupStatus.ACTIVE)).thenReturn(Arrays.asList("Otros", " dq_disenadores "));
        when(userGroups.currentUserGroups()).thenReturn(Arrays.asList("cn=DQ_Disenadores,ou=grupos,o=empresa", "DQ_Disenadores"));

        assertThat(service.canDesign()).isTrue();
    }

    @Test
    void groupConfiguredAsDistinguishedNameMatches() throws Exception {
        when(repository.findNamesByStatus(DesignerGroupStatus.ACTIVE))
                .thenReturn(Collections.singletonList("CN=DQ_Disenadores,OU=Grupos,O=Empresa"));
        when(userGroups.currentUserGroups()).thenReturn(Arrays.asList("cn=DQ_Disenadores,ou=Grupos,o=Empresa", "DQ_Disenadores"));

        assertThat(service.canDesign()).isTrue();
    }

    @Test
    void userOutsideTheGroupsCannotDesign() throws Exception {
        when(repository.findNamesByStatus(DesignerGroupStatus.ACTIVE)).thenReturn(Collections.singletonList("DQ_Disenadores"));
        when(userGroups.currentUserGroups()).thenReturn(Collections.singletonList("Consulta"));

        assertThat(service.canDesign()).isFalse();
    }

    @Test
    void withoutActiveGroupsNobodyCanDesign() throws Exception {
        when(repository.findNamesByStatus(DesignerGroupStatus.ACTIVE)).thenReturn(Collections.emptyList());

        assertThat(service.canDesign()).isFalse();
        verifyNoInteractions(userGroups);
    }

    @Test
    void webSphereGroupIdGivesDistinguishedNameAndShortName() {
        List<String> names = new ArrayList<>();
        WebSphereUserGroupsProvider.addGroup(names, "group:ldap.empresa.com:389/cn=DQ_Disenadores,ou=grupos,o=empresa");
        WebSphereUserGroupsProvider.addGroup(names, "group:defaultWIMFileBasedRealm/Administradores");

        assertThat(names).containsExactly("cn=DQ_Disenadores,ou=grupos,o=empresa", "DQ_Disenadores", "Administradores");
    }
}
