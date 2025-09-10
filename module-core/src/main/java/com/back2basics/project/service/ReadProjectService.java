package com.back2basics.project.service;

import com.back2basics.assignment.port.out.AssignmentQueryPort;
import com.back2basics.company.model.CompanyType;
import com.back2basics.company.port.out.ReadCompanyPort;
import com.back2basics.infra.validator.ProjectValidator;
import com.back2basics.infra.validator.UserValidator;
import com.back2basics.project.model.Project;
import com.back2basics.project.model.ProjectStatus;
import com.back2basics.project.model.StatusCountProjection;
import com.back2basics.project.port.in.ReadProjectUseCase;
import com.back2basics.project.port.out.ReadProjectPort;
import com.back2basics.project.service.result.ProjectClientUserResult;
import com.back2basics.project.service.result.ProjectCountResult;
import com.back2basics.project.service.result.ProjectDetailResult;
import com.back2basics.project.service.result.ProjectGetResult;
import com.back2basics.project.service.result.ProjectListResult;
import com.back2basics.project.service.result.ProjectRecentGetResult;
import com.back2basics.project.service.result.ProjectStatusResult;
import com.back2basics.user.model.Role;
import com.back2basics.user.model.UserType;
import com.back2basics.user.port.out.UserQueryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReadProjectService implements ReadProjectUseCase {

    private final ReadProjectPort readProjectPort;
    private final ProjectValidator projectValidator;
    private final UserValidator userValidator;
    private final UserQueryPort userQueryPort;
    private final ReadCompanyPort readCompanyPort;
    private final AssignmentQueryPort assignmentQueryPort;

    @Override
    public Page<ProjectListResult> getAllProjects(Pageable pageable) {
        Page<Project> projects = readProjectPort.findAll(pageable);
        return projects.map(ProjectListResult::toResult);
    }

    @Override
    public Page<ProjectListResult> getUserProjects(Long userId, Pageable pageable) {
        userValidator.validateNotFoundUserId(userId);
        Page<Project> projects = readProjectPort.findAllByUserId(userId, pageable);
        return projects.map(ProjectListResult::toResult);
    }

    @Override
    public ProjectDetailResult getProjectDetails(Long projectId, Long userId) {
        Project project = projectValidator.findAssignmentsProject(projectId, userId);
        UserType userType = assignmentQueryPort.findUserTypeByProjectIdAndUserId(projectId, userId);
        CompanyType companyType = assignmentQueryPort.findCompanyTypeByProjectIdAndUserId(projectId,
            userId);
        return ProjectDetailResult.of(project, userType, companyType);
    }

    @Override
    public Page<ProjectListResult> getDeletedProjects(Pageable pageable) {
        Page<Project> projects = readProjectPort.findAllDeletedProjects(pageable);
        return projects.map(ProjectListResult::toResult);
    }

    @Override
    public List<ProjectRecentGetResult> getRecentProjects() {
        return readProjectPort.getRecentProjects().stream().map(ProjectRecentGetResult::toResult)
            .toList();
    }

    @Override
    public List<ProjectStatusResult> findProjectByStatus(Long userId, Role role,
        ProjectStatus status) {
        List<Project> projects;
        if (role.equals(Role.USER)) {
            projects = readProjectPort.findByStatusAndUserId(userId, status);
        } else {
            projects = readProjectPort.findByStatus(status);
        }

        return projects.stream()
            .map(project -> new ProjectStatusResult(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getProgress()
            ))
            .toList();
    }

    @Override
    public List<ProjectGetResult> getAllProjects() {
        List<Project> projects = readProjectPort.getAllProjects();
        return projects.stream()
            .map(ProjectGetResult::toResult).toList();
    }

    @Override
    @Cacheable(value = "dashboard:projectStatusCount")
    public List<ProjectCountResult> getCountByProjectStatus() {
        List<StatusCountProjection> projections = readProjectPort.countProjectsByProjectStatus();
        return projections.stream().map(ProjectCountResult::toResult).toList();
    }

    @Override
    public List<ProjectClientUserResult> getClientUsersByProjectId(Long projectId) {
        Project project = readProjectPort.findById(projectId);
        return project.getAssignments().stream()
            .filter(assignment -> assignment.getCompanyType().equals(CompanyType.CLIENT))
            .map(assignment -> new ProjectClientUserResult(
                assignment.getUserId(),
                userQueryPort.findById(assignment.getUserId()).getName(),
                userQueryPort.findById(assignment.getUserId()).getUsername(),
                assignment.getCompanyId(),
                assignment.getCompanyName()
            )).toList();
    }
}
