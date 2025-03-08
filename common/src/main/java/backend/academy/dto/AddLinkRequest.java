package backend.academy.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public final class AddLinkRequest {
    private final String link;
    @Builder.Default
    private List<String> tags = List.of();
    @Builder.Default
    private List<String> filters = List.of();
}
