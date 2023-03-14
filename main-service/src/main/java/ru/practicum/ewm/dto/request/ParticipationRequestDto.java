package ru.practicum.ewm.dto.request;

import lombok.Builder;
import lombok.Setter;
import lombok.Value;

@Value
@Setter
@Builder
public class ParticipationRequestDto {

    String created;
    Long event;
    Long id;
    Long requester;
    String status;
}
