package com.ufrn.ppgti.servio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.model.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByClientIdAndServiceId(Long clientId, Long serviceId);

    List<Conversation> findByClientIdOrProviderId(Long clientId, Long providerId);
}
