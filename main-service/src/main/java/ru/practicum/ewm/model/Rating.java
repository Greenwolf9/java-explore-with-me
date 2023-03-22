package ru.practicum.ewm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "ratings")
@NoArgsConstructor
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "stars")
    private int stars;
    @Column(name = "comments")
    private String comments;
    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
}
