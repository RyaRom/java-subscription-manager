package backend.academy.scrapper.repository;

import java.util.List;

public record User(
    Long chatId,
    List<Long> subscriptionIds
    //    List<LocalDateTime> notificationsSchedule
) {
}
