package br.com.estapar.poc.domain.repository;

import br.com.estapar.poc.domain.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByTagCode(String tagCode);

    Optional<Tag> findByLicensePlate(String licensePlate);

    boolean existsByTagCode(String tagCode);

    boolean existsByLicensePlate(String licensePlate);
}
