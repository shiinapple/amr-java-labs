package cn.shiinapple.amr.tcpframing;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class FrameDecoderTest {
    private static byte[] frame(String text) {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[body.length + 2];
        bytes[0] = (byte) (body.length >>> 8);
        bytes[1] = (byte) body.length;
        System.arraycopy(body, 0, bytes, 2, body.length);
        return bytes;
    }

    private static List<String> feedText(byte[]... chunks) {
        var decoder = new MyFrameDecoder();
        List<String> decoded = new ArrayList<>();
        for (byte[] chunk : chunks) {
            List<String> current = decoder.feed(chunk).stream()
                    .map(body -> new String(body, StandardCharsets.UTF_8)).toList();
            decoded.addAll(current);
        }
        return decoded;
    }

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        baseline();
        if (failed > 0) {
            throw new AssertionError("请先通过基础用例。");
        }
        System.out.printf("基础测试通过 %d 项。%n", passed);
        if (Arrays.asList(args).contains("--goal")) {
            int basicPassed = passed;
            goal();
            System.out.printf("进阶测试通过 %d 项，未通过 %d 项。%n", passed - basicPassed, failed);
            if (failed > 0) {
                throw new AssertionError("进阶目标尚未达成，请编辑 MyFrameDecoder.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("一帧解出 UTF-8 正文", List.of("地图"),
                () -> feedText(frame("地图")));
        check("零长度的帧也要输出一个正文", 1,
                () -> feedText(frame("")).size());
        check("二进制正文按原字节保留", "00ff",
                () -> HexFormat.of().formatHex(new MyFrameDecoder().feed(new byte[]{0, 2, 0, (byte) 255}).get(0)));
    }

    private static void goal() {
        check("半个头、部分正文、剩余正文三次送入", List.of(0, 0, "ABC"),
                () -> {
                    var decoder = new MyFrameDecoder();
                    int first = decoder.feed(new byte[]{0}).size();
                    int second = decoder.feed(new byte[]{3, 65}).size();
                    byte[] body = decoder.feed(new byte[]{66, 67}).get(0);
                    return List.of(first, second, new String(body, StandardCharsets.UTF_8));
                });
        check("两帧一次收到", List.of("A", "B"),
                () -> feedText(new byte[]{0, 1, 65, 0, 1, 66}));
        check("第一个块含一帧和下帧的半个头", List.of("A", "B"),
                () -> feedText(new byte[]{0, 1, 65, 0}, new byte[]{1, 66}));
        check("跨块识别非法长度，清空后可读新帧", List.of(true, "C"),
                () -> {
                    var decoder = new MyFrameDecoder();
                    decoder.feed(new byte[]{16});
                    boolean rejected = false;
                    try {
                        decoder.feed(new byte[]{1});
                    } catch (IllegalArgumentException expected) {
                        rejected = true;
                    }
                    byte[] body = decoder.feed(frame("C")).get(0);
                    return List.of(rejected, new String(body, StandardCharsets.UTF_8));
                });
    }

    private static void check(String name, Object expected, Supplier<?> exercise) {
        try {
            Object actual = exercise.get();
            if (Objects.equals(expected, actual)) {
                passed++;
                System.out.printf("[通过] %s → %s%n", name, actual);
            } else {
                failed++;
                System.err.printf("[待通过] %s，预期 %s，实际 %s%n", name, expected, actual);
            }
        } catch (RuntimeException error) {
            failed++;
            System.err.printf("[待通过] %s，运行出错 %s%n", name, error);
        }
    }
}
