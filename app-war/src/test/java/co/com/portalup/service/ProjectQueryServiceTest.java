package co.com.portalup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.portalup.dto.ComposedQueryResponse;
import co.com.portalup.dto.CreateProjectQueryRequest;
import co.com.portalup.dto.ProjectComposeResponse;
import co.com.portalup.dto.ProjectQueryResponse;
import co.com.portalup.dto.UpdateProjectQueryRequest;
import co.com.portalup.entity.ProjectEntity;
import co.com.portalup.entity.ProjectQueryEntity;
import co.com.portalup.entity.ProjectQueryStatus;
import co.com.portalup.entity.ProjectStatus;
import co.com.portalup.entity.QueryEntity;
import co.com.portalup.entity.QueryStatus;
import co.com.portalup.exception.DuplicateProjectQueryException;
import co.com.portalup.exception.ProjectNotFoundException;
import co.com.portalup.exception.ProjectQueryNotFoundException;
import co.com.portalup.exception.QueryNotFoundException;
import co.com.portalup.repository.ProjectQueryRepository;
import co.com.portalup.repository.ProjectQueryRow;
import co.com.portalup.repository.ProjectRepository;
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
class ProjectQueryServiceTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 3, 19, 20);
    private static final String CONFIG = "{\"columns\":[\"Id\"]}";

    @Mock
    private ProjectQueryRepository projectQueryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private QueryRepository queryRepository;

    @InjectMocks
    private ProjectQueryService projectQueryService;

    @Test
    void createSavesAssociationWithRequestedStatus() {
        referencesExist(10L, 20L);
        when(projectQueryRepository.save(any(ProjectQueryEntity.class)))
                .thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        ProjectQueryResponse response = projectQueryService.create(
                createRequest(10L, 20L, "  " + CONFIG + " ", ProjectQueryStatus.INACTIVE));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProjectId()).isEqualTo(10L);
        assertThat(response.getQueryId()).isEqualTo(20L);
        assertThat(response.getConfig()).isEqualTo(CONFIG);
        assertThat(response.getStatus()).isEqualTo(ProjectQueryStatus.INACTIVE);
        assertThat(response.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void createWithoutStatusDefaultsToActive() {
        referencesExist(10L, 20L);
        when(projectQueryRepository.save(any(ProjectQueryEntity.class)))
                .thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        ProjectQueryResponse response = projectQueryService.create(createRequest(10L, 20L, CONFIG, null));

        assertThat(response.getStatus()).isEqualTo(ProjectQueryStatus.ACTIVE);
    }

    @Test
    void createWithBlankConfigStoresNull() {
        referencesExist(10L, 20L);
        when(projectQueryRepository.save(any(ProjectQueryEntity.class)))
                .thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        ProjectQueryResponse response = projectQueryService.create(createRequest(10L, 20L, "   ", null));

        assertThat(response.getConfig()).isNull();
    }

    @Test
    void createThrowsWhenProjectDoesNotExist() {
        when(projectRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> projectQueryService.create(createRequest(10L, 20L, null, null)))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("10");
        verify(projectQueryRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenQueryDoesNotExist() {
        when(projectRepository.existsById(10L)).thenReturn(true);
        when(queryRepository.existsById(20L)).thenReturn(false);

        assertThatThrownBy(() -> projectQueryService.create(createRequest(10L, 20L, null, null)))
                .isInstanceOf(QueryNotFoundException.class)
                .hasMessageContaining("20");
        verify(projectQueryRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenAssociationAlreadyExists() {
        referencesExist(10L, 20L);
        when(projectQueryRepository.existsByProjectIdAndQueryId(10L, 20L)).thenReturn(true);

        assertThatThrownBy(() -> projectQueryService.create(createRequest(10L, 20L, null, null)))
                .isInstanceOf(DuplicateProjectQueryException.class)
                .hasMessage("La consulta con id 20 ya está asociada al proyecto con id 10");
        verify(projectQueryRepository, never()).save(any());
    }

    @Test
    void findByIdReturnsExistingAssociation() {
        when(projectQueryRepository.findById(1L))
                .thenReturn(Optional.of(projectQuery(1L, 10L, 20L, ProjectQueryStatus.ACTIVE)));

        ProjectQueryResponse response = projectQueryService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProjectId()).isEqualTo(10L);
        assertThat(response.getQueryId()).isEqualTo(20L);
        assertThat(response.getConfig()).isEqualTo(CONFIG);
    }

    @Test
    void findByIdThrowsWhenAssociationDoesNotExist() {
        when(projectQueryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectQueryService.findById(99L))
                .isInstanceOf(ProjectQueryNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAllDelegatesFiltersToRepository() {
        when(projectQueryRepository.search(10L, null, ProjectQueryStatus.ACTIVE)).thenReturn(Arrays.asList(
                projectQuery(1L, 10L, 20L, ProjectQueryStatus.ACTIVE),
                projectQuery(2L, 10L, 21L, ProjectQueryStatus.ACTIVE)));

        List<ProjectQueryResponse> response = projectQueryService.findAll(10L, null, ProjectQueryStatus.ACTIVE);

        assertThat(response).extracting(ProjectQueryResponse::getId).containsExactly(1L, 2L);
    }

    @Test
    void findAllWithoutFiltersReturnsEveryAssociation() {
        when(projectQueryRepository.search(null, null, null))
                .thenReturn(Collections.singletonList(projectQuery(1L, 10L, 20L, ProjectQueryStatus.ACTIVE)));

        List<ProjectQueryResponse> response = projectQueryService.findAll(null, null, null);

        assertThat(response).hasSize(1);
    }

    @Test
    void findAllComposedGroupsQueriesByProjectKeepingOrder() {
        ProjectEntity first = projectEntity(10L, "Primero");
        ProjectEntity second = projectEntity(11L, "Segundo");
        QueryEntity queryA = queryEntity(20L, "Consulta A");
        QueryEntity queryB = queryEntity(21L, "Consulta B");
        when(projectQueryRepository.findAllComposed()).thenReturn(Arrays.asList(
                new ProjectQueryRow(projectQuery(1L, 10L, 20L, ProjectQueryStatus.ACTIVE), first, queryA),
                new ProjectQueryRow(projectQuery(2L, 10L, 21L, ProjectQueryStatus.INACTIVE), first, queryB),
                new ProjectQueryRow(projectQuery(3L, 11L, 20L, ProjectQueryStatus.ACTIVE), second, queryA)));

        List<ProjectComposeResponse> response = projectQueryService.findAllComposed();

        assertThat(response).extracting(ProjectComposeResponse::getId).containsExactly(10L, 11L);
        assertThat(response.get(0).getName()).isEqualTo("Primero");
        assertThat(response.get(0).getDescription()).isEqualTo("Descripción de Primero");
        assertThat(response.get(0).getQueries()).extracting(ComposedQueryResponse::getId).containsExactly(20L, 21L);
        assertThat(response.get(1).getQueries()).extracting(ComposedQueryResponse::getProjectQueryId)
                .containsExactly(3L);

        ComposedQueryResponse composed = response.get(0).getQueries().get(1);
        assertThat(composed.getName()).isEqualTo("Consulta B");
        assertThat(composed.getCesql()).isEqualTo("SELECT d.Id FROM Document d");
        assertThat(composed.getConfig()).isEqualTo("{\"query\":true}");
        assertThat(composed.getStatus()).isEqualTo(QueryStatus.ACTIVE);
        assertThat(composed.getProjectQueryId()).isEqualTo(2L);
        assertThat(composed.getProjectQueryConfig()).isEqualTo(CONFIG);
        assertThat(composed.getProjectQueryStatus()).isEqualTo(ProjectQueryStatus.INACTIVE);
    }

    @Test
    void findAllComposedWithoutAssociationsReturnsEmptyList() {
        when(projectQueryRepository.findAllComposed()).thenReturn(Collections.emptyList());

        assertThat(projectQueryService.findAllComposed()).isEmpty();
    }

    @Test
    void updateChangesFieldsAndStatusKeepingIdAndCreatedAt() {
        ProjectQueryEntity existing = projectQuery(1L, 10L, 20L, ProjectQueryStatus.ACTIVE);
        when(projectQueryRepository.findById(1L)).thenReturn(Optional.of(existing));
        referencesExist(11L, 21L);
        when(projectQueryRepository.save(existing)).thenReturn(existing);

        ProjectQueryResponse response = projectQueryService.update(1L,
                updateRequest(11L, 21L, " {} ", ProjectQueryStatus.INACTIVE));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProjectId()).isEqualTo(11L);
        assertThat(response.getQueryId()).isEqualTo(21L);
        assertThat(response.getConfig()).isEqualTo("{}");
        assertThat(response.getStatus()).isEqualTo(ProjectQueryStatus.INACTIVE);
        assertThat(response.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void updateWithoutStatusKeepsCurrentStatus() {
        ProjectQueryEntity existing = projectQuery(1L, 10L, 20L, ProjectQueryStatus.INACTIVE);
        when(projectQueryRepository.findById(1L)).thenReturn(Optional.of(existing));
        referencesExist(10L, 20L);
        when(projectQueryRepository.save(existing)).thenReturn(existing);

        ProjectQueryResponse response = projectQueryService.update(1L, updateRequest(10L, 20L, CONFIG, null));

        assertThat(response.getStatus()).isEqualTo(ProjectQueryStatus.INACTIVE);
    }

    @Test
    void updateWithoutConfigClearsIt() {
        ProjectQueryEntity existing = projectQuery(1L, 10L, 20L, ProjectQueryStatus.ACTIVE);
        when(projectQueryRepository.findById(1L)).thenReturn(Optional.of(existing));
        referencesExist(10L, 20L);
        when(projectQueryRepository.save(existing)).thenReturn(existing);

        ProjectQueryResponse response = projectQueryService.update(1L, updateRequest(10L, 20L, null, null));

        assertThat(response.getConfig()).isNull();
    }

    @Test
    void updateThrowsWhenAssociationDoesNotExist() {
        when(projectQueryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectQueryService.update(99L, updateRequest(10L, 20L, null, null)))
                .isInstanceOf(ProjectQueryNotFoundException.class);
        verify(projectQueryRepository, never()).save(any());
    }

    @Test
    void updateThrowsWhenNewProjectDoesNotExist() {
        when(projectQueryRepository.findById(1L))
                .thenReturn(Optional.of(projectQuery(1L, 10L, 20L, ProjectQueryStatus.ACTIVE)));
        when(projectRepository.existsById(11L)).thenReturn(false);

        assertThatThrownBy(() -> projectQueryService.update(1L, updateRequest(11L, 20L, null, null)))
                .isInstanceOf(ProjectNotFoundException.class);
        verify(projectQueryRepository, never()).save(any());
    }

    @Test
    void updateThrowsWhenAnotherAssociationHasTheSamePair() {
        when(projectQueryRepository.findById(1L))
                .thenReturn(Optional.of(projectQuery(1L, 10L, 20L, ProjectQueryStatus.ACTIVE)));
        referencesExist(10L, 21L);
        when(projectQueryRepository.existsByProjectIdAndQueryIdAndIdNot(10L, 21L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> projectQueryService.update(1L, updateRequest(10L, 21L, null, null)))
                .isInstanceOf(DuplicateProjectQueryException.class);
        verify(projectQueryRepository, never()).save(any());
    }

    @Test
    void updatePersistsTheSameEntityWithoutChangingIdOrCreatedAt() {
        ProjectQueryEntity existing = projectQuery(1L, 10L, 20L, ProjectQueryStatus.ACTIVE);
        when(projectQueryRepository.findById(1L)).thenReturn(Optional.of(existing));
        referencesExist(10L, 20L);
        when(projectQueryRepository.save(existing)).thenReturn(existing);

        projectQueryService.update(1L, updateRequest(10L, 20L, CONFIG, null));

        ArgumentCaptor<ProjectQueryEntity> captor = ArgumentCaptor.forClass(ProjectQueryEntity.class);
        verify(projectQueryRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void deleteRemovesExistingAssociation() {
        ProjectQueryEntity existing = projectQuery(1L, 10L, 20L, ProjectQueryStatus.ACTIVE);
        when(projectQueryRepository.findById(1L)).thenReturn(Optional.of(existing));

        projectQueryService.delete(1L);

        verify(projectQueryRepository).delete(existing);
    }

    @Test
    void deleteThrowsWhenAssociationDoesNotExist() {
        when(projectQueryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectQueryService.delete(99L))
                .isInstanceOf(ProjectQueryNotFoundException.class);
        verify(projectQueryRepository, never()).delete(any());
    }

    private void referencesExist(Long projectId, Long queryId) {
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(queryRepository.existsById(queryId)).thenReturn(true);
    }

    private static ProjectQueryEntity persisted(ProjectQueryEntity entity) {
        ReflectionTestUtils.setField(entity, "id", 1L);
        ReflectionTestUtils.setField(entity, "createdAt", CREATED_AT);
        return entity;
    }

    private static ProjectQueryEntity projectQuery(Long id, Long projectId, Long queryId, ProjectQueryStatus status) {
        ProjectQueryEntity entity = new ProjectQueryEntity();
        entity.setProjectId(projectId);
        entity.setQueryId(queryId);
        entity.setConfig(CONFIG);
        entity.setStatus(status);
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "createdAt", CREATED_AT);
        return entity;
    }

    private static ProjectEntity projectEntity(Long id, String name) {
        ProjectEntity entity = new ProjectEntity();
        entity.setName(name);
        entity.setDescription("Descripción de " + name);
        entity.setStatus(ProjectStatus.ACTIVE);
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "createdAt", CREATED_AT);
        return entity;
    }

    private static QueryEntity queryEntity(Long id, String name) {
        QueryEntity entity = new QueryEntity();
        entity.setName(name);
        entity.setCesql("SELECT d.Id FROM Document d");
        entity.setConfig("{\"query\":true}");
        entity.setStatus(QueryStatus.ACTIVE);
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "createdAt", CREATED_AT);
        return entity;
    }

    private static CreateProjectQueryRequest createRequest(Long projectId, Long queryId, String config,
                                                           ProjectQueryStatus status) {
        CreateProjectQueryRequest request = new CreateProjectQueryRequest();
        request.setProjectId(projectId);
        request.setQueryId(queryId);
        request.setConfig(config);
        request.setStatus(status);
        return request;
    }

    private static UpdateProjectQueryRequest updateRequest(Long projectId, Long queryId, String config,
                                                           ProjectQueryStatus status) {
        UpdateProjectQueryRequest request = new UpdateProjectQueryRequest();
        request.setProjectId(projectId);
        request.setQueryId(queryId);
        request.setConfig(config);
        request.setStatus(status);
        return request;
    }
}
