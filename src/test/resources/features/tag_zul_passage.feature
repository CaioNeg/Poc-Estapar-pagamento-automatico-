# language: pt
Funcionalidade: Pagamento Automatico Tag Zul+ integrado a ConectCar
  Como um motorista cliente do Zul+ da Estapar
  Eu quero ativar minha Tag e passar automaticamente por cancelas de estacionamentos e pedagios
  Para realizar pagamentos sem fila e com conciliacao automatica na ConectCar

  Cenário: Fluxo completo de ativacao de Tag Zul+, passagem em estacionamento Estapar via REST com JWT e recarga
    Dado que um motorista se cadastra com nome "Roberto Silva", email "roberto@estapar.com.br" e CPF "12312312399"
    E ativa uma Tag Zul+ de codigo "ZUL-ESTAPAR-01" para a placa "EST2A26" com saldo inicial de 50.00
    Quando o veiculo realiza uma passagem do tipo "PARKING" no local "Estapar Shopping Ibirapuera" no valor de 22.00
    Então a passagem deve ser registrada com sucesso e o saldo restante da Tag "ZUL-ESTAPAR-01" deve ser 28.00
    Quando o motorista realiza uma recarga de 32.00 na Tag "ZUL-ESTAPAR-01"
    Então o saldo atualizado da Tag "ZUL-ESTAPAR-01" deve ser 60.00

  Cenário: Validacao em tempo real na cancela de pedagio via gRPC e bloqueio por saldo insuficiente
    Dado que um motorista se cadastra com nome "Fernanda Costa", email "fernanda@estapar.com.br" e CPF "32132132188"
    E ativa uma Tag Zul+ de codigo "ZUL-GRPC-02" para a placa "GRP9C99" com saldo inicial de 20.00
    Quando a cancela valida via gRPC a Tag "ZUL-GRPC-02" no pedagio "Pedagio Bandeirantes KM 36" com tarifa de "14.50"
    Então a cancela gRPC deve responder autorizada "true" com saldo restante "5.50"
    Quando a cancela valida via gRPC a Tag "ZUL-GRPC-02" no pedagio "Pedagio Castello Branco" com tarifa de "12.00"
    Então a cancela gRPC deve responder autorizada "false" por saldo insuficiente
