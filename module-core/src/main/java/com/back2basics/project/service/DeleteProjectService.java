package com.back2basics.project.service;

import com.back2basics.global.cache.DashboardCacheService;
import com.back2basics.history.model.DomainType;
import com.back2basics.history.service.HistoryLogService;
import com.back2basics.infra.validator.ProjectValidator;
import com.back2basics.infra.validator.UserValidator;
import com.back2basics.project.model.Project;
import com.back2basics.project.port.in.DeleteProjectUseCase;
import com.back2basics.project.port.out.UpdateProjectPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteProjectService implements DeleteProjectUseCase {

    private final UpdateProjectPort port;
    private final UserValidator userValidator;
    private final ProjectValidator projectValidator;
    private final HistoryLogService historyLogService;
    private final DashboardCacheService dashboardCacheService;

    @Override
    public void deleteProject(Long id, Long loggedInUserId) {
        userValidator.checkAdmin(loggedInUserId);

        Project project = projectValidator.findById(id);
        project.softDeleted();
        port.update(project);

        historyLogService.logDeleted(DomainType.PROJECT, loggedInUserId, project,
            "프로젝트 비활성화");

        dashboardCacheService.incrementVersion(null, "projectStatusCount");
    }
}
