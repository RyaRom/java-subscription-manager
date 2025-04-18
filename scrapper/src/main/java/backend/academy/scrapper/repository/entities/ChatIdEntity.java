package backend.academy.scrapper.repository.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "chat")
public class ChatIdEntity {
    @Id
    private Long chatId;

    @Id
    @ManyToOne
    @JoinColumn(name = "link_id", referencedColumnName = "linkId")
    private LinkEntity link;
}
