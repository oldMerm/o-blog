package io.github.oldmerman.web.agent.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.oldmerman.common.response.PageResult;
import io.github.oldmerman.core.tool.Param;
import io.github.oldmerman.core.tool.Tool;
import io.github.oldmerman.model.po.Version;
import io.github.oldmerman.web.service.VersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemTools {

    private final VersionService service;

    @Qualifier("ignoreNullMapper")
    private final ObjectMapper objectMapper;

    @Tool(name = "get_version_history", description = "分页获取鱼人博客历史版本信息(按创建时间降序，第一条即为当前版本)")
    public String getVersionHistory(@Param(description = "当前页数", required = false) Long current,
                                    @Param(description = "每页大小", required = false) Long size) throws JsonProcessingException {
        PageResult<Version> page = service.page(
                (current == null ? 1 : current),
                (size == null ? 5 : size)
        );

        ObjectNode node = objectMapper.createObjectNode();
        List<Version> records = page.getRecords().stream().peek(i -> i.setId(null)).toList();
        node.put("total", page.getTotal());
        node.put("records", objectMapper.writeValueAsString(records));
        return node.toString();
    }

}
