package co.com.portalup.security;

import java.util.List;

/** Grupos del usuario autenticado en la solicitud actual; interfaz para poder probar sin WebSphere. */
public interface UserGroupsProvider {

    /** Nombres de los grupos (DN y nombre corto de cada uno); vacía si no hay usuario autenticado. */
    List<String> currentUserGroups() throws Exception;
}
