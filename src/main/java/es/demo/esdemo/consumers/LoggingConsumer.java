package es.demo.esdemo.consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LoggingConsumer {
    private static final Logger log = LoggerFactory.getLogger(LoggingConsumer.class);

    @KafkaListener(topics = "events", groupId = "logging-consumer")
    public void listen(ConsumerRecord<String, byte[]> record) {
        log.info("Received Kafka record:");
        log.info("Key: {}", record.key());
        log.info("Headers: {}", record.headers());
        log.info("Value: {}", new String(record.value()));
    }
}