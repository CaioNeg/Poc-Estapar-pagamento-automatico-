package br.com.estapar.poc.service;

import br.com.estapar.poc.api.dto.AuthResponse;
import br.com.estapar.poc.api.dto.LoginRequest;
import br.com.estapar.poc.api.dto.RegisterCustomerRequest;
import br.com.estapar.poc.domain.model.Customer;
import br.com.estapar.poc.domain.repository.CustomerRepository;
import br.com.estapar.poc.security.JwtTokenService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterCustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Ja existe um cliente cadastrado com o e-mail informado.");
        }
        if (customerRepository.existsByDocumentCpf(request.documentCpf())) {
            throw new IllegalArgumentException("Ja existe um cliente cadastrado com o CPF informado.");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        Customer customer = new Customer(
                request.name(),
                request.email(),
                request.documentCpf(),
                passwordHash
        );

        Customer saved = customerRepository.save(customer);
        String token = jwtTokenService.generateToken(saved);

        return new AuthResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                token,
                "Bearer",
                jwtTokenService.getExpirationSeconds()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais invalidas."));

        if (!passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            throw new BadCredentialsException("Credenciais invalidas.");
        }

        String token = jwtTokenService.generateToken(customer);
        return new AuthResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                token,
                "Bearer",
                jwtTokenService.getExpirationSeconds()
        );
    }
}
