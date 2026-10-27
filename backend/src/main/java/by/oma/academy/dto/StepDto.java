package by.oma.academy.dto;

import java.util.List;

public record StepDto(
        Long id,
        Integer stepNumber,
        String title,
        String description,
        String videoUrl,
        Integer sortOrder,
        List<MaterialDto> materials,
        List<ToolDto> tools,
        Boolean completed
) {}
