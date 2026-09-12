package com.ufrn.ppgti.servio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    Optional<Message> findFirstByConversationIdOrderBySentAtDesc(Long conversationId);

    long countByConversationIdAndSenderIdNotAndReadFalse(Long conversationId, Long senderId);

    List<Message> findByConversation_Client_IdAndConversation_Provider_IdOrderBySentAtAsc(Long clientId,
            Long providerId);
}
