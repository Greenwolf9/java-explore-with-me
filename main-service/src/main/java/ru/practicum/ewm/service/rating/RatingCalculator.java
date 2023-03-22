package ru.practicum.ewm.service.rating;

import ru.practicum.ewm.dto.rating.Score;

public class RatingCalculator {

    public static float calcScore(Score score) {
        float result = 0;
        try {
        result = score.getScore() / score.getResponses();
        } catch (ArithmeticException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }
}
