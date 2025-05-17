package backend.academy.scrapper.repository.links.dto.stackOverflow;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record StackOverflowFullInfo(
        String questionTitle, String username, String creationDate, String body, String type) {
    public static StackOverflowFullInfo fromResponse(
            StackResponseForUpdatesDto.StackAnswersResponseDto responseDto, String title, String type) {
        return new StackOverflowFullInfo(
                title,
                responseDto.owner().displayName(),
                LocalDateTime.from(responseDto.creationDate().atZone(ZoneId.of("UTC")))
                        .format(DateTimeFormatter.BASIC_ISO_DATE),
                limit(200, responseDto.bodyMarkdown()),
                type);
    }

    private static String limit(int limit, String src) {
        if (src.length() <= limit) {
            return src;
        } else {
            return src.substring(0, limit) + "...";
        }
    }
}
