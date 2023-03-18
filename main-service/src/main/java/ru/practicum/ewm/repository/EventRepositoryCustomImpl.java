package ru.practicum.ewm.repository;

import org.springframework.context.annotation.Lazy;
import ru.practicum.ewm.model.Event;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventRepositoryCustomImpl implements EventRepositoryCustom {
    private final EventRepository eventRepository;

    public EventRepositoryCustomImpl(@Lazy EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Event> findFilteredEvents(String text, List<Long> categories, boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, String sort, int from, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cr = cb.createQuery(Event.class);
        Root<Event> root = cr.from(Event.class);
        List<Predicate> predicates = new ArrayList<>();

        if (text != null && !text.isEmpty()) {
            Predicate annotationMatch = cb.like(cb.lower(root.get("annotation")), "%" + text.toLowerCase() + "%");
            Predicate descriptionMatch = cb.like(cb.lower(root.get("description")), "%" + text.toLowerCase() + "%");
            Predicate ifMatched = cb.or(annotationMatch, descriptionMatch);
            predicates.add(ifMatched);
        }

        if (categories != null) {
            CriteriaBuilder.In<Long> inClause = cb.in(root.get("category"));
            for (Long cat : categories) {
                inClause.value(cat);
            }
            predicates.add(inClause);
        }

        Predicate paidMatch = cb.equal(root.get("paid"), paid);
        predicates.add(paidMatch);

        if (rangeStart != null && rangeEnd != null) {
            Predicate dateMatch = cb.between(root.get("eventDate"), rangeStart, rangeEnd);
            predicates.add(dateMatch);
        }
        if (sort.equals("EVENT_DATE")) {
            cr.orderBy(cb.desc(root.get("eventDate")));
        }
        if (sort.equals("VIEWS")) {
            cr.orderBy(cb.desc(root.get("views")));
        }
        cr.select(root).where(predicates.toArray(new Predicate[]{}));
        return entityManager.createQuery(cr).setFirstResult(from).setMaxResults(size).getResultList();
    }
}
