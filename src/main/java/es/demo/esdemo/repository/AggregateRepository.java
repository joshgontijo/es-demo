package es.demo.esdemo.repository;

import es.demo.esdemo.Json;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

import static es.demo.esdemo.repository.ReflectionMagic.*;

/**
 * Typed repository for aggregates, allowing to handle events with specific methods in the aggregate class.
 * The aggregate class must have a default constructor and methods annotated with @EventType.
 *
 * @param <T> the type of the aggregate
 */
public class AggregateRepository<T extends Aggregate> {

    private final EventRepository db;
    private final Map<String, MappedHandler> handlers;
    private final Class<T> aggregateType;

    public AggregateRepository(EventRepository db, Class<T> aggregateType) {
        this.db = db;
        this.handlers = mapEventHandlers(aggregateType);
        this.aggregateType = aggregateType;
    }

    public long append(String stream, Object data, Version expectedVersion) {
        var event = new EventRecord()
                .streamId(stream)
                .type(toSnakeCase(data.getClass().getSimpleName()))
                .data(Json.toJson(data)); //TODO parse from protobuf message if applicable

        return db.append(event, expectedVersion);
    }


    @Transactional(readOnly = true)
    public T get(String stream) {
        var instance = newInstance(aggregateType);
        db.get(stream).forEach(event -> {
            var handler = handlers.get(event.type());
            if (handler != null) {
                var eventData = Json.fromJson(event.data(), handler.eventType());
                invoke(instance, handler, eventData);
                instance.version = event.version();
            }
        });
        return instance;
    }

}