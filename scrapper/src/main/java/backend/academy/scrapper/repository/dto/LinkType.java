package backend.academy.scrapper.repository.dto;

import backend.academy.scrapper.repository.entities.GithubInfoEntity;
import backend.academy.scrapper.repository.entities.LinkInfoEntity;
import backend.academy.scrapper.repository.entities.StackOverflowInfoEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LinkType {
    GITHUB(GithubInfoEntity.class),
    STACK_OVERFLOW(StackOverflowInfoEntity.class);

    private final Class<? extends LinkInfoEntity> infoType;
}
