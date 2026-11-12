package br.com.estapar.poc.domain.repository;

import br.com.estapar.poc.domain.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDocumentCpf(String documentCpf);
}
