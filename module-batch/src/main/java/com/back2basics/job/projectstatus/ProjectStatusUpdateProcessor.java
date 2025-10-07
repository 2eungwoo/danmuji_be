package com.back2basics.job.projectstatus;

import com.back2basics.project.model.Project;
import com.back2basics.project.model.ProjectStatus;
import java.time.LocalDate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectStatusUpdateProcessor implements ItemProcessor<Project, Project> {

    private static final int DUE_SOON_DAYS = 7;

    @Override
    public Project process(Project project) throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate endDate = project.getEndDate();

        boolean statusChanged = false;

        // 지연 플젝
        if (endDate.isBefore(today) && project.getProjectStatus() != ProjectStatus.COMPLETED) {
            project.update(ProjectStatus.DELAY);
            statusChanged = true;
        }

        // 마감 임박 플젝들
        else if (endDate.isBefore(today.plusDays(DUE_SOON_DAYS)) && project.getProjectStatus() == ProjectStatus.IN_PROGRESS) {
            project.update(ProjectStatus.DUE_SOON);
            statusChanged = true;
        }

        // 바뀐거만 리턴
        return statusChanged ? project : null;
    }
}
