package br.com.estapar.poc.service;

import br.com.estapar.poc.api.dto.CreateTagRequest;
import br.com.estapar.poc.api.dto.RechargeTagRequest;
import br.com.estapar.poc.api.dto.TagResponse;
import br.com.estapar.poc.domain.model.Customer;
import br.com.estapar.poc.domain.model.Tag;
import br.com.estapar.poc.domain.repository.CustomerRepository;
import br.com.estapar.poc.domain.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final CustomerRepository customerRepository;

    public TagService(TagRepository tagRepository, CustomerRepository customerRepository) {
        this.tagRepository = tagRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public TagResponse activateTag(CreateTagRequest request) {
        String normalizedPlate = request.licensePlate().toUpperCase();

        if (tagRepository.existsByTagCode(request.tagCode())) {
            throw new IllegalArgumentException("Ja existe uma Tag Zul+ ativa com o codigo informado.");
        }
        if (tagRepository.existsByLicensePlate(normalizedPlate)) {
            throw new IllegalArgumentException("Ja existe uma Tag Zul+ vinculada a esta placa.");
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado para o ID informado."));

        Tag tag = new Tag(
                request.tagCode(),
                normalizedPlate,
                customer,
                request.initialBalance()
        );

        Tag saved = tagRepository.save(tag);
        return TagResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TagResponse getByTagCode(String tagCode) {
        Tag tag = findTagEntityByCode(tagCode);
        return TagResponse.fromEntity(tag);
    }

    @Transactional
    public TagResponse rechargeBalance(String tagCode, RechargeTagRequest request) {
        Tag tag = findTagEntityByCode(tagCode);
        tag.creditBalance(request.amount());
        Tag saved = tagRepository.save(tag);
        return TagResponse.fromEntity(saved);
    }

    @Transactional
    public TagResponse blockTag(String tagCode) {
        Tag tag = findTagEntityByCode(tagCode);
        tag.block();
        return TagResponse.fromEntity(tagRepository.save(tag));
    }

    @Transactional(readOnly = true)
    public Tag findTagEntityByCode(String tagCode) {
        return tagRepository.findByTagCode(tagCode)
                .orElseThrow(() -> new EntityNotFoundException("Tag Zul+ nao encontrada para o codigo: " + tagCode));
    }
}
