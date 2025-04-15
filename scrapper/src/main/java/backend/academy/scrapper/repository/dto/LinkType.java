package backend.academy.scrapper.repository.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LinkType {
    GITHUB(LinkDto.GithubInfo.class),
    STACK_OVERFLOW(LinkDto.StackOverflowInfo.class);

    private final Class<? extends LinkInfo> infoType;
}
