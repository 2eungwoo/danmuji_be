package com.back2basics.project.service;

import com.back2basics.assignment.model.Assignment;
import com.back2basics.assignment.service.notification.AssignmentNotificationSender;
import com.back2basics.history.model.DomainType;
import com.back2basics.history.service.HistoryLogService;
import com.back2basics.infra.validator.UserValidator;
import com.back2basics.project.model.Project;
import com.back2basics.project.port.in.CreateProjectUseCase;
import com.back2basics.project.port.in.command.ProjectCreateCommand;
import com.back2basics.project.port.out.SaveProjectPort;
import com.back2basics.project.port.out.SaveProjectUserPort;
import com.back2basics.projectstep.model.ProjectStep;
import com.back2basics.projectstep.model.ProjectStepStatus;
import com.back2basics.projectstep.port.out.SaveProjectStepPort;
import com.back2basics.user.model.User;
import com.back2basics.user.port.out.UserQueryPort;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateProjectService implements CreateProjectUseCase {

    private final SaveProjectPort saveProjectPort;
    private final SaveProjectStepPort saveProjectStepPort;
    private final SaveProjectUserPort saveProjectUserPort;
    private final UserQueryPort userQueryPort;
    private final UserValidator userValidator;
    private final AssignmentNotificationSender assignmentNotificationSender;
    private final HistoryLogService historyLogService;

    private static final List<String> DEFAULT_STEPS =
        List.of("계약", "요구사항 정의", "화면설계", "개발", "검수", "수금");

    @Override
    @Transactional
    @CacheEvict(value = "dashboard:projectStatusCount", allEntries = true)
    public void createProject(ProjectCreateCommand command, Long loggedInUserId) {

        userValidator.checkAdmin(loggedInUserId);
        Project project = Project.create(command);
        Project savedProject = saveProjectPort.createSave(project);
        historyLogService.logCreated(DomainType.PROJECT, loggedInUserId, savedProject,
            "프로젝트 신규 등록");

        createDefaultSteps(savedProject.getId());
        assignUsers(savedProject, command);
    }

    private void createDefaultSteps(Long projectId) {
        List<String> defaultSteps = DEFAULT_STEPS;

        for (int i = 0; i < defaultSteps.size(); i++) {
            ProjectStepStatus projectStepStatus =
                (i == 0) ? ProjectStepStatus.IN_PROGRESS : ProjectStepStatus.PENDING;
            ProjectStep step = ProjectStep.create(
                projectId,
                defaultSteps.get(i),
                i + 1,
                projectStepStatus
            );
            saveProjectStepPort.save(step);
        }
    }

    //todo: hotfix
    private void assignUsers(Project project, ProjectCreateCommand command) {
        List<User> clientUsers = userQueryPort.findByIds(command.getClientUserId());
        List<User> devUsers = userQueryPort.findByIds(command.getDevUserId());

        List<Assignment> assignments = Assignment.createProjectUser(project,
            command.getDevManagerId(), command.getClientManagerId(), command.getDevUserId(),
            command.getClientUserId(), devUsers, clientUsers);
        saveProjectUserPort.saveAll(assignments);

        // 알림을 위한 assignments id 리스트
        List<Long> assignmentIds = assignments.stream()
            .map(Assignment::getUserId).toList();
        assignmentNotificationSender.sendNotification(assignmentIds, project.getId());
    }
}
