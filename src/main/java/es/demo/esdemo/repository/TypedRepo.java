package es.demo.esdemo.repository;

import es.demo.esdemo.Json;
import es.demo.esdemo.repo2.Event;
import es.demo.esdemo.repo2.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static es.demo.esdemo.repository.TypedRepo.ReflectionMagic.MappedHandler;
import static es.demo.esdemo.repository.TypedRepo.ReflectionMagic.invoke;
import static es.demo.esdemo.repository.TypedRepo.ReflectionMagic.mapEventHandlers;
import static es.demo.esdemo.repository.TypedRepo.ReflectionMagic.newInstance;
import static es.demo.esdemo.repository.TypedRepo.ReflectionMagic.toSnakeCase;

/**
 * Typed repository for aggregates, allowing to handle events with specific methods in the aggregate class.
 * The aggregate class must have a default constructor and methods annotated with @EventType.
 *
 * @param <T> the type of the aggregate
 */
public class TypedRepo<T extends Aggregate> {

    private static final Logger log = LoggerFactory.getLogger(TypedRepo.class);

    private final EventRepository db;
    private final Map<String, MappedHandler> handlers;
    private final Class<T> aggregateType;

    public TypedRepo(EventRepository db, Class<T> aggregateType) {
        this.db = db;
        this.handlers = mapEventHandlers(aggregateType);
        this.aggregateType = aggregateType;
    }

    public long append(String stream, Object data, int expectedVersion) {
        var className = data.getClass().getSimpleName();
        var evType = data.getClass().getAnnotation(DomainEvent.class);
        if (evType == null) {
            throw new IllegalArgumentException("Event class " + className + " is not annotated with " + DomainEvent.class.getSimpleName());
        }
        var evTypeName = evType.value() != null && !evType.value().isBlank() ? evType.value() : toSnakeCase(className);
        var event = new EventRecord()
                .streamId(stream)
                .eventType(evTypeName)
                .data(Json.toJson(expectedVersion));

        return db.append(event, new Version.Expect(expectedVersion));
    }



    static class ReflectionMagic {

        static Map<String, MappedHandler> mapEventHandlers(Class<?> type) {
            var handlers = new HashMap<String, MappedHandler>();
            for (var method : type.getDeclaredMethods()) {
                var params = method.getParameterTypes();
                if (params.length != 1) {
                    log.info("Method '{}' in class '{}' is not a valid event handler, must have exactly one parameter", method.getName(), type.getName());
                    continue;
                }
                Class<?> firstParamType = params[0];
                var evType = firstParamType.getAnnotation(DomainEvent.class);
                if (evType == null) {
                    log.info("Method '{}' in class '{}' is not a valid event handler, parameter type '{}' is not annotated with @EventType",
                            method.getName(), type.getName(), firstParamType.getName());
                    continue;
                }

                var evTypeName = evType.value() != null && !evType.value().isBlank() ? evType.value() : toSnakeCase(firstParamType.getSimpleName());
                handlers.put(evTypeName, new MappedHandler(evTypeName, method, firstParamType));
                log.info("Mapped event type '{}' to handler '{}'", evTypeName, type.getName());
            }
            return handlers;
        }

        static String toSnakeCase(String input) {
            var result = new StringBuilder();

            var chars = input.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                var upperCase = Character.toUpperCase(c);
                if (Character.isUpperCase(c) && i > 0 && (i < chars.length - 1 && !Character.isUpperCase(chars[i + 1]))) {
                    result.append("_");
                }
                result.append(upperCase);
            }

            return result.toString();
        }

        static void invoke(Object instance, MappedHandler handler, Object eventData) {
            try {
                handler.method.invoke(instance, eventData);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        static <T> T newInstance(Class<T> type) {
            try {
                return type.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Aggregate type must have a default constuctor", e);
            }
        }

        public record MappedHandler(String typeName, Method method, Class<?> eventType) {
        }

    }

}