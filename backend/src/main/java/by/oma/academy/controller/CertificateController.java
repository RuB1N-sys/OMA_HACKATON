package by.oma.academy.controller;

import by.oma.academy.dto.CertificateDto;
import by.oma.academy.dto.EnrollRequest;
import by.oma.academy.service.AuthHelper;
import by.oma.academy.service.CertificateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;
    private final AuthHelper authHelper;

    public CertificateController(CertificateService certificateService, AuthHelper authHelper) {
        this.certificateService = certificateService;
        this.authHelper = authHelper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CertificateDto issueCertificate(
            @Valid @RequestBody EnrollRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return certificateService.issueCertificate(userId, request.projectId());
    }

    @GetMapping
    public List<CertificateDto> getMyCertificates(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return certificateService.getUserCertificates(userId);
    }

    @GetMapping("/{id}")
    public CertificateDto getCertificate(@PathVariable Long id) {
        return certificateService.getCertificate(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadCertificatePdf(@PathVariable Long id) {
        byte[] pdfBytes = certificateService.generateCertificatePdf(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"oma-certificate-" + id + ".pdf\"")
                .body(pdfBytes);
    }
}
