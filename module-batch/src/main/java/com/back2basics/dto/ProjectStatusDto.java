package com.back2basics.dto;

import com.back2basics.project.model.ProjectStatus;
import lombok.Getter;

@Getter
public class ProjectStatusDto {
    private final Long id;
    private final ProjectStatus projectStatus;

    public ProjectStatusDto(Long id, ProjectStatus projectStatus) {
        this.id = id;
        this.projectStatus = projectStatus;
    }
}
