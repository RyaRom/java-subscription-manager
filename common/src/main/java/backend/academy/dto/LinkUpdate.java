package backend.academy.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record LinkUpdate(Long linkId, String url, String description, List<Long> tgChatIds) {}
