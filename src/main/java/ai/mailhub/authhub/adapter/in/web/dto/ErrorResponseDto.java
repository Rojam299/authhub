package ai.mailhub.authhub.adapter.in.web.dto;

import java.time.LocalDateTime;

public record ErrorResponseDto(String apiPath, int statusCode, String errorMessage, LocalDateTime errorTime) {

}
