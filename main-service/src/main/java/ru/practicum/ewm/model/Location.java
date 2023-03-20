package ru.practicum.ewm.model;

import lombok.*;

import javax.persistence.Embeddable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class Location {
    private float lat;
    private float lon;
}
