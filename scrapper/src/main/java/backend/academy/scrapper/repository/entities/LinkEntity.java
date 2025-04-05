package backend.academy.scrapper.repository.entities;

import java.util.Set;

import backend.academy.scrapper.repository.dto.LinkType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = "linkId")
@Table(name = "link")
public class LinkEntity {
    private Long linkId;
    @NonNull
    private String url;

    @Column(columnDefinition = "jsonb")
    private String linkInfo;

    @NonNull
    @Enumerated(EnumType.ORDINAL)
    private LinkType linkType;

    @NonNull
    @Column(columnDefinition = "bigint[]")
    private Set<Long> chatIds;
}
