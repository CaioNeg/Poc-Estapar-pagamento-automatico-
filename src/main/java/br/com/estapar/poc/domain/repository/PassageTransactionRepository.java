package br.com.estapar.poc.domain.repository;

import br.com.estapar.poc.domain.model.PassageTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassageTransactionRepository extends JpaRepository<PassageTransaction, Long> {

    List<PassageTransaction> findByTagIdOrderByOccurredAtDesc(Long tagId);

    List<PassageTransaction> findByTagTagCodeOrderByOccurredAtDesc(String tagCode);
}
