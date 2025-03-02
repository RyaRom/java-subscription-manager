package backend.academy.bot.repository;

import lombok.Builder;
import lombok.Data;
import java.util.List;

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
