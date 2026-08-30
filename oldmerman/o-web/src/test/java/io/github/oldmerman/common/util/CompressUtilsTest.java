package io.github.oldmerman.common.util;

import io.github.oldmerman.web.util.CompressUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest(classes = CompressUtils.class)
class CompressUtilsTest {

    @Test
    public void gzipTest() throws IOException {
        String s = CompressUtils.readLastWeekLog("C:\\Users\\asus\\Desktop\\error");
        System.out.println(s);
    }
}