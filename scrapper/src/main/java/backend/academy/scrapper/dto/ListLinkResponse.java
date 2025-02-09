package backend.academy.scrapper.dto;

import java.util.List;

public record ListLinkResponse(
    List<String> links,
    Integer size
) {
}
