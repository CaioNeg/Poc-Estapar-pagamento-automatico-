package br.com.estapar.poc.service;

import br.com.estapar.poc.api.dto.CreateTagRequest;
import br.com.estapar.poc.api.dto.RechargeTagRequest;
import br.com.estapar.poc.api.dto.TagResponse;
import br.com.estapar.poc.domain.model.Customer;
import br.com.estapar.poc.domain.model.Tag;
import br.com.estapar.poc.domain.model.TagStatus;
import br.com.estapar.poc.domain.repository.CustomerRepository;
import br.com.estapar.poc.domain.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    @DisplayName("Deve ativar nova Tag Zul+ com sucesso")
    void shouldActivateNewTagSuccessfully() {
        Customer customer = new Customer("Pedro", "pedro@estapar.com.br", "11122233344", "hash");
        CreateTagRequest request = new CreateTagRequest(1L, "ZUL-900", "bra2e19", new BigDecimal("100.00"));

        when(tagRepository.existsByTagCode("ZUL-900")).thenReturn(false);
        when(tagRepository.existsByLicensePlate("BRA2E19")).thenReturn(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        TagResponse response = tagService.activateTag(request);

        assertEquals("ZUL-900", response.tagCode());
        assertEquals("BRA2E19", response.licensePlate());
        assertEquals(new BigDecimal("100.00"), response.balance());
        assertEquals(TagStatus.ACTIVE, response.status());
    }

    @Test
    @DisplayName("Deve impedir ativacao de Tag com placa ja vinculada")
    void shouldRejectDuplicateLicensePlate() {
        CreateTagRequest request = new CreateTagRequest(1L, "ZUL-901", "bra2e19", new BigDecimal("50.00"));

        when(tagRepository.existsByTagCode("ZUL-901")).thenReturn(false);
        when(tagRepository.existsByLicensePlate("BRA2E19")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> tagService.activateTag(request));
    }

    @Test
    @DisplayName("Deve realizar recarga de saldo na Tag Zul+")
    void shouldRechargeTagBalance() {
        Customer customer = new Customer("Pedro", "pedro@estapar.com.br", "11122233344", "hash");
        Tag existingTag = new Tag("ZUL-900", "BRA2E19", customer, new BigDecimal("30.00"));

        when(tagRepository.findByTagCode("ZUL-900")).thenReturn(Optional.of(existingTag));
        when(tagRepository.save(existingTag)).thenReturn(existingTag);

        TagResponse response = tagService.rechargeBalance("ZUL-900", new RechargeTagRequest(new BigDecimal("40.00")));

        assertEquals(new BigDecimal("70.00"), response.balance());
    }
}
