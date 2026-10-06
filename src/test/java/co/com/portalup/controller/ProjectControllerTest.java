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

import co.com.portalup.dto.CreateProjectRequest;
import co.com.portalup.dto.ProjectResponse;
import co.com.portalup.dto.UpdateProjectRequest;
import co.com.portalup.entity.ProjectStatus;
import co.com.portalup.exception.ProjectNotFoundException;
import co.com.portalup.service.ProjectService;
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

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    private static final String BASE_URL = "/api/v1/project";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 3, 19, 20);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        when(projectService.create(any(CreateProjectRequest.class)))
                .thenReturn(response(1L, "Proyecto ejemplo", ProjectStatus.ACTIVE));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Proyecto ejemplo\",\"description\":\"Descripción\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/project/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Proyecto ejemplo"))
                .andExpect(jsonPath("$.description").value("Descripción"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").value("2026-10-03T19:20:00"));
    }

    @Test
    void createWithoutStatusPassesNullStatusToService() throws Exception {
        when(projectService.create(any(CreateProjectRequest.class)))
                .thenReturn(response(1L, "Proyecto", ProjectStatus.ACTIVE));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Proyecto\",\"description\":\"Descripción\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        ArgumentCaptor<CreateProjectRequest> captor = ArgumentCaptor.forClass(CreateProjectRequest.class);
        verify(projectService).create(captor.capture());
        assertThat(captor.getValue().getStatus()).isNull();
    }

    @Test
    void createWithBlankNameReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"description\":\"Descripción\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value(BASE_URL))
                .andExpect(jsonPath("$.details[0]").value("name: El nombre del proyecto es obligatorio"));
        verifyNoInteractions(projectService);
    }

    @Test
    void createWithoutNameReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"Descripción\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(projectService);
    }

    @Test
    void createWithNameLongerThan100Returns400() throws Exception {
        String name = String.join("", Collections.nCopies(101, "a"));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"description\":\"Descripción\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]")
                        .value("name: El nombre del proyecto no puede superar los 100 caracteres"));
        verifyNoInteractions(projectService);
    }

    @Test
    void createWithoutDescriptionReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Proyecto\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("description: La descripción del proyecto es obligatoria"));
        verifyNoInteractions(projectService);
    }

    @Test
    void createWithDescriptionLongerThan100Returns400() throws Exception {
        String description = String.join("", Collections.nCopies(101, "a"));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Proyecto\",\"description\":\"" + description + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]")
                        .value("description: La descripción del proyecto no puede superar los 100 caracteres"));
        verifyNoInteractions(projectService);
    }

    @Test
    void createWithInvalidStatusReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Proyecto\",\"description\":\"Descripción\",\"status\":\"DELETED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(projectService);
    }

    @Test
    void findByIdReturns200() throws Exception {
        when(projectService.findById(1L)).thenReturn(response(1L, "Proyecto", ProjectStatus.ACTIVE));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Proyecto"));
    }

    @Test
    void findByIdReturns404WhenProjectDoesNotExist() throws Exception {
        when(projectService.findById(99L)).thenThrow(new ProjectNotFoundException(99L));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No existe el proyecto con id 99"));
    }

    @Test
    void findByIdWithNonPositiveIdReturns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("El id del proyecto debe ser un número positivo"));
        verifyNoInteractions(projectService);
    }

    @Test
    void findByIdWithNonNumericIdReturns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/abc"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(projectService);
    }

    @Test
    void findAllReturns200WithEveryProject() throws Exception {
        when(projectService.findAll(null)).thenReturn(Arrays.asList(
                response(1L, "Uno", ProjectStatus.ACTIVE),
                response(2L, "Dos", ProjectStatus.INACTIVE)));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void findAllFiltersByStatus() throws Exception {
        when(projectService.findAll(ProjectStatus.ACTIVE))
                .thenReturn(Collections.singletonList(response(1L, "Uno", ProjectStatus.ACTIVE)));

        mockMvc.perform(get(BASE_URL).param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void findAllWithInvalidStatusReturns400() throws Exception {
        mockMvc.perform(get(BASE_URL).param("status", "DELETED"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(projectService);
    }

    @Test
    void updateReturns200() throws Exception {
        when(projectService.update(eq(1L), any(UpdateProjectRequest.class)))
                .thenReturn(response(1L, "Renombrado", ProjectStatus.INACTIVE));

        mockMvc.perform(put(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renombrado\",\"description\":\"Descripción\",\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renombrado"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void updateReturns404WhenProjectDoesNotExist() throws Exception {
        when(projectService.update(eq(99L), any(UpdateProjectRequest.class)))
                .thenThrow(new ProjectNotFoundException(99L));

        mockMvc.perform(put(BASE_URL + "/99").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renombrado\",\"description\":\"Descripción\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateIgnoresIdAndCreatedAtSentInBody() throws Exception {
        when(projectService.update(eq(1L), any(UpdateProjectRequest.class)))
                .thenReturn(response(1L, "Renombrado", ProjectStatus.ACTIVE));

        mockMvc.perform(put(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":500,\"createdAt\":\"2000-01-01T00:00:00\",\"name\":\"Renombrado\","
                                + "\"description\":\"Descripción\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.createdAt").value("2026-10-03T19:20:00"));

        verify(projectService).update(eq(1L), any(UpdateProjectRequest.class));
    }

    @Test
    void updateWithBlankNameReturns400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"description\":\"Descripción\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(projectService);
    }

    @Test
    void updateWithBlankDescriptionReturns400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renombrado\",\"description\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("description: La descripción del proyecto es obligatoria"));
        verifyNoInteractions(projectService);
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
        verify(projectService).delete(1L);
    }

    @Test
    void deleteReturns404WhenProjectDoesNotExist() throws Exception {
        doThrow(new ProjectNotFoundException(99L)).when(projectService).delete(99L);

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }

    private static ProjectResponse response(Long id, String name, ProjectStatus status) {
        return new ProjectResponse(id, name, "Descripción", status, CREATED_AT);
    }
}
