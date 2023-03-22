package ru.practicum.ewm.service.rating;

import ru.practicum.ewm.dto.rating.Score;

public class RatingCalculator {

    public static float calcScore(Score score) {
        return score.getScore() / score.getResponses();
    }
}
