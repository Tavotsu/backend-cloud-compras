package com.pedidos360.compras.dto;

import java.time.Instant;
import java.util.UUID;

// Evento que se publica en el tópico pedidos360.interacciones cada vez que un
// usuario interactúa con su carrito. La clave del mensaje es usuarioEmail.
public record InteraccionEvent(
        String eventoId,
        String tipo,
        String usuarioEmail,
        Long productoId,
        String nombreProducto,
        String categoria,
        Integer cantidad,
        Double valor,
        String origen,
        Instant ocurridoEn) {

    public static final String CARRITO_AGREGADO = "CARRITO_AGREGADO";
    public static final String CARRITO_QUITADO = "CARRITO_QUITADO";
    public static final String CUPON_APLICADO = "CUPON_APLICADO";
    public static final String ENVIO_CALCULADO = "ENVIO_CALCULADO";

    // Compras no conoce el nombre ni la categoría: analítica los completa con el catálogo
    public static InteraccionEvent de(String tipo, String usuarioEmail, Long productoId, Integer cantidad, Double valor) {
        return new InteraccionEvent(UUID.randomUUID().toString(), tipo, usuarioEmail, productoId,
                null, null, cantidad, valor, "compras", Instant.now());
    }
}
