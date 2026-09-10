package com.ufrn.ppgti.servio.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.response.ConversationResponseDTO;
import com.ufrn.ppgti.servio.dto.response.MessageResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.Conversation;
import com.ufrn.ppgti.servio.model.Message;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.ConversationRepository;
import com.ufrn.ppgti.servio.repository.MessageRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ServiceRepository serviceRepository;
    private final AuthService authService;

    public ChatService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            ServiceRepository serviceRepository,
            AuthService authService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.serviceRepository = serviceRepository;
        this.authService = authService;
    }

    @Transactional
    public ConversationResponseDTO startConversation(Long serviceId) {
        User client = authService.getAuthenticadUser();

        if (client.getRole() != Role.CLIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas clientes podem iniciar uma conversa.");
        }

        com.ufrn.ppgti.servio.model.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado."));

        if (service.isDeleted()) {
            throw new BusinessException("Serviço não está disponível para conversa.");
        }

        if (service.getProvider() == null || service.getProvider().getUser() == null) {
            throw new BusinessException("Serviço sem prestador associado.");
        }

        User provider = service.getProvider().getUser();

        if (provider.getId().equals(client.getId())) {
            throw new BusinessException("Você não pode iniciar uma conversa consigo mesmo.");
        }

        Conversation conversation = conversationRepository.findByClientIdAndServiceId(client.getId(), serviceId)
                .orElseGet(() -> conversationRepository.save(new Conversation(client, provider, service)));

        return toConversationDTO(conversation, client);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponseDTO> listMyConversations() {
        User me = authService.getAuthenticadUser();

        List<Conversation> conversations = conversationRepository.findByClientIdOrProviderId(me.getId(), me.getId());

        return conversations.stream()
                .map(c -> toConversationDTO(c, me))
                .sorted(Comparator.comparing(
                        ConversationResponseDTO::getLastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public List<MessageResponseDTO> getMessages(Long conversationId) {
        User me = authService.getAuthenticadUser();
        requireParticipant(conversationId, me);

        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);

        List<Message> unread = messages.stream()
                .filter(m -> !m.getSender().getId().equals(me.getId()) && !m.isRead())
                .toList();

        if (!unread.isEmpty()) {
            unread.forEach(m -> m.setRead(true));
            messageRepository.saveAll(unread);
        }

        return messages.stream().map(m -> toMessageDTO(m, me)).toList();
    }

    @Transactional
    public MessageResponseDTO sendMessage(Long conversationId, String content) {
        User me = authService.getAuthenticadUser();
        Conversation conversation = requireParticipant(conversationId, me);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(me);
        message.setContent(content.trim());

        Message saved = messageRepository.save(message);

        return toMessageDTO(saved, me);
    }

    private Conversation requireParticipant(Long conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada."));

        boolean isParticipant = conversation.getClient().getId().equals(user.getId())
                || conversation.getProvider().getId().equals(user.getId());

        if (!isParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não participa desta conversa.");
        }

        return conversation;
    }

    private ConversationResponseDTO toConversationDTO(Conversation conversation, User me) {
        boolean isClient = conversation.getClient().getId().equals(me.getId());
        User other = isClient ? conversation.getProvider() : conversation.getClient();

        Message last = messageRepository.findFirstByConversationIdOrderBySentAtDesc(conversation.getId())
                .orElse(null);
        long unread = messageRepository.countByConversationIdAndSenderIdNotAndReadFalse(conversation.getId(),
                me.getId());

        return new ConversationResponseDTO(
                conversation.getId(),
                conversation.getService().getId(),
                conversation.getService().getTitle(),
                other.getId(),
                other.getName(),
                last != null ? last.getContent() : null,
                last != null ? last.getSentAt() : null,
                unread);
    }

    private MessageResponseDTO toMessageDTO(Message message, User me) {
        return new MessageResponseDTO(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getSender().getId().equals(me.getId()),
                message.getContent(),
                message.getSentAt());
    }
}
