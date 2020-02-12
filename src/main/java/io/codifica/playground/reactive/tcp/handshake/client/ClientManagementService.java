package io.codifica.playground.reactive.tcp.handshake.client;

import io.netty.channel.ChannelOption;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClientManagementService {

    private static Map<String, TcpClient> ipToTcpClientMap = new ConcurrentHashMap<>();

    public Mono<TcpClient> addClient(String ip) {
        TcpClient client = TcpClient.create()
                .host("localhost")
                .port(7654)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .wiretap(true);

        ipToTcpClientMap.put(ip, client);
        return Mono.just(client);
    }

    public Mono<TcpClient> retrieveClient(String ip) {
        TcpClient client = ipToTcpClientMap.get(ip);

        if (client == null) {
            return Mono.empty();
        }

        return Mono.just(client);
    }

}
