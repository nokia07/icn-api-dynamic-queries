package co.com.portalup.security;

/** Usuario autenticado en la solicitud actual y sus grupos; interfaz para poder probar sin WebSphere. */
public interface UserGroupsProvider {

    /** Usuario y grupos de la solicitud; UserIdentity.anonymous() si no hay usuario autenticado. */
    UserIdentity currentIdentity() throws Exception;
}
