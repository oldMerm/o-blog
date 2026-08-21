package io.github.oldmerman.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest(classes = CompressUtils.class)
class CompressUtilsTest {

    @Test
    public void gzipTest() throws IOException {
        String path = "C:\\Users\\asus\\Desktop\\error\\web-error.2026-07-23.log.gz";
        String s = CompressUtils.readGzipAdaptively(path);
        System.out.println(s);
    }
}