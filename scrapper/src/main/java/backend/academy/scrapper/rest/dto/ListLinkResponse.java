package backend.academy.scrapper.rest.dto;

import java.util.List;

public record ListLinkResponse(
    List<String> links,
    Integer size
) {
}
