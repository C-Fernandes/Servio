package com.ufrn.ppgti.servio.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ufrn.ppgti.servio.dto.request.OrderCreateRequestDTO;
import com.ufrn.ppgti.servio.dto.response.OrderResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.mappers.OrderMapper;
import com.ufrn.ppgti.servio.model.Availability;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.OrderStatus;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ServiceRepository serviceRepository;
    private final AuthService authService;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository,
            ServiceRepository serviceRepository,
            AuthService authService,
            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.serviceRepository = serviceRepository;
        this.authService = authService;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderResponseDTO create(OrderCreateRequestDTO dto) {
        User currentUser = authService.getAuthenticadUser();
        // validateClient(currentUser);

        com.ufrn.ppgti.servio.model.Service service = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));

        if (!service.isActive()) {
            throw new BusinessException("Não é possível contratar um serviço inativo.");
        }

        ProviderProfile provider = service.getProvider();
        if (provider == null) {
            throw new BusinessException("O serviço selecionado não possui prestador associado.");
        }
        if (provider.getUser() != null && provider.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Você não pode reservar o seu próprio serviço.");
        }

        int durationInMinutes = service.getDurationInMinutes();
        if (durationInMinutes <= 0) {
            throw new BusinessException("O serviço possui uma duração inválida para agendamento.");
        }

        LocalTime endTime = dto.getStartTime().plusMinutes(durationInMinutes);
        validateAvailability(provider, dto.getDate().getDayOfWeek(), dto.getDate(), dto.getStartTime(), endTime);
        validateProviderConflict(provider.getId(), dto.getDate(), dto.getStartTime(), endTime);

        Order order = new Order();
        order.setClient(currentUser);
        order.setService(service);
        order.setProvider(provider);
        order.setDate(dto.getDate());
        order.setStartTime(dto.getStartTime());
        order.setEndTime(endTime);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        order = orderRepository.save(order);
        return orderMapper.toResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> findMyOrdersAsClient() {
        User currentUser = authService.getAuthenticadUser();
        validateClient(currentUser);

        return orderRepository.findByClient_IdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(orderMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> findMyOrdersAsProvider() {
        User currentUser = authService.getAuthenticadUser();
        validateProvider(currentUser);

        return orderRepository.findByProvider_IdOrderByCreatedAtDesc(currentUser.getProviderProfile().getId())
                .stream()
                .map(orderMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public OrderResponseDTO updateStatus(Long orderId, OrderStatus status) {
        User currentUser = authService.getAuthenticadUser();
        Order order;

        if (currentUser.getRole() == Role.PROVIDER) {
            validateProvider(currentUser);
            order = orderRepository.findByIdAndProvider_Id(orderId, currentUser.getProviderProfile().getId())
                    .orElseThrow(() -> new BusinessException("Pedido não encontrado para o prestador autenticado."));
            validateProviderStatusTransition(order.getStatus(), status);
        } else if (currentUser.getRole() == Role.CLIENT) {
            order = orderRepository.findByIdAndClient_Id(orderId, currentUser.getId())
                    .orElseThrow(() -> new BusinessException("Pedido não encontrado para o cliente autenticado."));
            validateClientStatusTransition(order.getStatus(), status);
        } else {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessException("Pedido não encontrado."));
        }

        order.setStatus(status);
        order = orderRepository.save(order);
        return orderMapper.toResponseDTO(order);
    }

    private void validateClient(User user) {
        if (user.getRole() != Role.CLIENT) {
            throw new BusinessException("Apenas clientes podem criar e listar seus pedidos.");
        }
    }

    private void validateProvider(User user) {
        if (user.getRole() != Role.PROVIDER || user.getProviderProfile() == null) {
            throw new BusinessException("Apenas prestadores podem acessar os pedidos do prestador.");
        }
    }

    private void validateAvailability(ProviderProfile provider,
            DayOfWeek targetDayOfWeek,
            java.time.LocalDate targetDate,
            LocalTime startTime,
            LocalTime endTime) {

        List<Availability> availabilitySlots = provider.getAvailabilitySlots();
        if (availabilitySlots == null || availabilitySlots.isEmpty()) {
            throw new BusinessException("O prestador não possui horários disponíveis cadastrados.");
        }

        boolean matchesAvailability = availabilitySlots.stream()
                .filter(Availability::getIsAvailable)
                .anyMatch(slot -> isMatchingDate(slot, targetDate, targetDayOfWeek)
                        && !startTime.isBefore(slot.getStartTime())
                        && !endTime.isAfter(slot.getEndTime()));

        if (!matchesAvailability) {
            throw new BusinessException("O horário selecionado não está disponível para esse prestador.");
        }
    }

    private boolean isMatchingDate(Availability slot, java.time.LocalDate targetDate, DayOfWeek targetDayOfWeek) {
        if (slot.getSpecificDate() != null) {
            return slot.getSpecificDate().equals(targetDate);
        }
        return slot.getDayOfWeek() != null && slot.getDayOfWeek().equals(targetDayOfWeek);
    }

    private void validateProviderConflict(Long providerId,
            java.time.LocalDate date,
            LocalTime startTime,
            LocalTime endTime) {
        boolean hasConflict = orderRepository
                .existsByProvider_IdAndDateAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                        providerId,
                        date,
                        OrderStatus.CANCELLED,
                        endTime,
                        startTime);

        if (hasConflict) {
            throw new BusinessException("Já existe um pedido para esse prestador no horário selecionado.");
        }
    }

    private void validateProviderStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.COMPLETED) {
            throw new BusinessException("Não é possível alterar o status de um pedido finalizado ou cancelado.");
        }

        boolean validTransition = (currentStatus == OrderStatus.PENDING &&
                (newStatus == OrderStatus.IN_PROGRESS || newStatus == OrderStatus.CANCELLED))
                || (currentStatus == OrderStatus.IN_PROGRESS && newStatus == OrderStatus.COMPLETED);

        if (!validTransition) {
            throw new BusinessException("Transição de status inválida para o prestador.");
        }
    }

    private void validateClientStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (newStatus != OrderStatus.CANCELLED) {
            throw new BusinessException("O cliente só pode cancelar pedidos.");
        }

        if (currentStatus != OrderStatus.PENDING) {
            throw new BusinessException("O cliente só pode cancelar pedidos pendentes.");
        }
    }
}
