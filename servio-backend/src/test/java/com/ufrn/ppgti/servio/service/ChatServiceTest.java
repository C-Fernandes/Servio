package com.ufrn.ppgti.servio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.response.ConversationResponseDTO;
import com.ufrn.ppgti.servio.dto.response.MessageResponseDTO;
import com.ufrn.ppgti.servio.model.Conversation;
import com.ufrn.ppgti.servio.model.Message;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.Role;
import com.ufrn.ppgti.servio.repository.ConversationRepository;
import com.ufrn.ppgti.servio.repository.MessageRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService - SPEC-008 (RF-16 Chat entre Cliente e Prestador)")
class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private AuthService authService;

    @InjectMocks
    private ChatService chatService;

    private static final Long CLIENT_ID = 1L;
    private static final Long PROVIDER_ID = 2L;
    private static final Long SERVICE_ID = 10L;
    private static final Long CONVERSATION_ID = 100L;

    @Test
    @DisplayName("Cenário 1: cliente inicia uma conversa a partir de um serviço de outro prestador")
    void startConversation_newPair_createsConversation() {
        User client = clientUser(CLIENT_ID);
        com.ufrn.ppgti.servio.model.Service service = serviceOf(PROVIDER_ID);

        when(authService.getAuthenticadUser()).thenReturn(client);
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service));
        when(conversationRepository.findByClientIdAndServiceId(CLIENT_ID, SERVICE_ID)).thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> {
            Conversation c = inv.getArgument(0);
            c.setId(CONVERSATION_ID);
            return c;
        });
        when(messageRepository.findFirstByConversationIdOrderBySentAtDesc(CONVERSATION_ID))
                .thenReturn(Optional.empty());
        when(messageRepository.countByConversationIdAndSenderIdNotAndReadFalse(CONVERSATION_ID, CLIENT_ID))
                .thenReturn(0L);

        ConversationResponseDTO result = chatService.startConversation(SERVICE_ID);

        assertEquals(CONVERSATION_ID, result.getId());
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    @DisplayName("Cenário 2: reabrir a mesma página reaproveita a conversa existente, sem duplicar")
    void startConversation_existingPair_reusesConversation() {
        User client = clientUser(CLIENT_ID);
        com.ufrn.ppgti.servio.model.Service service = serviceOf(PROVIDER_ID);
        Conversation existing = new Conversation(client, service.getProvider().getUser(), service);
        existing.setId(CONVERSATION_ID);

        when(authService.getAuthenticadUser()).thenReturn(client);
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service));
        when(conversationRepository.findByClientIdAndServiceId(CLIENT_ID, SERVICE_ID))
                .thenReturn(Optional.of(existing));
        when(messageRepository.findFirstByConversationIdOrderBySentAtDesc(CONVERSATION_ID))
                .thenReturn(Optional.empty());
        when(messageRepository.countByConversationIdAndSenderIdNotAndReadFalse(CONVERSATION_ID, CLIENT_ID))
                .thenReturn(0L);

        ConversationResponseDTO result = chatService.startConversation(SERVICE_ID);

        assertEquals(CONVERSATION_ID, result.getId());
        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cenário 3: ao abrir a conversa, mensagens não lidas do outro participante são marcadas como lidas")
    void getMessages_unreadFromOtherParticipant_marksAsRead() {
        User client = clientUser(CLIENT_ID);
        User provider = clientUser(PROVIDER_ID);
        Conversation conversation = conversationOf(client, provider);
        conversation.setId(CONVERSATION_ID);

        Message unreadFromProvider = message(conversation, provider, "Oi, posso ajudar?", false);
        Message alreadyReadFromMe = message(conversation, client, "Quero saber mais", true);

        when(authService.getAuthenticadUser()).thenReturn(client);
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderBySentAtAsc(CONVERSATION_ID))
                .thenReturn(List.of(alreadyReadFromMe, unreadFromProvider));

        List<MessageResponseDTO> result = chatService.getMessages(CONVERSATION_ID);

        assertEquals(2, result.size());
        verify(messageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Edge 1: usuário que não participa da conversa não pode acessá-la")
    void getMessages_notParticipant_throws403() {
        User client = clientUser(CLIENT_ID);
        User provider = clientUser(PROVIDER_ID);
        Conversation conversation = conversationOf(client, provider);
        conversation.setId(CONVERSATION_ID);

        User outsider = clientUser(999L);

        when(authService.getAuthenticadUser()).thenReturn(outsider);
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> chatService.getMessages(CONVERSATION_ID));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Edge 2: prestador não pode iniciar uma conversa")
    void startConversation_byProvider_throws403() {
        User providerAsSender = clientUser(PROVIDER_ID);
        providerAsSender.setRole(Role.PROVIDER);

        when(authService.getAuthenticadUser()).thenReturn(providerAsSender);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> chatService.startConversation(SERVICE_ID));
        assertEquals(403, ex.getStatusCode().value());
        verify(serviceRepository, never()).findById(any());
    }

    // ----- helpers -----

    private User clientUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("Usuário " + id);
        user.setRole(Role.CLIENT);
        return user;
    }

    private com.ufrn.ppgti.servio.model.Service serviceOf(Long providerUserId) {
        com.ufrn.ppgti.servio.model.Service service = new com.ufrn.ppgti.servio.model.Service();
        service.setId(SERVICE_ID);
        service.setTitle("Lancha");

        User providerUserAccount = clientUser(providerUserId);

        ProviderProfile provider = new ProviderProfile();
        provider.setId(providerUserId);
        provider.setUser(providerUserAccount);
        service.setProvider(provider);

        return service;
    }

    private Conversation conversationOf(User client, User provider) {
        com.ufrn.ppgti.servio.model.Service service = new com.ufrn.ppgti.servio.model.Service();
        service.setId(SERVICE_ID);
        service.setTitle("Lancha");
        return new Conversation(client, provider, service);
    }

    private Message message(Conversation conversation, User sender, String content, boolean read) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message.setRead(read);
        return message;
    }
}
