package ru.practicum.ewm.dto.request;

import lombok.Setter;
import lombok.Value;

import java.util.List;

@Value
@Setter
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;
    String status;
}
