package com.pedidos360.compras.messaging.producer;

import com.pedidos360.compras.dto.InteraccionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// Productor Kafka de interacciones. El envío es asíncrono: si Kafka falla se registra
// en el log y la respuesta al usuario no se ve afectada.
@Component
public class InteraccionPublisher {

    private static final Logger log = LoggerFactory.getLogger(InteraccionPublisher.class);

    private final KafkaTemplate<String, InteraccionEvent> kafkaTemplate;
    private final String topico;

    public InteraccionPublisher(KafkaTemplate<String, InteraccionEvent> kafkaTemplate,
                                @Value("${app.kafka.topicos.interacciones}") String topico) {
        this.kafkaTemplate = kafkaTemplate;
        this.topico = topico;
    }

    public void publicar(InteraccionEvent evento) {
        try {
            // La clave (email) manda todos los eventos de un usuario a la misma partición, en orden
            kafkaTemplate.send(topico, evento.usuarioEmail(), evento).whenComplete((resultado, error) -> {
                if (error != null) {
                    log.warn("No se pudo publicar {} (producto #{}) en '{}': {}",
                            evento.tipo(), evento.productoId(), topico, error.getMessage());
                } else {
                    log.debug("{} publicado en {}-{} offset {}", evento.tipo(), topico,
                            resultado.getRecordMetadata().partition(), resultado.getRecordMetadata().offset());
                }
            });
        } catch (RuntimeException e) {
            log.warn("Kafka no disponible, se omite el evento {}: {}", evento.tipo(), e.getMessage());
        }
    }
}
