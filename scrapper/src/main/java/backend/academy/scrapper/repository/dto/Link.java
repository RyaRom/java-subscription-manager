package backend.academy.scrapper.repository.dto;

import backend.academy.dto.LinkResponse;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public final class Link {
    @Builder.Default
    private Long linkId = UUID.randomUUID().timestamp();
    private String url;

    @Nullable
    private GithubInfo githubInfo;

    private Type linkType;
    private List<Long> chatIds;

    public LinkResponse toLinkResponse() {
        return new LinkResponse(
                linkId, url,
                List.of(), List.of());
    }

    public enum Type {
        GITHUB,
        STACK_OVERFLOW
    }
}
