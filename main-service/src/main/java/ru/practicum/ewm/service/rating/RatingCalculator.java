package ru.practicum.ewm.service.rating;

import ru.practicum.ewm.dto.rating.Score;

public class RatingCalculator {

    public static float calcScore(Score score) {
        if (score.getResponses() == 0) {
            return score.getScore();
        }
        return (float) score.getScore() / score.getResponses();
    }
}
