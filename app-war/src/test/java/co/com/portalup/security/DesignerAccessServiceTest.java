package co.com.portalup.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.com.portalup.entity.DesignerGroupStatus;
import co.com.portalup.repository.DesignerGroupRepository;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DesignerAccessServiceTest {

    private final DesignerGroupRepository repository = mock(DesignerGroupRepository.class);
    private final UserGroupsProvider provider = mock(UserGroupsProvider.class);
    private final DesignerAccessService service = new DesignerAccessService(repository, provider);

    private static final UserIdentity.Entry JPEREZ =
            UserIdentity.Entry.parse("jperez", "user:ldap.empresa.com:389/uid=jperez,ou=usuarios,o=empresa");

    private void identity(String... groupIds) throws Exception {
        UserIdentity.Entry[] groups = new UserIdentity.Entry[groupIds.length];
        for (int i = 0; i < groupIds.length; i++) {
            groups[i] = UserIdentity.Entry.parse(null, groupIds[i]);
        }
        when(provider.currentIdentity()).thenReturn(new UserIdentity(JPEREZ, Arrays.asList(groups)));
    }

    private void allowed(String... names) {
        when(repository.findNamesByStatus(DesignerGroupStatus.ACTIVE)).thenReturn(Arrays.asList(names));
    }

    @Test
    void memberOfAnActiveGroupCanDesignIgnoringCase() throws Exception {
        allowed("Otros", " dq_disenadores ");
        identity("group:ldap.empresa.com:389/cn=DQ_Disenadores,ou=grupos,o=empresa");

        assertThat(service.canDesign()).isTrue();
    }

    @Test
    void groupConfiguredAsDistinguishedNameMatchesIgnoringCaseAndSpaces() throws Exception {
        allowed("CN=DQ_Disenadores, OU=Grupos, O=Empresa");
        identity("group:ldap/cn=DQ_Disenadores,ou=grupos,o=empresa");

        assertThat(service.canDesign()).isTrue();
    }

    @Test
    void userOutsideTheGroupsCannotDesign() throws Exception {
        allowed("DQ_Disenadores");
        identity("group:defaultWIMFileBasedRealm/Consulta");

        assertThat(service.canDesign()).isFalse();
    }

    @Test
    void withoutActiveGroupsNobodyCanDesign() throws Exception {
        when(repository.findNamesByStatus(DesignerGroupStatus.ACTIVE)).thenReturn(Collections.<String>emptyList());
        identity("group:ldap/cn=DQ_Disenadores,ou=grupos,o=empresa");

        assertThat(service.canDesign()).isFalse();
    }

    @Test
    void userConfiguredByLoginNameOrDnCanDesignWithoutAGroup() throws Exception {
        identity("group:defaultWIMFileBasedRealm/Consulta");

        allowed("JPerez");
        assertThat(service.canDesign()).isTrue();
        allowed("UID=jperez,OU=usuarios,O=empresa");
        assertThat(service.canDesign()).isTrue();
    }

    @Test
    void anonymousCannotDesign() throws Exception {
        allowed("DQ_Disenadores");
        when(provider.currentIdentity()).thenReturn(UserIdentity.anonymous());

        assertThat(service.canDesign()).isFalse();
    }

    @Test
    void webSphereGroupIdGivesDistinguishedNameAndShortName() {
        UserIdentity.Entry ldap = UserIdentity.Entry.parse(null, "group:ldap.empresa.com:389/cn=DQ_Disenadores,ou=grupos,o=empresa");
        assertThat(ldap.getName()).isEqualTo("cn=DQ_Disenadores,ou=grupos,o=empresa");
        assertThat(ldap.getShortName()).isEqualTo("DQ_Disenadores");
        assertThat(ldap.getDn()).isEqualTo("cn=DQ_Disenadores,ou=grupos,o=empresa");
        assertThat(ldap.getId()).isEqualTo("group:ldap.empresa.com:389/cn=DQ_Disenadores,ou=grupos,o=empresa");

        UserIdentity.Entry file = UserIdentity.Entry.parse(null, "group:defaultWIMFileBasedRealm/Administradores");
        assertThat(file.names()).containsExactly("Administradores");
        assertThat(file.getDn()).isNull();
    }

    @Test
    void webSphereUserGivesLoginNameShortNameAndDistinguishedName() {
        assertThat(JPEREZ.getName()).isEqualTo("jperez");
        assertThat(JPEREZ.getShortName()).isEqualTo("jperez");
        assertThat(JPEREZ.getDn()).isEqualTo("uid=jperez,ou=usuarios,o=empresa");
        assertThat(JPEREZ.names()).containsExactly("jperez", "uid=jperez,ou=usuarios,o=empresa");
    }

    @Test
    void normalizeLowercasesAndRemovesSpacesAroundSeparators() {
        assertThat(UserIdentity.normalize(" CN=CEAdmins , OU=Grupos ,DC = prueba,DC=local "))
                .isEqualTo("cn=ceadmins,ou=grupos,dc=prueba,dc=local");
    }
}
