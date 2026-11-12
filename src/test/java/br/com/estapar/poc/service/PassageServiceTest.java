package br.com.estapar.poc.service;

import br.com.estapar.poc.api.dto.PassageResponse;
import br.com.estapar.poc.api.dto.RegisterPassageRequest;
import br.com.estapar.poc.domain.model.Customer;
import br.com.estapar.poc.domain.model.PassageStatus;
import br.com.estapar.poc.domain.model.PassageTransaction;
import br.com.estapar.poc.domain.model.PassageType;
import br.com.estapar.poc.domain.model.Tag;
import br.com.estapar.poc.domain.repository.PassageTransactionRepository;
import br.com.estapar.poc.domain.repository.TagRepository;
import br.com.estapar.poc.integration.conectcar.ConectCarEventPublisher;
import br.com.estapar.poc.integration.conectcar.ConectCarPassageEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassageServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PassageTransactionRepository passageTransactionRepository;

    @Mock
    private ConectCarEventPublisher conectCarEventPublisher;

    @InjectMocks
    private PassageService passageService;

    @Test
    @DisplayName("Deve debitar saldo da Tag, registrar passagem e publicar evento assincrono para ConectCar")
    void shouldRegisterPassageAndPublishAsyncEvent() {
        Customer customer = new Customer("Julia", "julia@estapar.com.br", "55566677788", "hash");
        Tag tag = new Tag("ZUL-777", "JUL7A77", customer, new BigDecimal("80.00"));

        RegisterPassageRequest request = new RegisterPassageRequest(
                "ZUL-777",
                PassageType.PARKING,
                "Estapar Shopping Eldorado",
                new BigDecimal("25.00")
        );

        when(tagRepository.findByTagCode("ZUL-777")).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);
        when(passageTransactionRepository.save(any(PassageTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(conectCarEventPublisher.publishAndReconcileAsync(any(ConectCarPassageEvent.class)))
                .thenReturn(CompletableFuture.completedFuture("CONECTCAR-12345678"));

        PassageResponse response = passageService.registerPassage(request);

        assertEquals("ZUL-777", response.tagCode());
        assertEquals(PassageType.PARKING, response.passageType());
        assertEquals(new BigDecimal("25.00"), response.amount());
        assertEquals(PassageStatus.PENDING_CONECTCAR_SYNC, response.status());
        assertEquals(new BigDecimal("55.00"), tag.getBalance());
        verify(conectCarEventPublisher).publishAndReconcileAsync(any(ConectCarPassageEvent.class));
    }

    @Test
    @DisplayName("Deve recusar passagem quando saldo da Tag for insuficiente e nao publicar na ConectCar")
    void shouldRejectPassageWhenBalanceIsInsufficient() {
        Customer customer = new Customer("Julia", "julia@estapar.com.br", "55566677788", "hash");
        Tag tag = new Tag("ZUL-777", "JUL7A77", customer, new BigDecimal("10.00"));

        RegisterPassageRequest request = new RegisterPassageRequest(
                "ZUL-777",
                PassageType.TOLL,
                "Pedagio Imigrantes KM 32",
                new BigDecimal("35.30")
        );

        when(tagRepository.findByTagCode("ZUL-777")).thenReturn(Optional.of(tag));

        assertThrows(IllegalStateException.class, () -> passageService.registerPassage(request));
        verify(passageTransactionRepository, never()).save(any());
        verify(conectCarEventPublisher, never()).publishAndReconcileAsync(any());
    }
}
