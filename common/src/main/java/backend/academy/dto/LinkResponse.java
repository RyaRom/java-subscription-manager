package backend.academy.dto;

import java.util.List;

public record LinkResponse(
    Long linkId,
    String url,
    List<String> tags,
    List<String> filters
) {
}
