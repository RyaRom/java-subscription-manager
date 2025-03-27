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

    public record StackOverflowInfo(Long questionId) {
        public static StackOverflowInfo parseStackOverflowInfo(String url) {
            var tokens = List.of(url.split("/"));
            if (tokens.contains("stackoverflow.com")) {
                int site = tokens.indexOf("stackoverflow.com");
                return new StackOverflowInfo(Long.parseLong(tokens.get(site + 2)));
            } else {
                throw new IllegalArgumentException("Not a stackoverflow link");
            }
        }
    }

    public record GithubInfo(String owner, String repo) {
        public static GithubInfo parseGithubInfo(String url) {
            var tokens = List.of(url.split("/"));
            if (tokens.contains("github.com")) {
                int site = tokens.indexOf("github.com");
                return new GithubInfo(tokens.get(site + 1), tokens.get(site + 2));
            } else {
                throw new IllegalArgumentException("Not a github link");
            }
        }
    }
}
