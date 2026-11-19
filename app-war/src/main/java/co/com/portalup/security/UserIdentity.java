package co.com.portalup.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Usuario de la solicitud y sus grupos tal como los ve WebSphere. Es lo que se compara con tbl_icn_designer_groups
 * (DesignerAccessService) y lo que muestra GET /api/v1/me/identity para copiar los nombres exactos a la tabla.
 */
public class UserIdentity {

    private static final Pattern SHORT_NAME = Pattern.compile("^(?:cn|uid)=([^,]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPACES_AROUND_SEPARATORS = Pattern.compile("\\s*([,=])\\s*");

    private final Entry user;
    private final List<Entry> groups;

    public UserIdentity(Entry user, List<Entry> groups) {
        this.user = user;
        this.groups = groups == null ? Collections.<Entry>emptyList() : groups;
    }

    /** Sin usuario autenticado (no hay Subject de WebSphere). */
    public static UserIdentity anonymous() {
        return new UserIdentity(null, null);
    }

    public Entry getUser() {
        return user;
    }

    public List<Entry> getGroups() {
        return groups;
    }

    /** Nombres del usuario que valen en la tabla: inicio de sesión, nombre corto y DN, sin repetidos. */
    public List<String> userNames() {
        return user == null ? Collections.<String>emptyList() : user.names();
    }

    /** Nombres de los grupos que valen en la tabla: nombre corto y DN de cada uno, sin repetidos. */
    public List<String> groupNames() {
        List<String> names = new ArrayList<>();
        for (Entry group : groups) {
            for (String name : group.names()) {
                if (!names.contains(name)) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    /** Forma en que se comparan los nombres: minúsculas y sin espacios alrededor de "," e "=" de los DN. */
    public static String normalize(String name) {
        if (name == null) {
            return null;
        }
        return SPACES_AROUND_SEPARATORS.matcher(name.trim().toLowerCase(Locale.ROOT)).replaceAll("$1");
    }

    /** Usuario o grupo: { name, shortName, dn, dnNormalized, id }. */
    public static class Entry {

        private final String name;
        private final String shortName;
        private final String dn;
        private final String id;

        public Entry(String name, String shortName, String dn, String id) {
            this.name = name;
            this.shortName = shortName;
            this.dn = dn;
            this.id = id;
        }

        /**
         * Desde los datos de WebSphere: name es el nombre de inicio de sesión (null para un grupo) y id el
         * identificador "user:realm/uniqueId" o "group:realm/uniqueId" (en LDAP el uniqueId es el DN).
         * "group:realm/cn=DQ_Disenadores,ou=grupos,o=empresa" → name y dn el DN, shortName "DQ_Disenadores";
         * "group:realm/Administradores" → name y shortName "Administradores", sin dn.
         */
        public static Entry parse(String name, String id) {
            String uniqueId = id == null ? "" : id.trim();
            if (uniqueId.startsWith("group:") || uniqueId.startsWith("user:")) {
                uniqueId = uniqueId.substring(uniqueId.indexOf(':') + 1);
                int slash = uniqueId.indexOf('/');
                if (slash >= 0) {
                    uniqueId = uniqueId.substring(slash + 1);
                }
                uniqueId = uniqueId.trim();
            }
            String dn = uniqueId.contains("=") ? uniqueId : null;
            String loginName = name == null || name.trim().isEmpty() ? null : name.trim();
            String shortName = null;
            if (dn != null) {
                Matcher matcher = SHORT_NAME.matcher(dn);
                if (matcher.find()) {
                    shortName = matcher.group(1).trim();
                }
            }
            if (shortName == null) {
                shortName = loginName != null ? loginName : emptyToNull(uniqueId);
            }
            return new Entry(loginName != null ? loginName : emptyToNull(uniqueId), shortName, dn, emptyToNull(id));
        }

        public String getName() {
            return name;
        }

        public String getShortName() {
            return shortName;
        }

        public String getDn() {
            return dn;
        }

        public String getDnNormalized() {
            return normalize(dn);
        }

        public String getId() {
            return id;
        }

        /** name, shortName y dn, sin vacíos ni repetidos. */
        public List<String> names() {
            List<String> names = new ArrayList<>();
            for (String value : new String[] { name, shortName, dn }) {
                if (value != null && !value.isEmpty() && !names.contains(value)) {
                    names.add(value);
                }
            }
            return names;
        }

        private static String emptyToNull(String value) {
            return value == null || value.trim().isEmpty() ? null : value.trim();
        }
    }
}
