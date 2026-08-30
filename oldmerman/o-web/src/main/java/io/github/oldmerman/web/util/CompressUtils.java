package io.github.oldmerman.web.util;

import io.github.oldmerman.common.constant.DateTimePatterns;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * 压缩文件处理工具类
 * @author oldmerman
 * @date 2026-8-21
 */
public class CompressUtils {

    // 读取文件的阈值，10MB
    private static final long SIZE_THRESHOLD = 10 * 1024 * 1024;

    public static String readLastWeekLog(String logPath) throws IOException{
        LocalDate now = LocalDate.now();
        LocalDate oneWeekAgo = now.minusDays(30);

        Path dir = Paths.get(logPath);
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("路径不是目录: " + logPath);
        }

        List<String> filePathList;
        try (Stream<Path> paths = Files.list(dir)) {
            filePathList = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String timeStr = path.getFileName().toString().split("\\.")[1];
                        LocalDate logTime = LocalDate.parse(timeStr, DateTimePatterns.LOG_TIME_PATTERN);
                        return logTime.isAfter(oneWeekAgo) && logTime.isBefore(now);
                    })
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }

        StringBuilder builder = new StringBuilder();
        String logStringPrefixPatterns = "==== 日志文件: %s ====\n";
        for (String path : filePathList) {
            System.out.println(path);
            String logPrefix = String.format(logStringPrefixPatterns, path);
            String logString = readGzipAdaptively(path);
            builder.append(logPrefix);
            builder.append(logString);
            builder.append("\n");
        }

        return builder.toString();
    }

    /**
     * 解压.gz文件
     */
    public static String readGzipAdaptively(String filePath) throws IOException {
        Path path = Path.of(filePath);
        long filesize = Files.size(path);
        // 根据文件大小读取
        if (filesize <= SIZE_THRESHOLD) {
            return readSmallGzip(path);
        } else {
            return readLargeGzip(path);
        }
    }

    /**
     * 小文件处理 .gz
     */
    private static String readSmallGzip(Path path) throws IOException {
        try(GZIPInputStream gis = new GZIPInputStream(
                new FileInputStream(path.toFile()))){
            return new String(gis.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * 大文件处理 .gz
     */
    private static String readLargeGzip(Path path) throws IOException {
         try(GZIPInputStream gis = new GZIPInputStream(
                 new FileInputStream(path.toFile()));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

             byte[] buffer = new byte[8192];
             int len;
             while ((len = gis.read(buffer)) > 0) {
                 baos.write(buffer, 0, len);
                 if (baos.size() > SIZE_THRESHOLD * 1.5) {
                     throw new IOException("文件过大");
                 }
             }
             return baos.toString(StandardCharsets.UTF_8);
         }
    }
}
