package backend.academy.scrapper.service.parsers;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.StackOverflowHttpClient;
import backend.academy.scrapper.repository.links.dto.LinkType;
import backend.academy.scrapper.repository.links.dto.stackOverflow.StackOverflowFullInfo;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import backend.academy.scrapper.repository.links.entities.StackOverflowInfoEntity;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Log4j2
@RequiredArgsConstructor
public class StackOverflowParser implements AbstractParser {
    private final StackOverflowHttpClient stackOverflowHttpClient;
    private final BotClient botClient;

    @Override
    public boolean parse(LinkEntity link, List<String> tokens) {
        if (tokens.contains("stackoverflow.com")) {
            link.setLinkType(LinkType.STACK_OVERFLOW);
            int siteIndex = tokens.indexOf("stackoverflow.com");
            var info = new StackOverflowInfoEntity(Long.parseLong(tokens.get(siteIndex + 2)));
            info.setLink(link);
            link.setLinkInfo(info);
            return true;
        }
        return false;
    }

    @Override
    public Mono<Boolean> update(LinkEntity link, Instant lastUpdated) {
        if (link.getLinkType() != LinkType.STACK_OVERFLOW) {
            return Mono.just(false);
        }
        if (link.getLinkInfo() instanceof StackOverflowInfoEntity stackOverflowInfo) {
            var answers = stackOverflowHttpClient
                .getStackOverflowNewAnswers(stackOverflowInfo.getQuestionId(), lastUpdated)
                .flatMapMany(res -> Flux.fromIterable(res.items()))
                .doOnNext(activity -> log.info("so answer update in {}", link.getLinkId()))
                .zipWith(
                    stackOverflowHttpClient.getQuestionTitle(stackOverflowInfo.getQuestionId()),
                    (answersResponseDto, title) ->
                        StackOverflowFullInfo.fromResponse(answersResponseDto, title, "Answer"))
                .flatMap(answer -> sendSoUpdate(answer, link));

            var comments = stackOverflowHttpClient
                .getStackOverflowNewComments(stackOverflowInfo.getQuestionId(), lastUpdated)
                .flatMapMany(res -> Flux.fromIterable(res.items()))
                .doOnNext(activity -> log.info("so comment update in {}", link.getLinkId()))
                .zipWith(
                    stackOverflowHttpClient.getQuestionTitle(stackOverflowInfo.getQuestionId()),
                    (answersResponseDto, title) ->
                        StackOverflowFullInfo.fromResponse(answersResponseDto, title, "Comment"))
                .flatMap(answer -> sendSoUpdate(answer, link));

            return Flux.merge(answers, comments).then(Mono.just(true));
        }
        throw new IllegalStateException("Parser doesn't work correctly");
    }

    private Mono<Void> sendSoUpdate(StackOverflowFullInfo answer, LinkEntity link) {
        if (answer.type().isEmpty()) {
            log.warn("Unknown type in stack update {}", answer);
        }
        LinkUpdate linkUpdate = LinkUpdate.builder()
            .linkId(link.getLinkId())
            .url(link.getUrl())
            .tgChatIds(link.getChatIdList())
            .description(getStackAnswerUpdate(answer))
            .build();
        return botClient.sendUpdate(linkUpdate);
    }

    private String getStackAnswerUpdate(StackOverflowFullInfo info) {
        return String.format(
            """
                New Stack overflow %s
                Question: %s
                User: %s
                Text: %s""",
            info.type(), info.questionTitle(), info.username(), info.body());
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
