package io.github.oldmerman.web.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * 路径相关工具
 * @author oldmerman
 * @date 2026-3-13
 */
public class PathUtils {

    /**
     * 通过url返回ossKey
     * @param urls url的集合
     * @return keys
     */
    public static List<String> getOssPubKeys(List<String> urls){
        return urls.stream().map(url -> {
            try {
                return new URL(url).getPath().replaceFirst("^/", "");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }
}
