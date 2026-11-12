package br.com.estapar.poc.bdd;

import br.com.estapar.poc.api.dto.AuthResponse;
import br.com.estapar.poc.api.dto.CreateTagRequest;
import br.com.estapar.poc.api.dto.PassageResponse;
import br.com.estapar.poc.api.dto.RechargeTagRequest;
import br.com.estapar.poc.api.dto.RegisterCustomerRequest;
import br.com.estapar.poc.api.dto.RegisterPassageRequest;
import br.com.estapar.poc.api.dto.TagResponse;
import br.com.estapar.poc.domain.model.PassageType;
import br.com.estapar.poc.grpc.TagGateValidatorGrpcService;
import br.com.estapar.poc.grpc.proto.GatePassageRequest;
import br.com.estapar.poc.grpc.proto.GatePassageResponse;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TagZulPassageSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TagGateValidatorGrpcService grpcService;

    private String jwtToken;
    private Long currentCustomerId;
    private ResponseEntity<PassageResponse> lastPassageResponse;
    private GatePassageResponse lastGrpcResponse;

    @Dado("que um motorista se cadastra com nome {string}, email {string} e CPF {string}")
    public void cadastrarMotorista(String nome, String email, String cpf) {
        RegisterCustomerRequest request = new RegisterCustomerRequest(nome, email, cpf, "senha123456");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register",
                request,
                AuthResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        this.jwtToken = response.getBody().accessToken();
        this.currentCustomerId = response.getBody().customerId();
    }

    @E("ativa uma Tag Zul+ de codigo {string} para a placa {string} com saldo inicial de {bigdecimal}")
    public void ativarTagZul(String tagCode, String placa, BigDecimal saldoInicial) {
        CreateTagRequest request = new CreateTagRequest(currentCustomerId, tagCode, placa, saldoInicial);
        HttpEntity<CreateTagRequest> entity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<TagResponse> response = restTemplate.exchange(
                "/api/v1/tags",
                HttpMethod.POST,
                entity,
                TagResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(tagCode, response.getBody().tagCode());
    }

    @Quando("o veiculo realiza uma passagem do tipo {string} no local {string} no valor de {bigdecimal}")
    public void realizarPassagemRest(String tipo, String local, BigDecimal valor) {
        RegisterPassageRequest request = new RegisterPassageRequest(
                "ZUL-ESTAPAR-01",
                PassageType.valueOf(tipo),
                local,
                valor
        );
        HttpEntity<RegisterPassageRequest> entity = new HttpEntity<>(request, authHeaders());

        this.lastPassageResponse = restTemplate.exchange(
                "/api/v1/passages",
                HttpMethod.POST,
                entity,
                PassageResponse.class
        );
    }

    @Então("a passagem deve ser registrada com sucesso e o saldo restante da Tag {string} deve ser {bigdecimal}")
    public void verificarPassagemESaldo(String tagCode, BigDecimal saldoEsperado) {
        assertEquals(HttpStatus.CREATED, lastPassageResponse.getStatusCode());
        assertNotNull(lastPassageResponse.getBody());

        ResponseEntity<TagResponse> tagResponse = restTemplate.exchange(
                "/api/v1/tags/" + tagCode,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                TagResponse.class
        );

        assertEquals(HttpStatus.OK, tagResponse.getStatusCode());
        assertNotNull(tagResponse.getBody());
        assertEquals(0, saldoEsperado.compareTo(tagResponse.getBody().balance()));
    }

    @Quando("o motorista realiza uma recarga de {bigdecimal} na Tag {string}")
    public void realizarRecarga(BigDecimal valorRecarga, String tagCode) {
        RechargeTagRequest request = new RechargeTagRequest(valorRecarga);
        HttpEntity<RechargeTagRequest> entity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<TagResponse> response = restTemplate.exchange(
                "/api/v1/tags/" + tagCode + "/recharge",
                HttpMethod.POST,
                entity,
                TagResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Então("o saldo atualizado da Tag {string} deve ser {bigdecimal}")
    public void verificarSaldoAtualizado(String tagCode, BigDecimal saldoEsperado) {
        ResponseEntity<TagResponse> tagResponse = restTemplate.exchange(
                "/api/v1/tags/" + tagCode,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                TagResponse.class
        );

        assertEquals(HttpStatus.OK, tagResponse.getStatusCode());
        assertNotNull(tagResponse.getBody());
        assertEquals(0, saldoEsperado.compareTo(tagResponse.getBody().balance()));
    }

    @Quando("a cancela valida via gRPC a Tag {string} no pedagio {string} com tarifa de {string}")
    public void validarCancelaViaGrpc(String tagCode, String local, String tarifa) {
        GatePassageRequest grpcRequest = GatePassageRequest.newBuilder()
                .setTagCode(tagCode)
                .setPassageType("TOLL")
                .setLocationName(local)
                .setAmount(tarifa)
                .build();

        AtomicReference<GatePassageResponse> ref = new AtomicReference<>();
        grpcService.validateAndAuthorizeGate(grpcRequest, new StreamObserver<>() {
            @Override
            public void onNext(GatePassageResponse value) {
                ref.set(value);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
            }
        });

        this.lastGrpcResponse = ref.get();
    }

    @Então("a cancela gRPC deve responder autorizada {string} com saldo restante {string}")
    public void verificarRespostaGrpcAutorizada(String esperadoAutorizado, String saldoRestanteEsperado) {
        assertNotNull(lastGrpcResponse);
        assertEquals(Boolean.parseBoolean(esperadoAutorizado), lastGrpcResponse.getAuthorized());
        assertEquals(
                0,
                new BigDecimal(saldoRestanteEsperado).compareTo(new BigDecimal(lastGrpcResponse.getRemainingBalance()))
        );
    }

    @Então("a cancela gRPC deve responder autorizada {string} por saldo insuficiente")
    public void verificarRespostaGrpcNegada(String esperadoAutorizado) {
        assertNotNull(lastGrpcResponse);
        assertEquals(Boolean.parseBoolean(esperadoAutorizado), lastGrpcResponse.getAuthorized());
        assertEquals("REJECTED", lastGrpcResponse.getStatus());
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        return headers;
    }
}
