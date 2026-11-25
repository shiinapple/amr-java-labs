package cn.shiinapple.amr.tcpframing;

import java.util.Arrays;
import java.util.List;

public final class MyFrameDecoder {
    private static final int HEADER_BYTES = 2;
    private static final int MAX_BODY_BYTES = 4096;

    List<byte[]> feed(byte[] chunk) {
        // 起步实现能读完整的一帧。它把每次收到的字节看成独立消息。
        if (chunk.length < HEADER_BYTES) {
            return List.of();
        }
        int length = (Byte.toUnsignedInt(chunk[0]) << 8) | Byte.toUnsignedInt(chunk[1]);
        if (length > MAX_BODY_BYTES) {
            throw new IllegalArgumentException("帧长超出上限：" + length);
        }
        int end = HEADER_BYTES + length;
        if (end > chunk.length) {
            return List.of();
        }
        return List.of(Arrays.copyOfRange(chunk, HEADER_BYTES, end));
    }
}
