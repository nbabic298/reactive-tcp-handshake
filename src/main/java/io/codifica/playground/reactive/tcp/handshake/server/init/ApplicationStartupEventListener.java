package io.codifica.playground.reactive.tcp.handshake.server.init;

import io.codifica.playground.reactive.tcp.handshake.server.handler.HandshakeHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

@Slf4j
@Component
@AllArgsConstructor
public class ApplicationStartupEventListener {

    private final HandshakeHandler handler;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Starting TCP Server on port 7654 ...");
        startTcpServer();
    }

    private void startTcpServer() {
        DisposableServer server = TcpServer.create()
                .port(7654)
                .wiretap(true)
                .handle(handler.handleInbound())
                .bindNow();
    }

}
