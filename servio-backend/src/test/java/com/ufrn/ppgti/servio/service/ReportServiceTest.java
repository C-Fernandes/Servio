package com.ufrn.ppgti.servio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ufrn.ppgti.servio.dto.request.CreateReportRequestDTO;
import com.ufrn.ppgti.servio.dto.response.ReportResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.Report;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.ReportReason;
import com.ufrn.ppgti.servio.model.enums.ReportStatus;
import com.ufrn.ppgti.servio.model.enums.ReportTargetType;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.ReportRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;
import com.ufrn.ppgti.servio.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService - SPEC-010 (RF-20 Denúncia de Serviço ou Usuário)")
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthService authService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReportService reportService;

    private static final Long REPORTER_ID = 1L;
    private static final Long PROVIDER_USER_ID = 2L;
    private static final Long SERVICE_ID = 10L;

    @Test
    @DisplayName("Cenário 1: cliente denuncia um serviço de outro prestador")
    void createReport_serviceFromOtherProvider_persistsPending() {
        User reporter = clientUser();
        com.ufrn.ppgti.servio.model.Service service = serviceOf(PROVIDER_USER_ID);

        when(authService.getAuthenticadUser()).thenReturn(reporter);
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service));
        when(reportRepository.findByReporterIdAndReportedServiceIdAndStatus(REPORTER_ID, SERVICE_ID,
                ReportStatus.PENDING)).thenReturn(Optional.empty());
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> {
            Report r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        ReportResponseDTO result = reportService.createReport(createServiceReportDto(ReportReason.SPAM));

        assertEquals(ReportStatus.PENDING, result.getStatus());
        assertEquals(ReportTargetType.SERVICE, result.getTargetType());
    }

    @Test
    @DisplayName("Cenário 2: admin revisa e o denunciado (dono do serviço) é notificado")
    void updateStatus_toReviewed_notifiesReportedServiceOwner() {
        User admin = adminUser();
        User providerUserAccount = new User();
        providerUserAccount.setId(PROVIDER_USER_ID);
        com.ufrn.ppgti.servio.model.Service service = serviceOf(PROVIDER_USER_ID);
        service.getProvider().setUser(providerUserAccount);

        Report report = new Report(clientUser(), service, ReportReason.SPAM, "denúncia");
        report.setId(100L);

        when(authService.getAuthenticadUser()).thenReturn(admin);
        when(reportRepository.findById(100L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        reportService.updateStatus(100L, ReportStatus.REVIEWED);

        verify(notificationService).notifyGeneric(eq(providerUserAccount), any(String.class));
    }

    @Test
    @DisplayName("Cenário 3: admin filtra denúncias por status")
    void listAll_withStatusFilter_returnsOnlyMatching() {
        when(reportRepository.findAllByStatusOrderByCreatedAtDesc(ReportStatus.PENDING))
                .thenReturn(java.util.List.of());

        reportService.listAll(ReportStatus.PENDING);

        verify(reportRepository).findAllByStatusOrderByCreatedAtDesc(ReportStatus.PENDING);
        verify(reportRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Edge 1: denúncia duplicada ainda pendente reaproveita a existente, não cria outra")
    void createReport_alreadyPendingForSameTarget_reusesExisting() {
        User reporter = clientUser();
        com.ufrn.ppgti.servio.model.Service service = serviceOf(PROVIDER_USER_ID);
        Report existing = new Report(reporter, service, ReportReason.FRAUD, "primeira denúncia");
        existing.setId(50L);

        when(authService.getAuthenticadUser()).thenReturn(reporter);
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service));
        when(reportRepository.findByReporterIdAndReportedServiceIdAndStatus(REPORTER_ID, SERVICE_ID,
                ReportStatus.PENDING)).thenReturn(Optional.of(existing));

        ReportResponseDTO result = reportService.createReport(createServiceReportDto(ReportReason.SPAM));

        assertEquals(50L, result.getId());
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Edge 2: prestador não pode denunciar o próprio serviço")
    void createReport_ownService_throwsBusinessException() {
        User reporterAsProvider = new User();
        reporterAsProvider.setId(PROVIDER_USER_ID);
        reporterAsProvider.setRole(Role.PROVIDER);

        com.ufrn.ppgti.servio.model.Service ownService = serviceOf(PROVIDER_USER_ID);

        when(authService.getAuthenticadUser()).thenReturn(reporterAsProvider);
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(ownService));

        assertThrows(BusinessException.class,
                () -> reportService.createReport(createServiceReportDto(ReportReason.OTHER)));
        verify(reportRepository, never()).save(any());
    }

    // ----- helpers -----

    private User clientUser() {
        User user = new User();
        user.setId(REPORTER_ID);
        user.setRole(Role.CLIENT);
        return user;
    }

    private User adminUser() {
        User user = new User();
        user.setId(99L);
        user.setRole(Role.ADMIN);
        return user;
    }

    private com.ufrn.ppgti.servio.model.Service serviceOf(Long providerUserId) {
        com.ufrn.ppgti.servio.model.Service service = new com.ufrn.ppgti.servio.model.Service();
        service.setId(SERVICE_ID);
        service.setTitle("Encanamento");

        User providerUserAccount = new User();
        providerUserAccount.setId(providerUserId);

        ProviderProfile provider = new ProviderProfile();
        provider.setId(providerUserId);
        provider.setUser(providerUserAccount);
        service.setProvider(provider);

        return service;
    }

    private CreateReportRequestDTO createServiceReportDto(ReportReason reason) {
        CreateReportRequestDTO dto = new CreateReportRequestDTO();
        dto.setTargetType(ReportTargetType.SERVICE);
        dto.setTargetId(SERVICE_ID);
        dto.setReason(reason);
        dto.setDescription("descrição da denúncia");
        return dto;
    }
}
