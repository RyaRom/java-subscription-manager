package backend.academy.scrapper.repository.dto;

import backend.academy.dto.LinkResponse;
import java.util.List;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public final class Link {
    private Long linkId;
    private String url;
    @Nullable
    private GithubInfo githubInfo;
    private Type linkType;
    private List<Long> chatIds;
    private List<String> tags;
    private List<String> filters;

    public LinkResponse toLinkResponse() {
        return new LinkResponse(
            linkId, url,
            tags, filters
        );
    }

    public enum Type {
        GITHUB, STACK_OVERFLOW
    }
}
