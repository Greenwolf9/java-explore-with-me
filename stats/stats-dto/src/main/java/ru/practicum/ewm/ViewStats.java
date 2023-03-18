package ru.practicum.ewm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@Builder
@JsonPropertyOrder({"app", "uri", "hits"})
public class ViewStats {
    @NonNull
    @NotBlank
    String app;
    @JsonProperty("uri")
    String uri;
    @JsonProperty("hits")
    Integer hits;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ViewStats(@JsonProperty("app") String app, @JsonProperty("uri") String uri, @JsonProperty("hits") Integer hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;
    }
}
