package by.oma.academy.dto;

import java.time.OffsetDateTime;

public record CertificateDto(
        Long id,
        Long projectId,
        String projectTitle,
        String userName,
        String certNumber,
        OffsetDateTime issuedAt,
        String downloadUrl
) {}
