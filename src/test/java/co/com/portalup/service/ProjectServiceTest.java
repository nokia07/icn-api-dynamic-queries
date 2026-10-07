package co.com.portalup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.portalup.dto.CreateProjectRequest;
import co.com.portalup.dto.ProjectResponse;
import co.com.portalup.dto.UpdateProjectRequest;
import co.com.portalup.entity.ProjectEntity;
import co.com.portalup.entity.ProjectStatus;
import co.com.portalup.exception.ProjectNotFoundException;
import co.com.portalup.exception.ResourceInUseException;
import co.com.portalup.repository.ProjectQueryRepository;
import co.com.portalup.repository.ProjectRepository;
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
class ProjectServiceTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 3, 19, 20);

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectQueryRepository projectQueryRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createSavesProjectWithRequestedStatus() {
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        ProjectResponse response = projectService.create(
                createRequest("  Proyecto ejemplo  ", "  Descripción ejemplo ", ProjectStatus.INACTIVE));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Proyecto ejemplo");
        assertThat(response.getDescription()).isEqualTo("Descripción ejemplo");
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.INACTIVE);
        assertThat(response.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void createWithoutStatusDefaultsToActive() {
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        ProjectResponse response = projectService.create(createRequest("Proyecto ejemplo", "Descripción", null));

        assertThat(response.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    void findByIdReturnsExistingProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L, "Proyecto", ProjectStatus.ACTIVE)));

        ProjectResponse response = projectService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Proyecto");
        assertThat(response.getDescription()).isEqualTo("Descripción de Proyecto");
    }

    @Test
    void findByIdThrowsWhenProjectDoesNotExist() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(99L))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAllWithoutStatusReturnsEveryProject() {
        when(projectRepository.findAll()).thenReturn(Arrays.asList(
                project(1L, "Uno", ProjectStatus.ACTIVE),
                project(2L, "Dos", ProjectStatus.INACTIVE)));

        List<ProjectResponse> response = projectService.findAll(null);

        assertThat(response).extracting(ProjectResponse::getId).containsExactly(1L, 2L);
        verify(projectRepository, never()).findByStatus(any());
    }

    @Test
    void findAllWithStatusFiltersByStatus() {
        when(projectRepository.findByStatus(ProjectStatus.INACTIVE))
                .thenReturn(Collections.singletonList(project(2L, "Dos", ProjectStatus.INACTIVE)));

        List<ProjectResponse> response = projectService.findAll(ProjectStatus.INACTIVE);

        assertThat(response).extracting(ProjectResponse::getStatus).containsExactly(ProjectStatus.INACTIVE);
        verify(projectRepository, never()).findAll();
    }

    @Test
    void updateChangesNameAndStatusKeepingIdAndCreatedAt() {
        ProjectEntity existing = project(1L, "Original", ProjectStatus.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(existing);

        ProjectResponse response = projectService.update(1L,
                updateRequest("  Renombrado ", " Nueva descripción  ", ProjectStatus.INACTIVE));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Renombrado");
        assertThat(response.getDescription()).isEqualTo("Nueva descripción");
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.INACTIVE);
        assertThat(response.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void updateWithoutStatusKeepsCurrentStatus() {
        ProjectEntity existing = project(1L, "Original", ProjectStatus.INACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(existing);

        ProjectResponse response = projectService.update(1L, updateRequest("Renombrado", "Descripción", null));

        assertThat(response.getStatus()).isEqualTo(ProjectStatus.INACTIVE);
    }

    @Test
    void updateThrowsWhenProjectDoesNotExist() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.update(99L, updateRequest("Nombre", "Descripción", null)))
                .isInstanceOf(ProjectNotFoundException.class);
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updatePersistsTheSameEntityWithoutChangingIdOrCreatedAt() {
        ProjectEntity existing = project(1L, "Original", ProjectStatus.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(existing);

        projectService.update(1L, updateRequest("Renombrado", "Descripción", null));

        ArgumentCaptor<ProjectEntity> captor = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(projectRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void deleteRemovesExistingProject() {
        ProjectEntity existing = project(1L, "Proyecto", ProjectStatus.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));

        projectService.delete(1L);

        verify(projectRepository).delete(existing);
    }

    @Test
    void deleteThrowsWhenProjectHasAssociations() {
        ProjectEntity existing = project(1L, "Con asociaciones", ProjectStatus.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectQueryRepository.existsByProjectId(1L)).thenReturn(true);

        assertThatThrownBy(() -> projectService.delete(1L))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessage("No se puede eliminar el proyecto con id 1 porque tiene consultas asociadas");
        verify(projectRepository, never()).delete(any());
    }

    @Test
    void deleteThrowsWhenProjectDoesNotExist() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(99L)).isInstanceOf(ProjectNotFoundException.class);
        verify(projectRepository, never()).delete(any());
    }

    private static ProjectEntity persisted(ProjectEntity entity) {
        ReflectionTestUtils.setField(entity, "id", 1L);
        ReflectionTestUtils.setField(entity, "createdAt", CREATED_AT);
        return entity;
    }

    private static ProjectEntity project(Long id, String name, ProjectStatus status) {
        ProjectEntity entity = new ProjectEntity();
        entity.setName(name);
        entity.setDescription("Descripción de " + name);
        entity.setStatus(status);
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "createdAt", CREATED_AT);
        return entity;
    }

    private static CreateProjectRequest createRequest(String name, String description, ProjectStatus status) {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName(name);
        request.setDescription(description);
        request.setStatus(status);
        return request;
    }

    private static UpdateProjectRequest updateRequest(String name, String description, ProjectStatus status) {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName(name);
        request.setDescription(description);
        request.setStatus(status);
        return request;
    }
}
