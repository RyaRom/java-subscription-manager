package backend.academy.scrapper.repository.dto;

import backend.academy.scrapper.repository.entities.LinkEntity;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "_type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = LinkEntity.GithubInfo.class, name = "gh"),
    @JsonSubTypes.Type(value = LinkEntity.StackOverflowInfo.class, name = "so"),
})
public interface LinkInfo {
}
