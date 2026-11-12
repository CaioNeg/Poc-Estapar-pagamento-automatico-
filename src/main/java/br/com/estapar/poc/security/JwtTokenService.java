package br.com.estapar.poc.security;

import br.com.estapar.poc.domain.model.Customer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            @Value("${app.security.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(Customer customer) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("estapar-tag-zul-plus")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(customer.getEmail())
                .claim("customerId", customer.getId())
                .claim("name", customer.getName())
                .claim("scope", "CUSTOMER")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
