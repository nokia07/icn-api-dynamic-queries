package co.com.portalup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.portalup.dto.CreateQueryRequest;
import co.com.portalup.dto.QueryResponse;
import co.com.portalup.dto.UpdateQueryRequest;
import co.com.portalup.entity.QueryEntity;
import co.com.portalup.entity.QueryStatus;
import co.com.portalup.exception.QueryNotFoundException;
import co.com.portalup.exception.ResourceInUseException;
import co.com.portalup.repository.ProjectQueryRepository;
import co.com.portalup.repository.QueryRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QueryServiceTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 3, 19, 20);
    private static final String CESQL = "SELECT d.Id FROM Document d WHERE d.IsCurrentVersion = TRUE";
    private static final String CONFIG = "{\"columns\":[\"Id\"]}";

    @Mock
    private QueryRepository queryRepository;

    @Mock
    private ProjectQueryRepository projectQueryRepository;

    @InjectMocks
    private QueryService queryService;

    @Test
    void createSavesQueryWithRequestedStatus() {
        when(queryRepository.save(any(QueryEntity.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        QueryResponse response = queryService.create(
                createRequest("  Consulta ejemplo  ", " " + CESQL + "  ", "  " + CONFIG + " ", QueryStatus.INACTIVE));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Consulta ejemplo");
        assertThat(response.getCesql()).isEqualTo(CESQL);
        assertThat(response.getConfig()).isEqualTo(CONFIG);
        assertThat(response.getStatus()).isEqualTo(QueryStatus.INACTIVE);
        assertThat(response.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void createWithoutStatusDefaultsToActive() {
        when(queryRepository.save(any(QueryEntity.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        QueryResponse response = queryService.create(createRequest("Consulta ejemplo", CESQL, CONFIG, null));

        assertThat(response.getStatus()).isEqualTo(QueryStatus.ACTIVE);
    }

    @Test
    void findByIdReturnsExistingQuery() {
        when(queryRepository.findById(1L)).thenReturn(Optional.of(query(1L, "Consulta", QueryStatus.ACTIVE)));

        QueryResponse response = queryService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Consulta");
        assertThat(response.getCesql()).isEqualTo(CESQL);
        assertThat(response.getConfig()).isEqualTo(CONFIG);
    }

    @Test
    void findByIdThrowsWhenQueryDoesNotExist() {
        when(queryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.findById(99L))
                .isInstanceOf(QueryNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAllWithoutStatusReturnsEveryQuery() {
        when(queryRepository.findAll()).thenReturn(Arrays.asList(
                query(1L, "Uno", QueryStatus.ACTIVE),
                query(2L, "Dos", QueryStatus.INACTIVE)));

        List<QueryResponse> response = queryService.findAll(null);

        assertThat(response).extracting(QueryResponse::getId).containsExactly(1L, 2L);
        verify(queryRepository, never()).findByStatus(any());
    }

    @Test
    void findAllWithStatusFiltersByStatus() {
        when(queryRepository.findByStatus(QueryStatus.INACTIVE))
                .thenReturn(Collections.singletonList(query(2L, "Dos", QueryStatus.INACTIVE)));

        List<QueryResponse> response = queryService.findAll(QueryStatus.INACTIVE);

        assertThat(response).extracting(QueryResponse::getStatus).containsExactly(QueryStatus.INACTIVE);
        verify(queryRepository, never()).findAll();
    }

    @Test
    void updateChangesFieldsAndStatusKeepingIdAndCreatedAt() {
        QueryEntity existing = query(1L, "Original", QueryStatus.ACTIVE);
        when(queryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(queryRepository.save(existing)).thenReturn(existing);

        QueryResponse response = queryService.update(1L,
                updateRequest("  Renombrada ", " SELECT d.Id FROM Document d ", " {} ", QueryStatus.INACTIVE));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Renombrada");
        assertThat(response.getCesql()).isEqualTo("SELECT d.Id FROM Document d");
        assertThat(response.getConfig()).isEqualTo("{}");
        assertThat(response.getStatus()).isEqualTo(QueryStatus.INACTIVE);
        assertThat(response.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void updateWithoutStatusKeepsCurrentStatus() {
        QueryEntity existing = query(1L, "Original", QueryStatus.INACTIVE);
        when(queryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(queryRepository.save(existing)).thenReturn(existing);

        QueryResponse response = queryService.update(1L, updateRequest("Renombrada", CESQL, CONFIG, null));

        assertThat(response.getStatus()).isEqualTo(QueryStatus.INACTIVE);
    }

    @Test
    void updateThrowsWhenQueryDoesNotExist() {
        when(queryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.update(99L, updateRequest("Nombre", CESQL, CONFIG, null)))
                .isInstanceOf(QueryNotFoundException.class);
        verify(queryRepository, never()).save(any());
    }

    @Test
    void updatePersistsTheSameEntityWithoutChangingIdOrCreatedAt() {
        QueryEntity existing = query(1L, "Original", QueryStatus.ACTIVE);
        when(queryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(queryRepository.save(existing)).thenReturn(existing);

        queryService.update(1L, updateRequest("Renombrada", CESQL, CONFIG, null));

        ArgumentCaptor<QueryEntity> captor = ArgumentCaptor.forClass(QueryEntity.class);
        verify(queryRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void deleteRemovesExistingQuery() {
        QueryEntity existing = query(1L, "Consulta", QueryStatus.ACTIVE);
        when(queryRepository.findById(1L)).thenReturn(Optional.of(existing));

        queryService.delete(1L);

        verify(queryRepository).delete(existing);
    }

    @Test
    void deleteThrowsWhenQueryHasAssociations() {
        QueryEntity existing = query(1L, "Con asociaciones", QueryStatus.ACTIVE);
        when(queryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectQueryRepository.existsByQueryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> queryService.delete(1L))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessage("No se puede eliminar la consulta con id 1 porque está asociada a uno o más proyectos");
        verify(queryRepository, never()).delete(any());
    }

    @Test
    void deleteThrowsWhenQueryDoesNotExist() {
        when(queryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.delete(99L)).isInstanceOf(QueryNotFoundException.class);
        verify(queryRepository, never()).delete(any());
    }

    private static QueryEntity persisted(QueryEntity entity) {
        ReflectionTestUtils.setField(entity, "id", 1L);
        ReflectionTestUtils.setField(entity, "createdAt", CREATED_AT);
        return entity;
    }

    private static QueryEntity query(Long id, String name, QueryStatus status) {
        QueryEntity entity = new QueryEntity();
        entity.setName(name);
        entity.setCesql(CESQL);
        entity.setConfig(CONFIG);
        entity.setStatus(status);
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "createdAt", CREATED_AT);
        return entity;
    }

    private static CreateQueryRequest createRequest(String name, String cesql, String config, QueryStatus status) {
        CreateQueryRequest request = new CreateQueryRequest();
        request.setName(name);
        request.setCesql(cesql);
        request.setConfig(config);
        request.setStatus(status);
        return request;
    }

    private static UpdateQueryRequest updateRequest(String name, String cesql, String config, QueryStatus status) {
        UpdateQueryRequest request = new UpdateQueryRequest();
        request.setName(name);
        request.setCesql(cesql);
        request.setConfig(config);
        request.setStatus(status);
        return request;
    }
}
