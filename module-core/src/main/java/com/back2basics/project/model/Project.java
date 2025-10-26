package com.back2basics.project.model;

import com.back2basics.assignment.model.Assignment;
import com.back2basics.history.strategy.TargetDomain;
import com.back2basics.project.port.in.command.ProjectCreateCommand;
import com.back2basics.project.port.in.command.ProjectUpdateCommand;
import com.back2basics.projectstep.model.ProjectStep;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Project implements TargetDomain {

    private final Long id;

    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private final LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private boolean isDeleted;

    private ProjectStatus projectStatus;

    private int progress;

    private String projectCost;

    private List<ProjectStep> steps;

    private List<Assignment> assignments;

    @Builder
    public Project(Long id, String name, String description, LocalDate startDate, LocalDate endDate,
        LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt,
        boolean isDeleted, ProjectStatus projectStatus, String projectCost, List<ProjectStep> steps,
        List<Assignment> assignments, int progress) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.isDeleted = isDeleted;
        this.projectStatus = projectStatus;
        this.projectCost = projectCost;
        this.steps = steps;
        this.assignments = assignments;
        this.progress = progress;
    }

    // todo: 함수 ㄴ, 값만
    public static Project create(ProjectCreateCommand command) {
        return Project.builder()
            .name(command.getName())
            .description(command.getDescription())
            .projectCost(command.getProjectCost())
            .startDate(command.getStartDate())
            .endDate(command.getEndDate())
            .projectStatus(createStatusByDate(command.getEndDate()))
            .progress(0)
            .build();
    }

    // todo: 함수 ㄴ, 값만
    public void update(ProjectUpdateCommand command) {
        this.name = command.getName();
        this.description = command.getDescription();
        this.projectCost = command.getProjectCost();
        this.startDate = command.getStartDate();
        this.endDate = command.getEndDate();
//        calculateStatusByDate(command.getEndDate()); // 배치로 변경
    }

    public void updateStatus(ProjectStatus newStatus) {
        this.projectStatus = newStatus;
    }

    public void statusCompleted() {
        this.projectStatus = ProjectStatus.COMPLETED;
    }

    public void statusInProgress() {
        this.projectStatus = ProjectStatus.IN_PROGRESS;
    }

    public void softDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        System.out.println("restore call");
    }

    public void calculateProgress(int totalStep, int completedStep) {
        double result = ((double) completedStep / totalStep) * 100;
        this.progress = (int) result;
        this.projectStatus = updateStatusByProgress(result);
    }

    private static ProjectStatus createStatusByDate(LocalDate endDate) {
        LocalDate today = LocalDate.now();
        ProjectStatus status = ProjectStatus.IN_PROGRESS;

        if (endDate != null) {
            long due = ChronoUnit.DAYS.between(today, endDate);
            if (due < 0) {
                status = ProjectStatus.DELAY;
            } else if (due <= 7) {
                status = ProjectStatus.DUE_SOON;
            }
        }
        return status;
    }

    private void calculateStatusByDate(LocalDate endDate) {
        LocalDate today = LocalDate.now();
        ProjectStatus status = ProjectStatus.IN_PROGRESS;

        if (endDate != null) {
            long due = ChronoUnit.DAYS.between(today, endDate);
            if (due < 0) {
                status = ProjectStatus.DELAY;
            } else if (due <= 7) {
                status = ProjectStatus.DUE_SOON;
            }
            if (this.progress == 100) {
                status = ProjectStatus.COMPLETED;
            }
        }
        this.projectStatus = status;
    }

    private ProjectStatus updateStatusByProgress(double result) {
        LocalDate today = LocalDate.now();
        ProjectStatus status = ProjectStatus.IN_PROGRESS;
        if (this.endDate != null) {
            long due = ChronoUnit.DAYS.between(today, this.endDate);

            if (due < 0) {
                status = ProjectStatus.DELAY;
            } else if (due <= 7) {
                status = ProjectStatus.DUE_SOON;
            }
        }
        if ((int) result == 100) {
            status = ProjectStatus.COMPLETED;
        }
        return status;
    }

    public static Project fromUpdateCommand(ProjectUpdateCommand command) {
        return Project.builder()
            .name(command.getName())
            .description(command.getDescription())
            .projectCost(command.getProjectCost())
            .startDate(command.getStartDate())
            .endDate(command.getEndDate())
            .build();
    }

    public static Project copyOf(Project project) {
        return Project.builder()
            .id(project.getId())
            .name(project.getName())
            .description(project.getDescription())
            .startDate(project.getStartDate())
            .endDate(project.getEndDate())
            .createdAt(project.getCreatedAt())
            .updatedAt(project.getUpdatedAt())
            .deletedAt(project.getDeletedAt())
            .isDeleted(project.isDeleted())
            .projectStatus(project.getProjectStatus())
            .steps(project.getSteps())
            .assignments(project.getAssignments())
            .progress(project.getProgress())
            .build();
    }

    public void calculateStatus() {
        LocalDate today = LocalDate.now();
        LocalDate end = this.getEndDate();

        if (end == null) {
            return;
        }

        if (end.isBefore(today)) {
            this.projectStatus = ProjectStatus.DELAY;
        } else if (!end.isBefore(today) && ChronoUnit.DAYS.between(today, end) <= 7) {
            this.projectStatus = ProjectStatus.DUE_SOON;
        } else if (this.progress == 100) {
            this.projectStatus = ProjectStatus.COMPLETED;
        }
    }
}