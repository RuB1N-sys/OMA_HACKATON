package by.oma.academy.service;

import by.oma.academy.dto.CertificateDto;
import by.oma.academy.exception.BadRequestException;
import by.oma.academy.exception.NotFoundException;
import by.oma.academy.model.Certificate;
import by.oma.academy.model.Enrollment;
import by.oma.academy.model.Project;
import by.oma.academy.model.User;
import by.oma.academy.repository.CertificateRepository;
import by.oma.academy.repository.EnrollmentRepository;
import by.oma.academy.repository.ProjectRepository;
import by.oma.academy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AchievementService achievementService;

    public CertificateService(
            CertificateRepository certificateRepository,
            EnrollmentRepository enrollmentRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            AchievementService achievementService
    ) {
        this.certificateRepository = certificateRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.achievementService = achievementService;
    }

    @Transactional
    public CertificateDto issueCertificate(Long userId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Проект не найден"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Enrollment enrollment = enrollmentRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new BadRequestException("Вы не записаны на данный курс"));

        if (!"completed".equalsIgnoreCase(enrollment.getStatus())) {
            throw new BadRequestException("Сертификат доступен только после завершения всех шагов обучения");
        }

        Certificate certificate = certificateRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseGet(() -> {
                    Certificate cert = new Certificate();
                    cert.setUserId(userId);
                    cert.setProjectId(projectId);
                    cert.setCertNumber(generateCertNumber());
                    return certificateRepository.save(cert);
                });

        achievementService.checkAndAward(userId);
        return toDto(certificate, user.getName(), project.getTitle());
    }

    @Transactional(readOnly = true)
    public List<CertificateDto> getUserCertificates(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        String userName = user != null ? user.getName() : "Пользователь";

        return certificateRepository.findByUserId(userId).stream()
                .map(cert -> {
                    String title = projectRepository.findById(cert.getProjectId())
                            .map(Project::getTitle)
                            .orElse("Проект ОМА");
                    return toDto(cert, userName, title);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CertificateDto getCertificate(Long certId) {
        Certificate cert = certificateRepository.findById(certId)
                .orElseThrow(() -> new NotFoundException("Сертификат не найден"));

        User user = userRepository.findById(cert.getUserId()).orElse(null);
        String userName = user != null ? user.getName() : "Пользователь";
        String title = projectRepository.findById(cert.getProjectId())
                .map(Project::getTitle)
                .orElse("Проект ОМА");

        return toDto(cert, userName, title);
    }

    @Transactional(readOnly = true)
    public byte[] generateCertificatePdf(Long certId) {
        Certificate cert = certificateRepository.findById(certId)
                .orElseThrow(() -> new NotFoundException("Сертификат не найден"));

        User user = userRepository.findById(cert.getUserId()).orElse(null);
        String studentName = user != null ? user.getName() : "Student";
        Project project = projectRepository.findById(cert.getProjectId()).orElse(null);
        String courseTitle = project != null ? project.getTitle() : "OMA Academy Course";
        String certNum = cert.getCertNumber();
        String issueDate = cert.getIssuedAt() != null
                ? cert.getIssuedAt().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        return buildPdfDocument(studentName, courseTitle, certNum, issueDate);
    }

    private String generateCertNumber() {
        int year = OffsetDateTime.now().getYear();
        int randomDigits = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "OMA-" + year + "-" + randomDigits;
    }

    private CertificateDto toDto(Certificate c, String userName, String projectTitle) {
        return new CertificateDto(
                c.getId(),
                c.getProjectId(),
                projectTitle,
                userName,
                c.getCertNumber(),
                c.getIssuedAt(),
                "/api/certificates/" + c.getId() + "/pdf"
        );
    }

    /**
     * Создает валидный PDF-документ (спецификация PDF-1.4) сертификата ОМА.
     */
    private byte[] buildPdfDocument(String studentName, String courseTitle, String certNumber, String issueDate) {
        // Очищаем строки от не-ASCII символов для Type1 шрифтов
        String cleanStudent = transliterate(studentName);
        String cleanCourse = transliterate(courseTitle);

        String streamContent = "BT\n"
                + "/F1 28 Tf\n"
                + "50 720 Td (OMA ACADEMY) Tj\n"
                + "/F2 14 Tf\n"
                + "0 -35 Td (OFFICIAL CERTIFICATE OF COMPLETION) Tj\n"
                + "/F2 12 Tf\n"
                + "0 -40 Td (This certificate is proudly presented to:) Tj\n"
                + "/F1 22 Tf\n"
                + "0 -35 Td (" + escapePdf(cleanStudent) + ") Tj\n"
                + "/F2 12 Tf\n"
                + "0 -35 Td (for successfully completing the specialized professional program:) Tj\n"
                + "/F1 16 Tf\n"
                + "0 -30 Td (" + escapePdf(cleanCourse) + ") Tj\n"
                + "/F2 11 Tf\n"
                + "0 -50 Td (Certificate ID: " + escapePdf(certNumber) + ") Tj\n"
                + "0 -20 Td (Issue Date: " + escapePdf(issueDate) + ") Tj\n"
                + "0 -20 Td (Accreditation: OMA Retail Group & Partner Brands) Tj\n"
                + "0 -40 Td (OMA.BY / ACADEMY - RETAIL & PRO ECOSYSTEM) Tj\n"
                + "ET\n";

        byte[] streamBytes = streamContent.getBytes(StandardCharsets.US_ASCII);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));

            long[] offsets = new long[6];

            // Obj 1: Catalog
            offsets[1] = out.size();
            out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 2: Pages
            offsets[2] = out.size();
            out.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 3: Page
            offsets[3] = out.size();
            out.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /Resources 4 0 R /MediaBox [0 0 595 842] /Contents 5 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 4: Resources
            offsets[4] = out.size();
            out.write(("4 0 obj\n<< /Font << "
                    + "/F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >> "
                    + "/F2 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> "
                    + ">> >>\nendobj\n").getBytes(StandardCharsets.US_ASCII));

            // Obj 5: Stream
            offsets[5] = out.size();
            out.write(("5 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n").getBytes(StandardCharsets.US_ASCII));
            out.write(streamBytes);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Cross-Reference Table
            long xrefStart = out.size();
            out.write("xref\n0 6\n".getBytes(StandardCharsets.US_ASCII));
            out.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
            for (int i = 1; i <= 5; i++) {
                out.write(String.format("%010d 00000 n \n", offsets[i]).getBytes(StandardCharsets.US_ASCII));
            }

            out.write(("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefStart + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка формирования PDF сертификата", e);
        }
    }

    private String escapePdf(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String transliterate(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case 'А' -> sb.append("A"); case 'Б' -> sb.append("B"); case 'В' -> sb.append("V");
                case 'Г' -> sb.append("G"); case 'Д' -> sb.append("D"); case 'Е' -> sb.append("E");
                case 'Ё' -> sb.append("Yo"); case 'Ж' -> sb.append("Zh"); case 'З' -> sb.append("Z");
                case 'И' -> sb.append("I"); case 'Й' -> sb.append("Y"); case 'К' -> sb.append("K");
                case 'Л' -> sb.append("L"); case 'М' -> sb.append("M"); case 'Н' -> sb.append("N");
                case 'О' -> sb.append("O"); case 'П' -> sb.append("P"); case 'Р' -> sb.append("R");
                case 'С' -> sb.append("S"); case 'Т' -> sb.append("T"); case 'У' -> sb.append("U");
                case 'Ф' -> sb.append("F"); case 'Х' -> sb.append("Kh"); case 'Ц' -> sb.append("Ts");
                case 'Ч' -> sb.append("Ch"); case 'Ш' -> sb.append("Sh"); case 'Щ' -> sb.append("Shch");
                case 'Ъ', 'Ь' -> sb.append(""); case 'Ы' -> sb.append("Y"); case 'Э' -> sb.append("E");
                case 'Ю' -> sb.append("Yu"); case 'Я' -> sb.append("Ya");
                case 'а' -> sb.append("a"); case 'б' -> sb.append("b"); case 'в' -> sb.append("v");
                case 'г' -> sb.append("g"); case 'д' -> sb.append("d"); case 'е' -> sb.append("e");
                case 'ё' -> sb.append("yo"); case 'ж' -> sb.append("zh"); case 'з' -> sb.append("z");
                case 'и' -> sb.append("i"); case 'й' -> sb.append("y"); case 'к' -> sb.append("k");
                case 'л' -> sb.append("l"); case 'м' -> sb.append("m"); case 'н' -> sb.append("n");
                case 'о' -> sb.append("o"); case 'п' -> sb.append("p"); case 'р' -> sb.append("r");
                case 'с' -> sb.append("s"); case 'т' -> sb.append("t"); case 'у' -> sb.append("u");
                case 'ф' -> sb.append("f"); case 'х' -> sb.append("kh"); case 'ц' -> sb.append("ts");
                case 'ч' -> sb.append("ch"); case 'ш' -> sb.append("sh"); case 'щ' -> sb.append("shch");
                case 'ъ', 'ь' -> sb.append(""); case 'ы' -> sb.append("y"); case 'э' -> sb.append("e");
                case 'ю' -> sb.append("yu"); case 'я' -> sb.append("ya");
                default -> {
                    if (c >= 32 && c <= 126) sb.append(c);
                    else sb.append(" ");
                }
            }
        }
        return sb.toString();
    }
}
