package co.com.portalup.security;

import com.ibm.websphere.security.auth.WSSubject;
import com.ibm.websphere.security.cred.WSCredential;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.Subject;
import org.springframework.stereotype.Component;

/**
 * Usuario y grupos según WebSphere: los de la credencial del Subject de la solicitud, autenticada con la cookie LTPA
 * de ICN (web.xml). Del usuario se toman el nombre de inicio de sesión (getSecurityName) y su "user:realm/uniqueId"
 * (getAccessId); de los grupos sus "group:realm/uniqueId" (getGroupIds). UserIdentity.Entry.parse los descompone.
 */
@Component
public class WebSphereUserGroupsProvider implements UserGroupsProvider {

    @Override
    public UserIdentity currentIdentity() throws Exception {
        Subject subject = WSSubject.getCallerSubject();
        if (subject == null) {
            return UserIdentity.anonymous();
        }
        UserIdentity.Entry user = null;
        List<UserIdentity.Entry> groups = new ArrayList<>();
        for (WSCredential credential : subject.getPublicCredentials(WSCredential.class)) {
            if (user == null) {
                user = UserIdentity.Entry.parse(credential.getSecurityName(), credential.getAccessId());
            }
            for (Object groupId : credential.getGroupIds()) {
                groups.add(UserIdentity.Entry.parse(null, String.valueOf(groupId)));
            }
        }
        return new UserIdentity(user, groups);
    }
}
