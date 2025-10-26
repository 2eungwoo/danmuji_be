package com.back2basics.project.service;

import com.back2basics.assignment.port.out.AssignmentQueryPort;
import com.back2basics.company.model.CompanyType;
import com.back2basics.company.port.out.ReadCompanyPort;
import com.back2basics.global.config.CacheKeyProperties;
import com.back2basics.infra.validator.ProjectValidator;
import com.back2basics.infra.validator.UserValidator;
import com.back2basics.project.model.Project;
import com.back2basics.project.model.ProjectStatus;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadProjectService implements ReadProjectUseCase {

    private final ReadProjectPort readProjectPort;
    private final ProjectValidator projectValidator;
    private final UserValidator userValidator;
    private final UserQueryPort userQueryPort;
    private final ReadCompanyPort readCompanyPort;
    private final AssignmentQueryPort assignmentQueryPort;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheKeyProperties cacheKeyProperties;

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
    public List<ProjectCountResult> getCountByProjectStatus() {
        String redisKey = cacheKeyProperties.getDashboard() + ":stats:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String jsonResult = redisTemplate.opsForValue().get(redisKey);

        // rdb fallback을 위한 널처리
        if (jsonResult == null) {
            log.warn("데일리 통계 레디스에 없음 빈 리스트 반환, 키: {}.", redisKey);
            return List.of();
        }

        try {
            Map<String, Object> statsMap = objectMapper.readValue(jsonResult, new TypeReference<>() {});
            return convertMapToProjectCountResult(statsMap);
        } catch (IOException e) {
            log.error("레디스 직렬화 실패 예외, 빈 리스트 반환", e);
            return List.of();
        }
    }

    private List<ProjectCountResult> convertMapToProjectCountResult(Map<String, Object> statsMap) {
        List<ProjectCountResult> results = new ArrayList<>();
        results.add(new ProjectCountResult(ProjectStatus.IN_PROGRESS, ((Number) statsMap.getOrDefault("inProgressCount", 0)).longValue()));
        results.add(new ProjectCountResult(ProjectStatus.DUE_SOON, ((Number) statsMap.getOrDefault("dueSoonCount", 0)).longValue()));
        results.add(new ProjectCountResult(ProjectStatus.DELAY, ((Number) statsMap.getOrDefault("delayCount", 0)).longValue()));
        results.add(new ProjectCountResult(ProjectStatus.COMPLETED, ((Number) statsMap.getOrDefault("completedCount", 0)).longValue()));
        return results;
    }

    @Override
    public List<ProjectClientUserResult> getClientUsersByProjectId(Long projectId) {
        // todo: redis에 저장된 클라이언트 유저 리스트를 읽어와서 반환하느느 쿼리 작성
        return null;
    }
}
