package backend.academy.scrapper.rest.dto;

import java.util.List;

public record LinkResponse(
    Long linkId,
    String url,
    List<String> tags,
    List<String> filters
) {
}
