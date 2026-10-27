package by.oma.academy.service;

import by.oma.academy.dto.*;
import by.oma.academy.exception.BadRequestException;
import by.oma.academy.exception.ForbiddenException;
import by.oma.academy.exception.NotFoundException;
import by.oma.academy.model.*;
import by.oma.academy.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class B2bService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StepRepository stepRepository;
    private final StepCompletionRepository stepCompletionRepository;

    public B2bService(
            CompanyRepository companyRepository,
            CompanyMemberRepository companyMemberRepository,
            CourseAssignmentRepository courseAssignmentRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            EnrollmentRepository enrollmentRepository,
            StepRepository stepRepository,
            StepCompletionRepository stepCompletionRepository
    ) {
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.stepRepository = stepRepository;
        this.stepCompletionRepository = stepCompletionRepository;
    }

    @Transactional
    public CompanyDto createCompany(Long ownerId, CreateCompanyRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Company company = new Company();
        company.setName(request.name().trim());
        company.setOwnerId(ownerId);
        company = companyRepository.save(company);

        // Добавляем создателя владельцем
        CompanyMember ownerMember = new CompanyMember();
        ownerMember.setCompanyId(company.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setRole("owner");
        companyMemberRepository.save(ownerMember);

        return toCompanyDto(company, owner.getName(), 1);
    }

    @Transactional(readOnly = true)
    public List<CompanyDto> getUserCompanies(Long userId) {
        List<CompanyMember> memberships = companyMemberRepository.findByUserId(userId);
        return memberships.stream().map(m -> {
            Company company = companyRepository.findById(m.getCompanyId()).orElse(null);
            if (company == null) return null;
            String ownerName = userRepository.findById(company.getOwnerId())
                    .map(User::getName)
                    .orElse("Владелец");
            int membersCount = companyMemberRepository.findByCompanyId(company.getId()).size();
            return toCompanyDto(company, ownerName, membersCount);
        }).filter(c -> c != null).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompanyDto getCompany(Long companyId, Long requesterId) {
        checkMembership(companyId, requesterId);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Компания не найдена"));

        String ownerName = userRepository.findById(company.getOwnerId())
                .map(User::getName)
                .orElse("Владелец");
        int membersCount = companyMemberRepository.findByCompanyId(company.getId()).size();
        return toCompanyDto(company, ownerName, membersCount);
    }

    @Transactional
    public CompanyMemberDto addMember(Long companyId, Long managerId, AddCompanyMemberRequest request) {
        checkManagerOrOwner(companyId, managerId);

        User targetUser = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new NotFoundException("Пользователь с email " + request.email() + " не зарегистрирован на платформе"));

        if (companyMemberRepository.existsByCompanyIdAndUserId(companyId, targetUser.getId())) {
            throw new BadRequestException("Пользователь уже состоит в данной компании/бригаде");
        }

        CompanyMember member = new CompanyMember();
        member.setCompanyId(companyId);
        member.setUserId(targetUser.getId());
        member.setRole(request.role() != null && !request.role().isBlank() ? request.role() : "member");
        member = companyMemberRepository.save(member);

        return new CompanyMemberDto(member.getId(), targetUser.getId(), targetUser.getName(), targetUser.getEmail(), member.getRole());
    }

    @Transactional(readOnly = true)
    public List<CompanyMemberDto> listMembers(Long companyId, Long requesterId) {
        checkMembership(companyId, requesterId);

        List<CompanyMember> members = companyMemberRepository.findByCompanyId(companyId);
        Map<Long, User> userMap = userRepository.findAllById(
                members.stream().map(CompanyMember::getUserId).toList()
        ).stream().collect(Collectors.toMap(User::getId, u -> u));

        return members.stream().map(m -> {
            User u = userMap.get(m.getUserId());
            return new CompanyMemberDto(
                    m.getId(),
                    m.getUserId(),
                    u != null ? u.getName() : "Сотрудник",
                    u != null ? u.getEmail() : "",
                    m.getRole()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public CourseAssignmentDto assignCourse(Long companyId, Long managerId, AssignCourseRequest request) {
        checkManagerOrOwner(companyId, managerId);

        if (!companyMemberRepository.existsByCompanyIdAndUserId(companyId, request.userId())) {
            throw new BadRequestException("Назначаемый сотрудник не является участником компании");
        }

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new NotFoundException("Проект не найден"));

        User employee = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("Сотрудник не найден"));

        CourseAssignment assignment = courseAssignmentRepository
                .findByCompanyIdAndUserIdAndProjectId(companyId, request.userId(), request.projectId())
                .orElseGet(() -> {
                    CourseAssignment ca = new CourseAssignment();
                    ca.setCompanyId(companyId);
                    ca.setUserId(request.userId());
                    ca.setProjectId(request.projectId());
                    ca.setAssignedBy(managerId);
                    ca.setDeadline(request.deadline());
                    return courseAssignmentRepository.save(ca);
                });

        // Автоматически создаем запись на курс для сотрудника, если еще нет
        if (enrollmentRepository.findByUserIdAndProjectId(request.userId(), request.projectId()).isEmpty()) {
            Enrollment e = new Enrollment();
            e.setUser(employee);
            e.setProject(project);
            e.setStatus("in_progress");
            enrollmentRepository.save(e);
        }

        return toAssignmentDto(assignment, employee.getName(), project.getTitle());
    }

    @Transactional(readOnly = true)
    public List<CourseAssignmentDto> listAssignments(Long companyId, Long requesterId) {
        checkMembership(companyId, requesterId);

        List<CourseAssignment> list = courseAssignmentRepository.findByCompanyId(companyId);
        return list.stream().map(a -> {
            String userName = userRepository.findById(a.getUserId()).map(User::getName).orElse("Сотрудник");
            String projectTitle = projectRepository.findById(a.getProjectId()).map(Project::getTitle).orElse("Курс");
            return toAssignmentDto(a, userName, projectTitle);
        }).collect(Collectors.toList());
    }

    private void checkMembership(Long companyId, Long userId) {
        if (!companyMemberRepository.existsByCompanyIdAndUserId(companyId, userId)) {
            throw new ForbiddenException("У вас нет доступа к данной компании");
        }
    }

    private void checkManagerOrOwner(Long companyId, Long userId) {
        CompanyMember member = companyMemberRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ForbiddenException("У вас нет доступа к компании"));
        if (!"owner".equalsIgnoreCase(member.getRole()) && !"manager".equalsIgnoreCase(member.getRole())) {
            throw new ForbiddenException("Только владелец или бригадир может управлять сотрудниками и курсами");
        }
    }

    private CompanyDto toCompanyDto(Company c, String ownerName, int membersCount) {
        return new CompanyDto(
                c.getId(),
                c.getName(),
                c.getOwnerId(),
                ownerName,
                c.getCreatedAt(),
                membersCount
        );
    }

    private CourseAssignmentDto toAssignmentDto(CourseAssignment a, String userName, String projectTitle) {
        var enrollmentOpt = enrollmentRepository.findByUserIdAndProjectId(a.getUserId(), a.getProjectId());
        String status = enrollmentOpt.map(Enrollment::getStatus).orElse("assigned");

        List<Step> steps = stepRepository.findByProjectIdOrderBySortOrder(a.getProjectId());
        List<Long> stepIds = steps.stream().map(Step::getId).toList();
        int completedSteps = stepIds.isEmpty() ? 0 :
                (int) stepCompletionRepository.countByUserIdAndStepIdIn(a.getUserId(), stepIds);

        return new CourseAssignmentDto(
                a.getId(),
                a.getCompanyId(),
                a.getUserId(),
                userName,
                a.getProjectId(),
                projectTitle,
                a.getAssignedBy(),
                a.getAssignedAt(),
                a.getDeadline(),
                status,
                completedSteps,
                steps.size()
        );
    }
}
