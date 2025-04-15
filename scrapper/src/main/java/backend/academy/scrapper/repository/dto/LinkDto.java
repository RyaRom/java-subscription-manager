package backend.academy.scrapper.repository.dto;

import backend.academy.dto.LinkResponse;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class LinkDto {
    @Nullable
    private Long linkId;

    @NonNull
    private String url;

    @NonNull
    private LinkInfo linkInfo;

    @NonNull
    private LinkType linkType;

    @NonNull
    private Set<Long> chatIds;

    public LinkResponse toLinkResponse() {
        return new LinkResponse(linkId, url, List.of(), List.of());
    }

    public record GithubInfo(String owner, String repo) implements LinkInfo {
    }

    public record StackOverflowInfo(Long questionId) implements LinkInfo {
    }
}
