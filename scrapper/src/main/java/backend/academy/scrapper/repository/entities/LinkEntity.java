package backend.academy.scrapper.repository.entities;

import backend.academy.dto.LinkResponse;
import backend.academy.scrapper.repository.dto.LinkType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = "linkId")
@Table(name = "link")
@ToString
@Entity
public class LinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkId;

    @NonNull
    private String url;

    @Nullable
    @OneToOne(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true)
    private LinkInfoEntity linkInfo;

    @NonNull
    @Enumerated(EnumType.ORDINAL)
    private LinkType linkType;

    @NonNull
    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ChatIdEntity> chatIds;

    public LinkResponse toLinkResponse() {
        return new LinkResponse(linkId, url, List.of(), List.of());
    }

    public List<Long> getChatIdList() {
        return chatIds.stream()
            .map(ChatIdEntity::getChatId)
            .toList();
    }
}
