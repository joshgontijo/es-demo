package es.demo.esdemo.repository;

import com.google.protobuf.Message;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class ReflectionMagic {

    static final Logger log = org.slf4j.LoggerFactory.getLogger(ReflectionMagic.class);

    public record MappedHandler(String typeName, Method method, Class<? super Message> eventType) {
    }

    static Map<String, MappedHandler> mapEventHandlers(Class<?> type) {
        var handlers = new HashMap<String, MappedHandler>();
        for (var method : type.getDeclaredMethods()) {
            if (method.getAnnotation(EventHandler.class) == null) {
                continue;
            }

            var params = method.getParameterTypes();
            if (params.length != 1) {
                throw new IllegalArgumentException("Event handler method '" + method.getName() + "' in class '" + type.getName() +
                        "' must have exactly one parameter, found " + params.length);
            }
            Class<?> firstParamType = params[0];

            //Olly Protobuf messages are allowed as event parameters
            if (Message.class.isAssignableFrom(firstParamType)) {
                throw new IllegalArgumentException("Event handler method '" + method.getName() + "' in class '" + type.getName() +
                        "' must have a parameter of type com.google.protobuf.Message");
            }

            var evTypeName = toSnakeCase(firstParamType.getSimpleName());
            handlers.put(evTypeName, new MappedHandler(evTypeName, method, (Class<? super Message>) firstParamType));
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
            throw new RuntimeException("Aggregate type must have a default constructor", e);
        }
    }

    static <T extends Message> T readProto(byte[] data, Class<T> type) {
        try {
            var method = type.getMethod("parseFrom", byte[].class);
            @SuppressWarnings("unchecked")
            T message = (T) method.invoke(null, data);
            return message;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse protobuf message from bytes", e);
        }
    }

}