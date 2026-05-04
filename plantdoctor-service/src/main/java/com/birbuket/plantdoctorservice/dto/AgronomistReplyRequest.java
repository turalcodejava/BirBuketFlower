package com.birbuket.plantdoctorservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgronomistReplyRequest {
    @NotNull(message = "agronomistId is required")
    private Long agronomistId;

    /** JSON-da adətən «response»; köhnə front «message», «replyText» də göndərə bilər. */
    @NotBlank(message = "response is required")
    @JsonProperty("response")
    @JsonAlias({"message", "replyText", "reply", "text"})
    private String response;

    /** İstəyə bağlı; indi PENDING ikən reply həmişə rezerv + qəbul məktubunu özü edir. Keçid üçün saxlanılır. */
    private Boolean forceReserveAndComplete;
}
