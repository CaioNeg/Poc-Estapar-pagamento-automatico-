package br.com.estapar.poc.integration.conectcar;

import br.com.estapar.poc.domain.repository.PassageTransactionRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.conectcar.sqs-listener-enabled", havingValue = "true")
public class ConectCarSqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(ConectCarSqsConsumer.class);

    private final PassageTransactionRepository transactionRepository;

    public ConectCarSqsConsumer(PassageTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @SqsListener("${app.conectcar.sqs-queue-name:conectcar-passage-events-queue}")
    @Transactional
    public void onPassageEvent(ConectCarPassageEvent event) {
        log.info("Recebido evento SQS da ConectCar para transacao id={}, tag={}",
                event.transactionId(), event.tagCode());

        transactionRepository.findById(event.transactionId()).ifPresent(tx -> {
            if (tx.getExternalConectcarId() == null) {
                String protocol = "CONECTCAR-SQS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                tx.markApprovedByConectCar(protocol);
                transactionRepository.save(tx);
            }
        });
    }
}
