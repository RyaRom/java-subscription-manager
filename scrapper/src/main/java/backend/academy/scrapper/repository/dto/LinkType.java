package backend.academy.scrapper.repository.dto;

import backend.academy.scrapper.repository.entities.LinkEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LinkType {
    GITHUB(LinkEntity.GithubInfo.class),
    STACK_OVERFLOW(LinkEntity.StackOverflowInfo.class);

    private final Class<? extends LinkInfo> infoType;
}
