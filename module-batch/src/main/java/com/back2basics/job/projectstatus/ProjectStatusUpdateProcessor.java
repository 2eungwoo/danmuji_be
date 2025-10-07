package com.back2basics.job.projectstatus;

import com.back2basics.project.model.Project;
import com.back2basics.project.model.ProjectStatus;
import java.time.LocalDate;
import org.springframework.batch.item.ItemProcessor;

public class ProjectStatusUpdateProcessor implements ItemProcessor<Project, Project> {

    private static final int DUE_SOON_DAYS = 7;

    @Override
    public Project process(Project project) throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate endDate = project.getEndDate();

        if (endDate == null) {
            return null; // 마감날짜가 nullable이라 널처
        }

        boolean statusChanged = false;

        // 지연 플젝
        if (endDate.isBefore(today) && project.getProjectStatus() != ProjectStatus.COMPLETED) {
            project.updateStatus(ProjectStatus.DELAY);
            statusChanged = true;
        }
        // 마감 임박 플젝들
        else if (endDate.isBefore(today.plusDays(DUE_SOON_DAYS)) && project.getProjectStatus() == ProjectStatus.IN_PROGRESS) {
            project.updateStatus(ProjectStatus.DUE_SOON);
            statusChanged = true;
        }

        return statusChanged ? project : null;
    }
}