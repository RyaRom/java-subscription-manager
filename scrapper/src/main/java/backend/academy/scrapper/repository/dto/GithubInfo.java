package backend.academy.scrapper.repository.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class GithubInfo {
    @Builder.Default
    private String githubInfoId = UUID.randomUUID().toString();

    private String owner;
    private String repo;

    public static GithubInfo getGithubInfo(String url) {
        var tokens = List.of(url.split("/"));
        if (tokens.contains("github.com")) {
            int site = tokens.indexOf("github.com");
            return GithubInfo.builder()
                    .owner(tokens.get(site + 1))
                    .repo(tokens.get(site + 2))
                    .build();
        } else {
            throw new RuntimeException("Not a github link");
        }
    }
}
