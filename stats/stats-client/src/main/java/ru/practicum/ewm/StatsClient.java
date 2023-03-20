package ru.practicum.ewm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StatsClient extends BaseClient {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String API_PREFIX_HIT = "/hit";
    private static final String API_PREFIX_STATS = "/stats";
    private static final String SERVICE = "ewm_main_service";

    @Autowired
    public StatsClient(@Value("${stat-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build()
        );
    }

    public ResponseEntity<Object> hit(HttpServletRequest request) {
        HitDto hitDto = new HitDto(SERVICE,
                request.getRequestURI(),
                request.getRemoteAddr(), LocalDateTime.now().format(formatter));
        return post(API_PREFIX_HIT, hitDto);
    }

    public ResponseEntity<Object> stats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter)
        );

        StringBuilder builder = new StringBuilder();
        builder.append(API_PREFIX_STATS);
        builder.append("?start=").append((parameters.get("start")));
        builder.append("&end=").append(parameters.get("end"));
        if (!uris.isEmpty()) {
            for (String s : uris) {
                builder.append("&uris=").append(s);
            }
        }
        if (unique) {
            builder.append("&unique=").append(unique);
        }
        return get(builder.toString());
    }
}

