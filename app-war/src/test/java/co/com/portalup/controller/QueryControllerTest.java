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

import co.com.portalup.dto.CreateQueryRequest;
import co.com.portalup.dto.QueryResponse;
import co.com.portalup.dto.UpdateQueryRequest;
import co.com.portalup.entity.QueryStatus;
import co.com.portalup.exception.QueryNotFoundException;
import co.com.portalup.exception.ResourceInUseException;
import co.com.portalup.service.QueryService;
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

@WebMvcTest(QueryController.class)
class QueryControllerTest {

    private static final String BASE_URL = "/api/v1/query";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 3, 19, 20);
    private static final String CESQL = "SELECT d.Id FROM Document d";
    private static final String CONFIG = "{}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueryService queryService;

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        when(queryService.create(any(CreateQueryRequest.class)))
                .thenReturn(response(1L, "Consulta ejemplo", QueryStatus.ACTIVE));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Consulta ejemplo", CESQL, CONFIG, "ACTIVE")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/query/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Consulta ejemplo"))
                .andExpect(jsonPath("$.cesql").value(CESQL))
                .andExpect(jsonPath("$.config").value(CONFIG))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").value("2026-10-03T19:20:00"));
    }

    @Test
    void createWithoutStatusPassesNullStatusToService() throws Exception {
        when(queryService.create(any(CreateQueryRequest.class)))
                .thenReturn(response(1L, "Consulta", QueryStatus.ACTIVE));

        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Consulta", CESQL, CONFIG, null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        ArgumentCaptor<CreateQueryRequest> captor = ArgumentCaptor.forClass(CreateQueryRequest.class);
        verify(queryService).create(captor.capture());
        assertThat(captor.getValue().getStatus()).isNull();
    }

    @Test
    void createWithBlankNameReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("   ", CESQL, CONFIG, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value(BASE_URL))
                .andExpect(jsonPath("$.details[0]").value("name: El nombre de la consulta es obligatorio"));
        verifyNoInteractions(queryService);
    }

    @Test
    void createWithoutCesqlReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Consulta", null, CONFIG, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("cesql: La sentencia CE SQL de la consulta es obligatoria"));
        verifyNoInteractions(queryService);
    }

    @Test
    void createWithoutConfigReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Consulta", CESQL, null, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("config: La configuración de la consulta es obligatoria"));
        verifyNoInteractions(queryService);
    }

    @Test
    void createWithNameLongerThan100Returns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body(repeat(101), CESQL, CONFIG, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]")
                        .value("name: El nombre de la consulta no puede superar los 100 caracteres"));
        verifyNoInteractions(queryService);
    }

    @Test
    void createWithCesqlLongerThan512Returns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Consulta", repeat(513), CONFIG, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]")
                        .value("cesql: La sentencia CE SQL de la consulta no puede superar los 512 caracteres"));
        verifyNoInteractions(queryService);
    }

    @Test
    void createWithConfigLongerThan1024Returns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Consulta", CESQL, repeat(1025), null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]")
                        .value("config: La configuración de la consulta no puede superar los 1024 caracteres"));
        verifyNoInteractions(queryService);
    }

    @Test
    void createWithInvalidStatusReturns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Consulta", CESQL, CONFIG, "DELETED")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(queryService);
    }

    @Test
    void findByIdReturns200() throws Exception {
        when(queryService.findById(1L)).thenReturn(response(1L, "Consulta", QueryStatus.ACTIVE));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Consulta"));
    }

    @Test
    void findByIdReturns404WhenQueryDoesNotExist() throws Exception {
        when(queryService.findById(99L)).thenThrow(new QueryNotFoundException(99L));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No existe la consulta con id 99"));
    }

    @Test
    void findByIdWithNonPositiveIdReturns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("El id de la consulta debe ser un número positivo"));
        verifyNoInteractions(queryService);
    }

    @Test
    void findByIdWithNonNumericIdReturns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/abc"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(queryService);
    }

    @Test
    void findAllReturns200WithEveryQuery() throws Exception {
        when(queryService.findAll(null)).thenReturn(Arrays.asList(
                response(1L, "Uno", QueryStatus.ACTIVE),
                response(2L, "Dos", QueryStatus.INACTIVE)));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void findAllFiltersByStatus() throws Exception {
        when(queryService.findAll(QueryStatus.ACTIVE))
                .thenReturn(Collections.singletonList(response(1L, "Uno", QueryStatus.ACTIVE)));

        mockMvc.perform(get(BASE_URL).param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void findAllWithInvalidStatusReturns400() throws Exception {
        mockMvc.perform(get(BASE_URL).param("status", "DELETED"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(queryService);
    }

    @Test
    void updateReturns200() throws Exception {
        when(queryService.update(eq(1L), any(UpdateQueryRequest.class)))
                .thenReturn(response(1L, "Renombrada", QueryStatus.INACTIVE));

        mockMvc.perform(put(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Renombrada", CESQL, CONFIG, "INACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renombrada"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void updateReturns404WhenQueryDoesNotExist() throws Exception {
        when(queryService.update(eq(99L), any(UpdateQueryRequest.class)))
                .thenThrow(new QueryNotFoundException(99L));

        mockMvc.perform(put(BASE_URL + "/99").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Renombrada", CESQL, CONFIG, null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateIgnoresIdAndCreatedAtSentInBody() throws Exception {
        when(queryService.update(eq(1L), any(UpdateQueryRequest.class)))
                .thenReturn(response(1L, "Renombrada", QueryStatus.ACTIVE));

        mockMvc.perform(put(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":500,\"createdAt\":\"2000-01-01T00:00:00\",\"name\":\"Renombrada\","
                                + "\"cesql\":\"" + CESQL + "\",\"config\":\"" + CONFIG + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.createdAt").value("2026-10-03T19:20:00"));

        verify(queryService).update(eq(1L), any(UpdateQueryRequest.class));
    }

    @Test
    void updateWithBlankNameReturns400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body("", CESQL, CONFIG, null)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(queryService);
    }

    @Test
    void updateWithBlankCesqlReturns400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Renombrada", "  ", CONFIG, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("cesql: La sentencia CE SQL de la consulta es obligatoria"));
        verifyNoInteractions(queryService);
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
        verify(queryService).delete(1L);
    }

    @Test
    void deleteReturns409WhenQueryHasAssociations() throws Exception {
        doThrow(new ResourceInUseException("No se puede eliminar la consulta con id 1 porque está asociada a uno o más proyectos")).when(queryService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("No se puede eliminar la consulta con id 1 porque está asociada a uno o más proyectos"));
    }

    @Test
    void deleteReturns404WhenQueryDoesNotExist() throws Exception {
        doThrow(new QueryNotFoundException(99L)).when(queryService).delete(99L);

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }

    private static QueryResponse response(Long id, String name, QueryStatus status) {
        return new QueryResponse(id, name, CESQL, CONFIG, status, CREATED_AT);
    }

    /** Arma el JSON del request omitiendo los campos null. */
    private static String body(String name, String cesql, String config, String status) {
        StringBuilder json = new StringBuilder("{");
        appendField(json, "name", name);
        appendField(json, "cesql", cesql);
        appendField(json, "config", config);
        appendField(json, "status", status);
        return json.append('}').toString();
    }

    private static void appendField(StringBuilder json, String field, String value) {
        if (value == null) {
            return;
        }
        if (json.length() > 1) {
            json.append(',');
        }
        json.append('"').append(field).append("\":\"").append(value).append('"');
    }

    private static String repeat(int times) {
        return String.join("", Collections.nCopies(times, "a"));
    }
}
