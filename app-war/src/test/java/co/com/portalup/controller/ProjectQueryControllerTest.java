package co.com.portalup.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.portalup.dto.CreateProjectQueryRequest;
import co.com.portalup.dto.ProjectQueryResponse;
import co.com.portalup.dto.UpdateProjectQueryRequest;
import co.com.portalup.entity.ProjectQueryStatus;
import co.com.portalup.exception.DuplicateProjectQueryException;
import co.com.portalup.exception.ProjectNotFoundException;
import co.com.portalup.exception.ProjectQueryNotFoundException;
import co.com.portalup.exception.QueryNotFoundException;
import co.com.portalup.service.ProjectQueryService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectQueryController.class)
class ProjectQueryControllerTest {

    private static final String BASE_URL = "/api/v1/project-query";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 3, 19, 20);
    private static final String CONFIG = "{}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectQueryService projectQueryService;

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        when(projectQueryService.create(any(CreateProjectQueryRequest.class)))
                .thenReturn(response(1L, 10L, 20L, ProjectQueryStatus.ACTIVE));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("10", "20", CONFIG, "ACTIVE")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/project-query/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.projectId").value(10))
                .andExpect(jsonPath("$.queryId").value(20))
                .andExpect(jsonPath("$.config").value(CONFIG))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").value("2026-10-03T19:20:00"));
    }

    @Test
    void createWithoutStatusAndConfigPassesNullsToService() throws Exception {
        when(projectQueryService.create(any(CreateProjectQueryRequest.class)))
                .thenReturn(response(1L, 10L, 20L, ProjectQueryStatus.ACTIVE));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("10", "20", null, null)))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateProjectQueryRequest> captor = ArgumentCaptor.forClass(CreateProjectQueryRequest.class);
        verify(projectQueryService).create(captor.capture());
        assertThat(captor.getValue().getStatus()).isNull();
        assertThat(captor.getValue().getConfig()).isNull();
    }

    @Test
    void createWithoutProjectIdReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body(null, "20", null, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value(BASE_URL))
                .andExpect(jsonPath("$.details[0]").value("projectId: El id del proyecto es obligatorio"));
        verifyNoInteractions(projectQueryService);
    }

    @Test
    void createWithoutQueryIdReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("10", null, null, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("queryId: El id de la consulta es obligatorio"));
        verifyNoInteractions(projectQueryService);
    }

    @Test
    void createWithNonPositiveProjectIdReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("0", "20", null, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("projectId: El id del proyecto debe ser un número positivo"));
        verifyNoInteractions(projectQueryService);
    }

    @Test
    void createWithConfigLongerThan1024Returns400() throws Exception {
        String config = String.join("", Collections.nCopies(1025, "a"));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("10", "20", config, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]")
                        .value("config: La configuración no puede superar los 1024 caracteres"));
        verifyNoInteractions(projectQueryService);
    }

    @Test
    void createWithInvalidStatusReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("10", "20", null, "DELETED")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(projectQueryService);
    }

    @Test
    void createReturns404WhenProjectDoesNotExist() throws Exception {
        when(projectQueryService.create(any(CreateProjectQueryRequest.class)))
                .thenThrow(new ProjectNotFoundException(10L));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("10", "20", null, null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el proyecto con id 10"));
    }

    @Test
    void createReturns404WhenQueryDoesNotExist() throws Exception {
        when(projectQueryService.create(any(CreateProjectQueryRequest.class)))
                .thenThrow(new QueryNotFoundException(20L));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("10", "20", null, null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe la consulta con id 20"));
    }

    @Test
    void createReturns409WhenAssociationAlreadyExists() throws Exception {
        when(projectQueryService.create(any(CreateProjectQueryRequest.class)))
                .thenThrow(new DuplicateProjectQueryException(10L, 20L));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("10", "20", null, null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("La consulta con id 20 ya está asociada al proyecto con id 10"));
    }

    @Test
    void findByIdReturns200() throws Exception {
        when(projectQueryService.findById(1L)).thenReturn(response(1L, 10L, 20L, ProjectQueryStatus.ACTIVE));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.projectId").value(10));
    }

    @Test
    void findByIdReturns404WhenAssociationDoesNotExist() throws Exception {
        when(projectQueryService.findById(99L)).thenThrow(new ProjectQueryNotFoundException(99L));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No existe la asociación proyecto-consulta con id 99"));
    }

    @Test
    void findByIdWithNonPositiveIdReturns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]")
                        .value("El id de la asociación proyecto-consulta debe ser un número positivo"));
        verifyNoInteractions(projectQueryService);
    }

    @Test
    void findByIdWithNonNumericIdReturns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/abc"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(projectQueryService);
    }

    @Test
    void findAllReturns200WithEveryAssociation() throws Exception {
        when(projectQueryService.findAll(null, null, null)).thenReturn(Arrays.asList(
                response(1L, 10L, 20L, ProjectQueryStatus.ACTIVE),
                response(2L, 11L, 21L, ProjectQueryStatus.INACTIVE)));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void findAllPassesFiltersToService() throws Exception {
        when(projectQueryService.findAll(10L, 20L, ProjectQueryStatus.ACTIVE))
                .thenReturn(Collections.singletonList(response(1L, 10L, 20L, ProjectQueryStatus.ACTIVE)));

        mockMvc.perform(get(BASE_URL).param("projectId", "10").param("queryId", "20").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].projectId").value(10));
    }

    @Test
    void findAllWithInvalidStatusReturns400() throws Exception {
        mockMvc.perform(get(BASE_URL).param("status", "DELETED"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(projectQueryService);
    }

    @Test
    void findAllWithNonPositiveProjectIdReturns400() throws Exception {
        mockMvc.perform(get(BASE_URL).param("projectId", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("El id del proyecto debe ser un número positivo"));
        verifyNoInteractions(projectQueryService);
    }

    @Test
    void updateReturns200() throws Exception {
        when(projectQueryService.update(eq(1L), any(UpdateProjectQueryRequest.class)))
                .thenReturn(response(1L, 11L, 21L, ProjectQueryStatus.INACTIVE));

        mockMvc.perform(put(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body("11", "21", CONFIG, "INACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(11))
                .andExpect(jsonPath("$.queryId").value(21))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void updateReturns404WhenAssociationDoesNotExist() throws Exception {
        when(projectQueryService.update(eq(99L), any(UpdateProjectQueryRequest.class)))
                .thenThrow(new ProjectQueryNotFoundException(99L));

        mockMvc.perform(put(BASE_URL + "/99").contentType(MediaType.APPLICATION_JSON)
                        .content(body("10", "20", null, null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateIgnoresIdAndCreatedAtSentInBody() throws Exception {
        when(projectQueryService.update(eq(1L), any(UpdateProjectQueryRequest.class)))
                .thenReturn(response(1L, 10L, 20L, ProjectQueryStatus.ACTIVE));

        mockMvc.perform(put(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":500,\"createdAt\":\"2000-01-01T00:00:00\",\"projectId\":10,\"queryId\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.createdAt").value("2026-10-03T19:20:00"));

        verify(projectQueryService).update(eq(1L), any(UpdateProjectQueryRequest.class));
    }

    @Test
    void updateWithoutQueryIdReturns400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body("10", null, null, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("queryId: El id de la consulta es obligatorio"));
        verifyNoInteractions(projectQueryService);
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
        verify(projectQueryService).delete(1L);
    }

    @Test
    void deleteReturns404WhenAssociationDoesNotExist() throws Exception {
        doThrow(new ProjectQueryNotFoundException(99L)).when(projectQueryService).delete(99L);

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }

    private static ProjectQueryResponse response(Long id, Long projectId, Long queryId, ProjectQueryStatus status) {
        return new ProjectQueryResponse(id, projectId, queryId, CONFIG, status, CREATED_AT);
    }

    /** Arma el JSON del request omitiendo los campos null; los ids se envían como números. */
    private static String body(String projectId, String queryId, String config, String status) {
        StringBuilder json = new StringBuilder("{");
        appendField(json, "projectId", projectId, false);
        appendField(json, "queryId", queryId, false);
        appendField(json, "config", config, true);
        appendField(json, "status", status, true);
        return json.append('}').toString();
    }

    private static void appendField(StringBuilder json, String field, String value, boolean quoted) {
        if (value == null) {
            return;
        }
        if (json.length() > 1) {
            json.append(',');
        }
        json.append('"').append(field).append("\":");
        if (quoted) {
            json.append('"').append(value).append('"');
        } else {
            json.append(value);
        }
    }
}
