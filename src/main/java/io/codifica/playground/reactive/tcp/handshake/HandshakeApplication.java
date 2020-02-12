package io.codifica.playground.reactive.tcp.handshake;

import io.codifica.playground.reactive.tcp.handshake.client.ClientManagementService;
import io.codifica.playground.reactive.tcp.handshake.client.support.ClientHandshakeState;
import io.codifica.playground.reactive.tcp.handshake.util.DataTypeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@AllArgsConstructor
@SpringBootApplication
public class HandshakeApplication implements CommandLineRunner {

    private final ClientManagementService clientManagementService;

    public static void main(String[] args) {
        SpringApplication.run(HandshakeApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        clientManagementService.retrieveClient("localhost")
                .switchIfEmpty(clientManagementService.addClient("localhost"))
                .flatMap(TcpClient::connect)
                .retryBackoff(3L, Duration.ofSeconds(1))
                .flatMap(this::handleHandshake)
                .subscribe();

    }

    public Mono<Void> handleHandshake(Connection connection) {
        AtomicReference<ClientHandshakeState> handshakeState = new AtomicReference<>(ClientHandshakeState.SEND_INIT_REQ);

        log.info("Client is initiating custom handshake with payload: {} (HEX)",
                DataTypeUtil.bytesToHex(handshakeState.get().getPayload()));

        connection.outbound().sendByteArray(Mono.just(handshakeState.get().getPayload())).then().subscribe();

        return connection.inbound().receive()
                .asByteArray()
                .flatMap(bytes -> {
                    log.info("Client received HEX payload: {}", DataTypeUtil.bytesToHex(bytes));

                    if (handshakeState.get().receivedPayloadMatchesExpected(bytes)) {

                        if (handshakeState.get().getNextStateOrdinal() != null) {
                            ClientHandshakeState nextState = ClientHandshakeState.values()[handshakeState.get().getNextStateOrdinal()];
                            log.info("Client handshake state will become: {}", nextState);
                            handshakeState.set(nextState);

                            log.info("Client is sending new request payload: {} (HEX)",
                                    DataTypeUtil.bytesToHex(handshakeState.get().getPayload()));

                            connection.outbound().sendByteArray(Mono.just(handshakeState.get().getPayload()))
                                    .then()
                                    .subscribe();
                        } else {
                            log.info("Client has completed the custom handshake.");
                        }

                    } else {
                        return Mono.error(new IllegalStateException("Data received is not valid!"));
                    }

                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    log.error("Error occurred: {}", e.getMessage(), e);
                    return Mono.empty();
                })
                .then();
    }

}
