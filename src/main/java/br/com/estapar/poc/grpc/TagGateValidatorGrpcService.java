package br.com.estapar.poc.grpc;

import br.com.estapar.poc.api.dto.PassageResponse;
import br.com.estapar.poc.api.dto.RegisterPassageRequest;
import br.com.estapar.poc.api.dto.TagResponse;
import br.com.estapar.poc.domain.model.PassageType;
import br.com.estapar.poc.grpc.proto.GatePassageRequest;
import br.com.estapar.poc.grpc.proto.GatePassageResponse;
import br.com.estapar.poc.grpc.proto.TagGateValidatorServiceGrpc;
import br.com.estapar.poc.service.PassageService;
import br.com.estapar.poc.service.TagService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;

@GrpcService
public class TagGateValidatorGrpcService extends TagGateValidatorServiceGrpc.TagGateValidatorServiceImplBase {

    private final PassageService passageService;
    private final TagService tagService;

    public TagGateValidatorGrpcService(PassageService passageService, TagService tagService) {
        this.passageService = passageService;
        this.tagService = tagService;
    }

    @Override
    public void validateAndAuthorizeGate(
            GatePassageRequest request,
            StreamObserver<GatePassageResponse> responseObserver) {
        try {
            PassageType passageType = PassageType.valueOf(request.getPassageType().toUpperCase());
            BigDecimal amount = new BigDecimal(request.getAmount());

            RegisterPassageRequest dto = new RegisterPassageRequest(
                    request.getTagCode(),
                    passageType,
                    request.getLocationName(),
                    amount
            );

            PassageResponse passage = passageService.registerPassage(dto);
            TagResponse updatedTag = tagService.getByTagCode(request.getTagCode());

            GatePassageResponse response = GatePassageResponse.newBuilder()
                    .setAuthorized(true)
                    .setStatus(passage.status().name())
                    .setMessage("Cancela liberada com sucesso via Tag Zul+")
                    .setTransactionId(passage.id() != null ? passage.id() : 0L)
                    .setRemainingBalance(updatedTag.balance().toPlainString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            GatePassageResponse rejected = GatePassageResponse.newBuilder()
                    .setAuthorized(false)
                    .setStatus("REJECTED")
                    .setMessage(ex.getMessage() != null ? ex.getMessage() : "Falha ao validar Tag na cancela")
                    .setTransactionId(0L)
                    .setRemainingBalance("0.00")
                    .build();

            responseObserver.onNext(rejected);
            responseObserver.onCompleted();
        }
    }
}
