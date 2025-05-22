package backend.academy.scrapper.repository.links;

import backend.academy.proto.impl.LinkEntities;
import backend.academy.proto.impl.LinkEntities.FullLinkProto;
import backend.academy.scrapper.config.DataConnectionProperties;
import backend.academy.scrapper.repository.links.dto.LinkType;
import backend.academy.scrapper.repository.links.entities.ChatIdEntity;
import backend.academy.scrapper.repository.links.entities.GithubInfoEntity;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import backend.academy.scrapper.repository.links.entities.StackOverflowInfoEntity;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.Nullable;
import static io.lettuce.core.SetArgs.Builder.ex;

@Log4j2
@RequiredArgsConstructor
public class CachedLinkRepository implements LinkRepository {
    private final DataConnectionProperties dataConnectionProperties;
    private final RedisAsyncCommands<String, FullLinkProto> redisAsyncCommands;
    private final LinkRepository delegated;

    @Override
    public Optional<LinkEntity> findById(Long linkId) {
        return delegated.findById(linkId);
    }

    @Override
    public Optional<LinkEntity> findByUrl(String url) {
        try {
            FullLinkProto cached = redisAsyncCommands.get(prefixUrl(url)).get();
            if (cached != null) {
                return Optional.of(mapProto(cached));
            } else {
                Optional<LinkEntity> found = delegated.findByUrl(url);
                found.ifPresent(linkEntity ->
                    redisAsyncCommands.set(prefixUrl(url), mapProto(linkEntity),
                        ex(dataConnectionProperties.redisExMs())));
                return found;
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while getting link from cache", e);
            return delegated.findByUrl(url);
        } catch (ExecutionException e) {
            log.error("Error while getting link from cache", e);
            return delegated.findByUrl(url);
        }
    }

    @Override
    public List<LinkEntity> findAll() {
        return delegated.findAll();
    }

    @Override
    public List<LinkEntity> findAllPaginated(long lastId, int limit) {
        return delegated.findAllPaginated(lastId, limit);
    }

    @Override
    public @Nullable LinkEntity save(LinkEntity link) {
        clearCacheByUrl(link.getUrl());
        return delegated.save(link);
    }

    @Override
    public void addChatId(LinkEntity link, Long chatId) {
        clearCacheByUrl(link.getUrl());
        delegated.addChatId(link, chatId);
    }

    @Override
    public List<LinkEntity> saveAll(List<LinkEntity> links) {
        links.stream()
            .map(LinkEntity::getUrl)
            .forEach(this::clearCacheByUrl);
        return delegated.saveAll(links);
    }

    @Override
    public Optional<LinkEntity> deleteByUrl(String url) {
        clearCacheByUrl(url);
        return delegated.deleteByUrl(url);
    }

    @Override
    public boolean dropForTest() {
        boolean wasDeleted = delegated.dropForTest();
        if (wasDeleted) {
            redisAsyncCommands.flushall();
        }
        return wasDeleted;
    }

    @Override
    public List<LinkEntity> findWithChatId(Long chatId) {
        return delegated.findWithChatId(chatId);
    }

    private void clearCacheByUrl(String url) {
        try {
            redisAsyncCommands.del(prefixUrl(url)).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while deleting link from cache", e);
        }
    }


    private static LinkEntity mapProto(FullLinkProto cached) {
        var result = new LinkEntity();
        result.setLinkId(cached.getId());
        result.setUrl(cached.getUrl());
        result.setChatIds(cached.getChatIdsList()
            .stream()
            .map(it -> new ChatIdEntity(it, result))
            .toList());
        result.setLinkType(LinkType.values()[cached.getLinkTypeValue()]);
        switch (result.getLinkType()) {
            case GITHUB -> result.setLinkInfo(new GithubInfoEntity()
                .setOwner(cached.getGithubInfo().getOwner())
                .setRepo(cached.getGithubInfo().getRepo()));
            case STACK_OVERFLOW -> result.setLinkInfo(new StackOverflowInfoEntity()
                .setQuestionId(cached.getStackOverflowInfo().getQuestionId()));
            default -> result.setLinkInfo(null);
        }
        return result;
    }

    private static FullLinkProto mapProto(LinkEntity link) {
        var result = FullLinkProto.newBuilder()
            .setId(link.getLinkId())
            .setUrl(link.getUrl())
            .addAllChatIds(link.getChatIds()
                .stream()
                .map(ChatIdEntity::getChatId)
                .toList())
            .setLinkTypeValue(link.getLinkType().ordinal());
        switch (link.getLinkType()) {
            case GITHUB -> result.setGithubInfo(LinkEntities.GithubInfoProto.newBuilder()
                .setOwner(link.getLinkInfo().getGithubInfo().getOwner())
                .setRepo(link.getLinkInfo().getGithubInfo().getRepo())
                .build());
            case STACK_OVERFLOW -> result.setStackOverflowInfo(LinkEntities.StackOverflowInfoProto.newBuilder()
                .setQuestionId(link.getLinkInfo().getStackOverflowInfo().getQuestionId())
                .build());
        }
        return result.build();
    }


    private static String prefixUrl(String url) {
        return "link:url:" + url;
    }
}
