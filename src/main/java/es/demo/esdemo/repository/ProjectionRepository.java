package es.demo.esdemo.repository;

import es.demo.esdemo.Json;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

import static es.demo.esdemo.repository.ReflectionMagic.*;

/**
 * Typed repository for aggregates, allowing to handle events with specific methods in the aggregate class.
 * The aggregate class must have a default constructor and methods annotated with @EventType.
 *
 * @param <T> the type of the aggregate
 */
public class ProjectionRepository<T> {

    private final EventRepository db;
    private final Map<String, MappedHandler> handlers;
    private final Class<T> aggregateType;

    public ProjectionRepository(EventRepository db, Class<T> projectionType) throws IOException {
        this.db = db;
        this.handlers = mapEventHandlers(projectionType);
        this.aggregateType = projectionType;

    }


    @Transactional(readOnly = true)
    public T project(String stream) {
        var instance = newInstance(aggregateType);
        db.get(stream).forEach(event -> {
            var handler = handlers.get(event.type());
            if (handler != null) {
                var eventData = Json.fromJson(event.data(), handler.eventType());
                invoke(instance, handler, eventData);
            }
        });
        return instance;
    }

}