package backend.academy.scrapper.repository.dto;

import backend.academy.dto.LinkResponse;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class Link {
    private static final AtomicLong SIMULATE_COUNTER = new AtomicLong();
    // replace with spring data id
    @Builder.Default
    private Long linkId = SIMULATE_COUNTER.addAndGet(1L);

    @NonNull
    private String url;

    @Nullable
    private GithubInfo githubInfo;

    @Nullable
    private StackOverflowInfo stackOverflowInfo;

    @NonNull
    private Type linkType;

    @NonNull
    private Set<Long> chatIds;

    public LinkResponse toLinkResponse() {
        return new LinkResponse(linkId, url, List.of(), List.of());
    }

    public enum Type {
        GITHUB,
        STACK_OVERFLOW
    }

    public record StackOverflowInfo(Long questionId) {}

    public record GithubInfo(String owner, String repo) {}
}
