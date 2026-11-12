package br.com.estapar.poc.service;

import br.com.estapar.poc.api.dto.AuthResponse;
import br.com.estapar.poc.api.dto.LoginRequest;
import br.com.estapar.poc.api.dto.RegisterCustomerRequest;
import br.com.estapar.poc.domain.model.Customer;
import br.com.estapar.poc.domain.repository.CustomerRepository;
import br.com.estapar.poc.security.JwtTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Deve cadastrar novo cliente e retornar token JWT")
    void shouldRegisterCustomerAndReturnJwtToken() {
        RegisterCustomerRequest request = new RegisterCustomerRequest(
                "Ana Motorista",
                "ana@estapar.com.br",
                "98765432100",
                "senhaSegura123"
        );

        when(customerRepository.existsByEmail(request.email())).thenReturn(false);
        when(customerRepository.existsByDocumentCpf(request.documentCpf())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.generateToken(any(Customer.class))).thenReturn("jwt-token-mock");
        when(jwtTokenService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("ana@estapar.com.br", response.email());
        assertEquals("jwt-token-mock", response.accessToken());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar cadastrar e-mail duplicado")
    void shouldThrowExceptionWhenRegisteringDuplicateEmail() {
        RegisterCustomerRequest request = new RegisterCustomerRequest(
                "Ana Motorista",
                "ana@estapar.com.br",
                "98765432100",
                "senhaSegura123"
        );

        when(customerRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve autenticar cliente com sucesso e retornar token JWT")
    void shouldLoginCustomerSuccessfully() {
        LoginRequest request = new LoginRequest("ana@estapar.com.br", "senhaSegura123");
        Customer customer = new Customer("Ana Motorista", "ana@estapar.com.br", "98765432100", "hashedPassword");

        when(customerRepository.findByEmail(request.email())).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(request.password(), "hashedPassword")).thenReturn(true);
        when(jwtTokenService.generateToken(customer)).thenReturn("jwt-login-token");
        when(jwtTokenService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(request);

        assertEquals("jwt-login-token", response.accessToken());
    }

    @Test
    @DisplayName("Deve rejeitar login com senha incorreta")
    void shouldRejectLoginWithWrongPassword() {
        LoginRequest request = new LoginRequest("ana@estapar.com.br", "senhaErrada");
        Customer customer = new Customer("Ana Motorista", "ana@estapar.com.br", "98765432100", "hashedPassword");

        when(customerRepository.findByEmail(request.email())).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(request.password(), "hashedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
