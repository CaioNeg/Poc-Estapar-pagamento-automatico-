package br.com.estapar.poc.service;

import br.com.estapar.poc.api.dto.PassageResponse;
import br.com.estapar.poc.api.dto.RegisterPassageRequest;
import br.com.estapar.poc.domain.model.PassageStatus;
import br.com.estapar.poc.domain.model.PassageTransaction;
import br.com.estapar.poc.domain.model.Tag;
import br.com.estapar.poc.domain.repository.PassageTransactionRepository;
import br.com.estapar.poc.domain.repository.TagRepository;
import br.com.estapar.poc.integration.conectcar.ConectCarEventPublisher;
import br.com.estapar.poc.integration.conectcar.ConectCarPassageEvent;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PassageService {

    private final TagRepository tagRepository;
    private final PassageTransactionRepository passageTransactionRepository;
    private final ConectCarEventPublisher conectCarEventPublisher;

    public PassageService(
            TagRepository tagRepository,
            PassageTransactionRepository passageTransactionRepository,
            ConectCarEventPublisher conectCarEventPublisher) {
        this.tagRepository = tagRepository;
        this.passageTransactionRepository = passageTransactionRepository;
        this.conectCarEventPublisher = conectCarEventPublisher;
    }

    @Transactional
    public PassageResponse registerPassage(RegisterPassageRequest request) {
        Tag tag = tagRepository.findByTagCode(request.tagCode())
                .orElseThrow(() -> new EntityNotFoundException("Tag Zul+ nao encontrada: " + request.tagCode()));

        tag.debitPassage(request.amount());
        tagRepository.save(tag);

        PassageTransaction transaction = new PassageTransaction(
                tag,
                request.passageType(),
                request.locationName(),
                request.amount(),
                PassageStatus.PENDING_CONECTCAR_SYNC
        );

        PassageTransaction saved = passageTransactionRepository.save(transaction);

        ConectCarPassageEvent event = new ConectCarPassageEvent(
                saved.getId(),
                tag.getTagCode(),
                tag.getLicensePlate(),
                saved.getPassageType(),
                saved.getLocationName(),
                saved.getAmount(),
                saved.getOccurredAt()
        );

        conectCarEventPublisher.publishAndReconcileAsync(event);

        return PassageResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PassageResponse> listByTagCode(String tagCode) {
        return passageTransactionRepository.findByTagTagCodeOrderByOccurredAtDesc(tagCode)
                .stream()
                .map(PassageResponse::fromEntity)
                .toList();
    }
}
