package backend.academy.scrapper.repository.entities;

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
import java.util.List;
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
@Entity
public class LinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkId;

    @NonNull
    private String url;

    @Column(columnDefinition = "jsonb")
    private String linkInfo;

    @NonNull
    @Enumerated(EnumType.ORDINAL)
    private LinkType linkType;

    @NonNull
    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatIdEntity> chatIds;
}
