package backend.academy.scrapper.service.mappers;

import backend.academy.scrapper.repository.dto.LinkDto;
import backend.academy.scrapper.repository.dto.LinkInfo;
import backend.academy.scrapper.repository.entities.ChatIdEntity;
import backend.academy.scrapper.repository.entities.LinkEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static LinkDto parseToDto(LinkEntity link) {
        return LinkDto.builder()
            .linkId(link.getLinkId())
            .url(link.getUrl())
            .linkInfo(parseInfo(link))
            .linkType(link.getLinkType())
            .chatIds(link.getChatIds().stream()
                .map(ChatIdEntity::getChatId)
                .collect(Collectors.toSet()))
            .build();
    }

    public static LinkEntity parseToEntity(LinkDto link) {
        return new LinkEntity()
            .setLinkId(link.getLinkId())
            .setUrl(link.getUrl())
            .setLinkInfo(writeInfo(link.getLinkInfo()))
            .setLinkType(link.getLinkType())
            .setChatIds(link.getChatIds().stream()
                .map(chatId -> new ChatIdEntity().setChatId(chatId))
                .toList());
    }

    @SneakyThrows
    private static LinkInfo parseInfo(LinkEntity link) {
        return MAPPER.readValue(link.getLinkInfo(), link.getLinkType().getInfoType());
    }

    @SneakyThrows
    private static String writeInfo(LinkInfo info) {
        return MAPPER.writeValueAsString(info);
    }
}
