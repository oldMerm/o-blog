package io.github.oldmerman.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_count")
@Builder
public class Counter {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Byte countType;

    private Long count;

    private LocalDateTime createdAt;

    public static class Type {
        public static final Byte USER = 1;
        public static final Byte ARTICLE = 2;
    }
}
