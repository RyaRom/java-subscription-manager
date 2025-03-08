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
    @Nullable
    private StackOverflowInfo stackOverflowInfo;

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

    public record StackOverflowInfo(
        Long questionId
    ) {
        public static StackOverflowInfo getStackOverflowInfo(String url) {
            var tokens = List.of(url.split("/"));
            if (tokens.contains("stackoverflow.com")) {
                int site = tokens.indexOf("stackoverflow.com");
                return new StackOverflowInfo(
                    Long.parseLong(tokens.get(site + 2))
                );
            } else {
                throw new IllegalArgumentException("Not a stackoverflow link");
            }
        }
    }

    public record GithubInfo(
        String owner,
        String repo
    ) {
        public static GithubInfo getGithubInfo(String url) {
            var tokens = List.of(url.split("/"));
            if (tokens.contains("github.com")) {
                int site = tokens.indexOf("github.com");
                return new GithubInfo(
                    tokens.get(site + 1),
                    tokens.get(site + 2)
                );
            } else {
                throw new IllegalArgumentException("Not a github link");
            }
        }
    }
}
