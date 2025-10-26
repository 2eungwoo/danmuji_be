import com.back2basics.global.cache.DashboardCacheService;
import com.back2basics.history.model.DomainType;
import com.back2basics.history.service.HistoryLogService;
import com.back2basics.infra.validator.ProjectValidator;
import com.back2basics.infra.validator.UserValidator;
import com.back2basics.project.model.Project;
import com.back2basics.project.model.ProjectStatus;
import com.back2basics.project.port.in.UpdateProjectUseCase;
import com.back2basics.project.port.in.command.ProjectUpdateCommand;
import com.back2basics.project.port.out.ReadProjectPort;
import com.back2basics.project.port.out.SaveProjectPort;
import com.back2basics.projectstep.model.ProjectStep;
import com.back2basics.projectstep.model.ProjectStepStatus;
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
    private final ReadProjectPort readProjectPort;

    @Override
    @Transactional
    public void updateProject(Long projectId, ProjectUpdateCommand command, Long loggedInUserId) {
        userValidator.checkAdmin(loggedInUserId);
        Project project = projectValidator.findById(projectId);

        historyLogService.logUpdated(DomainType.PROJECT, loggedInUserId, project, Project.fromUpdateCommand(command),
            "프로젝트 정보 변경");

        project.update(command);
        dashboardCacheService.incrementVersion(null, "projectStatusCount");
    }

    @Override
    @Transactional
    public void changedStatus(Long projectId, Long loggedInUserId) {
        userValidator.checkAdmin(loggedInUserId);
        Project project = projectValidator.findById(projectId);

        project.updateStatus(ProjectStatus.IN_PROGRESS);
        historyLogService.logUpdated(DomainType.PROJECT, loggedInUserId, project, project,
            "프로젝트 상태 변경");
        dashboardCacheService.incrementVersion(null, "projectStatusCount");
    }

    @Override
    public void calculateProgressRate(Long projectId) {
        Project project = projectValidator.findById(projectId);
        List<ProjectStep> activeSteps = project.getSteps().stream()
            .filter(step -> !step.isDeleted())
            .toList();
        int totalSteps = activeSteps.size();
        long completedSteps = activeSteps.stream()
            .filter(step -> step.getProjectStepStatus() == ProjectStepStatus.COMPLETED)
            .count();

        if (totalSteps > 0) {
            project.calculateProgress(totalSteps, (int) completedSteps);
            saveProjectPort.save(project);
        }
    }

    @Override
    public void calculateProgressRateByDeleteStep(Long projectId) {
        calculateProgressRate(projectId);
    }

    @Transactional
    public void updateToDone(Long projectId, Long loggedInUserId) {
        userValidator.checkAdmin(loggedInUserId);
        Project project = projectValidator.findById(projectId);
        project.updateStatus(ProjectStatus.COMPLETED);
        historyLogService.logUpdated(DomainType.PROJECT, loggedInUserId, project, project,
            "프로젝트 상태를 완료로 변경");
        dashboardCacheService.incrementVersion(null, "projectStatusCount");
    }

    @Transactional
    public void updateToHold(Long projectId, Long loggedInUserId) {
        userValidator.checkAdmin(loggedInUserId);
        Project project = projectValidator.findById(projectId);
        project.updateStatus(ProjectStatus.HOLD);
        historyLogService.logUpdated(DomainType.PROJECT, loggedInUserId, project, project,
            "프로젝트 상태를 보류로 변경");
        dashboardCacheService.incrementVersion(null, "projectStatusCount");
    }

    @Override
    @Transactional
    public void updateDueSoonAndDelayedProjects() {
        List<Project> projects = readProjectPort.findUpdatableProjects();
        projects.forEach(p -> saveProjectPort.save(p));
        dashboardCacheService.incrementVersion(null, "projectStatusCount");
    }
}