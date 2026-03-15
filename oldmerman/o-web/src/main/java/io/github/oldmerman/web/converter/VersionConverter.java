package io.github.oldmerman.web.converter;

import io.github.oldmerman.model.dto.VersionDTO;
import io.github.oldmerman.model.po.Version;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VersionConverter {

    Version createToVersion(VersionDTO dto);
}
