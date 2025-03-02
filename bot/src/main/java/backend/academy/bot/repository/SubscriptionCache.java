package backend.academy.bot.repository;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class SubscriptionCache {
    @Builder.Default
    private Long linkId = -1L;

    @Builder.Default
    private List<String> tags = List.of();

    @Builder.Default
    private List<String> filters = List.of();
}
