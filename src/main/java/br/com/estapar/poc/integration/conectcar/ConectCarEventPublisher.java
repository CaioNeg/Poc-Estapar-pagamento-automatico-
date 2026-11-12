package br.com.estapar.poc.integration.conectcar;

import br.com.estapar.poc.domain.repository.PassageTransactionRepository;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class ConectCarEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ConectCarEventPublisher.class);

    private final PassageTransactionRepository transactionRepository;
    private final SqsTemplate sqsTemplate;
    private final String queueName;

    public ConectCarEventPublisher(
            PassageTransactionRepository transactionRepository,
            @Autowired(required = false) SqsTemplate sqsTemplate,
            @Value("${app.conectcar.sqs-queue-name:conectcar-passage-events-queue}") String queueName) {
        this.transactionRepository = transactionRepository;
        this.sqsTemplate = sqsTemplate;
        this.queueName = queueName;
    }

    @Async
    @Transactional
    public CompletableFuture<String> publishAndReconcileAsync(ConectCarPassageEvent event) {
        String protocolId = "CONECTCAR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        try {
            if (sqsTemplate != null) {
                sqsTemplate.send(to -> to.queue(queueName).payload(event));
                log.info("Evento de passagem enviado para fila AWS SQS [{}]: txId={}", queueName, event.transactionId());
            }
        } catch (Exception ex) {
            log.warn("Fila AWS SQS indisponivel no momento ({}). Conciliando localmente protocolo={}",
                    ex.getMessage(), protocolId);
        }

        transactionRepository.findById(event.transactionId()).ifPresent(tx -> {
            tx.markApprovedByConectCar(protocolId);
            transactionRepository.save(tx);
        });

        return CompletableFuture.completedFuture(protocolId);
    }
}
