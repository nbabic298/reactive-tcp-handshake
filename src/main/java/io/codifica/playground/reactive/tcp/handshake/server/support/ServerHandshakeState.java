package io.codifica.playground.reactive.tcp.handshake.server.support;

import io.codifica.playground.reactive.tcp.handshake.util.DataTypeUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@Getter
@AllArgsConstructor
public enum ServerHandshakeState {

    RCV_INIT_REQ(new byte[] {1, 2, 3}, new byte[] {2, 3, 4}, 1),
    RCV_FINAL_REQ(new byte[] {5, 6, 7}, new byte[] {6, 7, 8}, null);

    private byte[] expectedPayload;

    private byte[] responsePayload;

    private Integer nextStateOrdinal;

    public boolean receivedPayloadMatchesExpected(byte[] receivedPayload) {
        log.info("Comparing received payload (HEX): {} and expected payload (HEX): {}",
                DataTypeUtil.bytesToHex(receivedPayload), DataTypeUtil.bytesToHex(expectedPayload));
        return Arrays.equals(receivedPayload, expectedPayload);
    }

}
