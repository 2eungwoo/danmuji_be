import com.back2basics.global.cache.DashboardCacheService;
import com.back2basics.history.model.DomainType;
import com.back2basics.history.service.HistoryLogService;
import com.back2basics.infra.validator.ProjectValidator;
import com.back2basics.infra.validator.UserValidator;
import com.back2basics.project.model.Project;
import com.back2basics.project.port.in.UpdateProjectUseCase;
import com.back2basics.project.port.in.command.ProjectUpdateCommand;
import com.back2basics.project.port.out.SaveProjectPort;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateProjectService implements UpdateProjectUseCase {

    private final ProjectValidator projectValidator;
    private final UserValidator userValidator;
    private final SaveProjectPort saveProjectPort;
    private final HistoryLogService historyLogService;
    private final DashboardCacheService dashboardCacheService;

    @Override
    @Transactional
    public void updateProject(Long projectId, ProjectUpdateCommand command, Long loggedInUserId) {
        userValidator.checkAdmin(loggedInUserId);
        Project project = projectValidator.findProject(projectId);

        historyLogService.logUpdated(DomainType.PROJECT, loggedInUserId, project, command,
            "프로젝트 정보 변경");

        project.update(command);
        dashboardCacheService.incrementVersion(null, "projectStatusCount");
    }

    @Override
    @Transactional
    public void updateToDone(Long projectId, Long loggedInUserId) {
        userValidator.checkAdmin(loggedInUserId);
        Project project = projectValidator.findProject(projectId);
        project.updateToDone();
        historyLogService.logUpdated(DomainType.PROJECT, loggedInUserId, project, project,
            "프로젝트 상태를 완료로 변경");
        dashboardCacheService.incrementVersion(null, "projectStatusCount");
    }

    @Override
    @Transactional
    public void updateToHold(Long projectId, Long loggedInUserId) {
        userValidator.checkAdmin(loggedInUserId);
        Project project = projectValidator.findProject(projectId);
        project.updateToHold();
        historyLogService.logUpdated(DomainType.PROJECT, loggedInUserId, project, project,
            "프로젝트 상태를 보류로 변경");
        dashboardCacheService.incrementVersion(null, "projectStatusCount");
    }

    @Override
    @Transactional
    public void updateDueSoonAndDelayedProjects() {
        List<Project> projects = saveProjectPort.updateAllProjectsToDueSoonOrDelayed();
        projects.forEach(p -> saveProjectPort.save(p));
        dashboardCacheService.incrementVersion(null, "projectStatusCount");
    }
}