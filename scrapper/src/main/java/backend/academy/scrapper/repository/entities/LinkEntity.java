package backend.academy.scrapper.repository.entities;

import java.util.List;

import backend.academy.dto.LinkResponse;
import backend.academy.scrapper.repository.dto.LinkInfo;
import backend.academy.scrapper.repository.dto.LinkType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.NonNull;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = "linkId")
@Table(name = "link")
@Entity
public class LinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkId;

    @NonNull
    private String url;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private LinkInfo linkInfo;

    @NonNull
    @Enumerated(EnumType.ORDINAL)
    private LinkType linkType;

    @NonNull
    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatIdEntity> chatIds;

    public LinkResponse toLinkResponse() {
        return new LinkResponse(linkId, url, List.of(), List.of());
    }

    public List<Long> getChatIdList() {
        return chatIds.stream()
                .map(ChatIdEntity::getChatId)
                .toList();
    }

    public record GithubInfo(String owner, String repo) implements LinkInfo {
    }

    public record StackOverflowInfo(Long questionId) implements LinkInfo {
    }
}
