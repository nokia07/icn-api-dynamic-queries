package co.com.portalup.security;

import com.ibm.websphere.security.auth.WSSubject;
import com.ibm.websphere.security.cred.WSCredential;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.Subject;
import org.springframework.stereotype.Component;

/**
 * Grupos del usuario según WebSphere: los de la credencial del Subject de la solicitud, autenticada con la cookie LTPA
 * de ICN (web.xml). WebSphere los da como "group:realm/uniqueId" (el uniqueId es el DN en LDAP); de cada uno se
 * devuelve el DN y el nombre corto (CN), para que en la tabla se pueda configurar cualquiera de los dos.
 */
@Component
public class WebSphereUserGroupsProvider implements UserGroupsProvider {

    private static final Pattern COMMON_NAME = Pattern.compile("^cn=([^,]+)", Pattern.CASE_INSENSITIVE);

    @Override
    public List<String> currentUserGroups() throws Exception {
        List<String> names = new ArrayList<>();
        Subject subject = WSSubject.getCallerSubject();
        if (subject == null) {
            return names;
        }
        for (WSCredential credential : subject.getPublicCredentials(WSCredential.class)) {
            for (Object groupId : credential.getGroupIds()) {
                addGroup(names, String.valueOf(groupId));
            }
        }
        return names;
    }

    /** "group:realm/cn=DQ_Disenadores,ou=grupos,o=empresa" → el DN y "DQ_Disenadores". */
    static void addGroup(List<String> names, String groupId) {
        String uniqueId = groupId.startsWith("group:") ? groupId.substring("group:".length()) : groupId;
        int slash = uniqueId.indexOf('/');
        if (slash >= 0) {
            uniqueId = uniqueId.substring(slash + 1);
        }
        uniqueId = uniqueId.trim();
        if (uniqueId.isEmpty()) {
            return;
        }
        names.add(uniqueId);
        Matcher commonName = COMMON_NAME.matcher(uniqueId);
        if (commonName.find()) {
            names.add(commonName.group(1).trim());
        }
    }
}
