package backend.academy.bot.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class ScrapperClient {
    @Qualifier("scrapperHttpClient")
    private final WebClient webClient;

    public void registerChat(Long chatId) {
        webClient.post()
            .uri("/tg-chat/{chatId}", chatId)
            .retrieve();
    }
}
