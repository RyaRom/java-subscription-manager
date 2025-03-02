package backend.academy.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stackTrace) {}
