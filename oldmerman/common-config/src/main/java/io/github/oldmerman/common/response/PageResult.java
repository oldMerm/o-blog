package io.github.oldmerman.common.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页数据封装类
 */
@Data
@Accessors(chain = true)
public class PageResult<T> implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long current;
    
    private Long size;
    
    private Long total;
    
    private Long pages;
    
    private List<T> records;
    
    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(Long current, Long size, Long total, List<T> records) {
        Long pages = (total + size - 1) / size;
        return new PageResult<T>()
                .setCurrent(current)
                .setSize(size)
                .setTotal(total)
                .setPages(pages)
                .setRecords(records);
    }
}