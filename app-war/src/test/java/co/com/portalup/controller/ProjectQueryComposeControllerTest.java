package co.com.portalup.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.portalup.dto.ComposedQueryResponse;
import co.com.portalup.dto.ProjectComposeResponse;
import co.com.portalup.entity.ProjectQueryStatus;
import co.com.portalup.entity.ProjectStatus;
import co.com.portalup.entity.QueryStatus;
import co.com.portalup.service.ProjectQueryService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectQueryComposeController.class)
class ProjectQueryComposeControllerTest {

    private static final String BASE_URL = "/api/v1/project-query-compose";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 3, 19, 20);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectQueryService projectQueryService;

    @Test
    void findAllReturnsProjectsWithTheirQueries() throws Exception {
        ComposedQueryResponse query = new ComposedQueryResponse(20L, "Consulta", "SELECT d.Id FROM Document d",
                "{}", QueryStatus.ACTIVE, CREATED_AT, 1L, "{\"page\":10}", ProjectQueryStatus.INACTIVE);
        when(projectQueryService.findAllComposed()).thenReturn(Arrays.asList(
                new ProjectComposeResponse(10L, "Proyecto", "Descripción", ProjectStatus.ACTIVE, CREATED_AT,
                        Collections.singletonList(query)),
                new ProjectComposeResponse(11L, "Otro", "Otra descripción", ProjectStatus.INACTIVE, CREATED_AT,
                        Collections.singletonList(query))));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Proyecto"))
                .andExpect(jsonPath("$[0].description").value("Descripción"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-10-03T19:20:00"))
                .andExpect(jsonPath("$[0].queries.length()").value(1))
                .andExpect(jsonPath("$[0].queries[0].id").value(20))
                .andExpect(jsonPath("$[0].queries[0].name").value("Consulta"))
                .andExpect(jsonPath("$[0].queries[0].cesql").value("SELECT d.Id FROM Document d"))
                .andExpect(jsonPath("$[0].queries[0].config").value("{}"))
                .andExpect(jsonPath("$[0].queries[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].queries[0].projectQueryId").value(1))
                .andExpect(jsonPath("$[0].queries[0].projectQueryConfig").value("{\"page\":10}"))
                .andExpect(jsonPath("$[0].queries[0].projectQueryStatus").value("INACTIVE"))
                .andExpect(jsonPath("$[1].id").value(11));
    }

    @Test
    void findAllWithoutAssociationsReturnsEmptyList() throws Exception {
        when(projectQueryService.findAllComposed()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
