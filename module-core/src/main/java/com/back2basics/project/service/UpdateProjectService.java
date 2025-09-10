package com.back2basics.project.service;

import com.back2basics.assignment.model.Assignment;
import com.back2basics.assignment.port.out.AssignmentQueryPort;
import com.back2basics.assignment.port.out.DeleteAssignmentPort;
import com.back2basics.assignment.service.notification.AssignmentNotificationSender;
import com.back2basics.company.model.CompanyType;
import com.back2basics.history.model.DomainType;
import com.back2basics.history.service.HistoryLogService;
import com.back2basics.infra.validator.ProjectValidator;
import com.back2basics.infra.validator.UserValidator;
import com.back2basics.project.model.Project;
import com.back2basics.project.model.ProjectStatus;
import com.back2basics.project.port.in.UpdateProjectUseCase;
import com.back2basics.project.port.in.command.ProjectUpdateCommand;
import com.back2basics.project.port.out.ReadProjectPort;
import com.back2basics.project.port.out.SaveProjectUserPort;
import com.back2basics.project.port.out.UpdateProjectPort;
import com.back2basics.projectstep.port.out.ReadProjectStepPort;
import com.back2basics.user.model.User;
import com.back2basics.user.model.UserType;
import com.back2basics.user.port.out.UserQueryPort;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateProjectService implements UpdateProjectUseCase {

    private final ProjectValidator projectValidator;
    private final UpdateProjectPort updateProjectPort;
    private final AssignmentQueryPort assignmentQueryPort;
    private final SaveProjectUserPort saveProjectUserPort;
    private final UserQueryPort userQueryPort;
    private final UserValidator userValidator;
    private final DeleteAssignmentPort deleteAssignmentPort;
    private final AssignmentNotificationSender assignmentNotificationSender;
    private final HistoryLogService historyLogService;

    private final ReadProjectPort readProjectPort;
    private final ReadProjectStepPort readProjectStepPort;

    //todo: 리팩토링 . 편의 메서드
    // PR 작성 : 수정 시 새로 등록된 유저의 경우 updateService 에서 create 해주는 게 맞는건지 의문
    @Override
    @Transactional
    @CacheEvict(value = "dashboard:projectStatusCount", allEntries = true)
    public void updateProject(Long projectId,
        ProjectUpdateCommand command, Long loggedInUserId) {
        userValidator.checkAdmin(loggedInUserId);
        Project project = projectValidator.findById(projectId);
        Project before = Project.copyOf(project);
        project.update(command);

        updateProjectPort.update(project);

        // 업체별 기존 assignments
        List<Assignment> oldDevs = assignmentQueryPort.findAllByProjectId(projectId)
            .stream().filter(assignment -> assignment.getCompanyType() == CompanyType.DEVELOPER)
            .toList();
        List<Assignment> oldClients = assignmentQueryPort.findAllByProjectId(projectId)
            .stream().filter(assignment -> assignment.getCompanyType() == CompanyType.CLIENT)
            .toList();

        updateAssignments(oldDevs, command.getDevUserId(), command.getDevManagerId());
        updateAssignments(oldClients, command.getClientUserId(), command.getClientManagerId());
        newAssignUsers(project, oldDevs, oldClients, command.getDevManagerId(),
            command.getDevUserId(), command.getClientManagerId(), command.getClientUserId());

        historyLogService.logUpdated(DomainType.PROJECT, loggedInUserId, before, project,
            "프로젝트 정보 수정");
    }

    @Override
    @CacheEvict(value = "dashboard:projectStatusCount", allEntries = true)
    public void changedStatus(Long projectId, Long loggedInUserId) {
        userValidator.checkAdmin(loggedInUserId);
        Project project = projectValidator.findById(projectId);
        Project before = Project.copyOf(project);

        if (project.getProjectStatus() == ProjectStatus.IN_PROGRESS) {
            project.statusCompleted();
        } else {
            project.statusInProgress();
        }

        updateProjectPort.update(project);
        historyLogService.logUpdated(DomainType.PROJECT, loggedInUserId, before, project,
            "프로젝트 상태 수정");
    }

    // todo: db 갔다오지말기, 멘토링
    @Override
    public void calculateProgressRate(Long projectId) {
        Project project = projectValidator.findById(projectId);
        int totalStep = readProjectStepPort.totalStep(projectId);
        int completedStep = readProjectStepPort.totalCompletedStep(projectId);
        project.calculateProgress(totalStep, completedStep);
        updateProjectPort.update(project);
    }

    // 단계 삭제용 진행률 계산
    @Override
    public void calculateProgressRateByDeleteStep(Long projectId) {
        Project project = projectValidator.findById(projectId);
        int totalStep = readProjectStepPort.totalStep(projectId);
        int completedStep = readProjectStepPort.totalCompletedStep(projectId);
        project.calculateProgress(totalStep - 1, completedStep);
        updateProjectPort.update(project);
    }

    @Override
    @CacheEvict(value = "dashboard:projectStatusCount", allEntries = true)
    public void updateDueSoonAndDelayedProjects() {
        List<Project> projects = readProjectPort.findUpdatableProjects();
        for (Project project : projects) {
            project.calculateStatus();
            updateProjectPort.update(project);
        }
    }

    // 기존 user의 userType이 변경된 경우 또는 user가 프로젝트에서 제외된 경우
    private void updateAssignments(List<Assignment> oldAssignments, List<Long> updatedUsers,
        List<Long> updatedManagers) {
        // 변경된 userId 집합
        Set<Long> updatedUserIds = new HashSet<>(updatedUsers);
        // 변경된 manager userId 집합
        Set<Long> managerIds = new HashSet<>(updatedManagers);

        oldAssignments.stream()
            .filter(assignment -> updatedUserIds.contains(
                assignment.getUserId())) // 변경된 유저아이디에 기존 아이디가 포함되어 있으면 userType 변경
            .forEach(assignment -> {
                UserType newUserType =
                    managerIds.contains(assignment.getUserId()) ? UserType.MANAGER
                        : UserType.MEMBER;
                assignment.updateUserType(newUserType);
            });
        saveProjectUserPort.saveAll(oldAssignments);

        List<Assignment> deleteUsers = oldAssignments.stream()
            .filter(assignment -> !updatedUserIds.contains(assignment.getUserId()))
            .toList();
        deleteAssignmentPort.deleteAll(deleteUsers);
    }

    private void newAssignUsers(Project project, List<Assignment> oldDevs,
        List<Assignment> oldClients, List<Long> devManagers, List<Long> devUsers,
        List<Long> clientManagers, List<Long> clientUsers) {
        // 기존 userId 리스트
        List<Long> oldDevUserIds = oldDevs.stream().map(Assignment::getUserId)
            .toList();
        List<Long> oldClientUserIds = oldClients.stream()
            .map(Assignment::getUserId).toList();

        // 새로 추가된 user 리스트 - 기존 userId 에 포함되어 있지 않은 user 리스트
        List<Long> newDevUsers = devUsers.stream()
            .filter(user -> !oldDevUserIds.contains(user))
            .toList();
        List<Long> newClientUsers = clientUsers.stream()
            .filter(user -> !oldClientUserIds.contains(user))
            .toList();

        List<User> clientUserList = userQueryPort.findByIds(newClientUsers);
        List<User> devUserList = userQueryPort.findByIds(newDevUsers);

        // 생성
        List<Assignment> newAssignments = Assignment.createProjectUser(
            project,
            devManagers,
            clientManagers,
            newDevUsers,
            newClientUsers,
            devUserList,
            clientUserList
        );

        saveProjectUserPort.saveAll(newAssignments);

        List<Long> assignmentIds = newAssignments.stream()
            .map(Assignment::getUserId).toList();
        assignmentNotificationSender.sendNotification(assignmentIds, project.getId());
    }
}
