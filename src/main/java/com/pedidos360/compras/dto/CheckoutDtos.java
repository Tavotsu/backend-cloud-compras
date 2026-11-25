package com.pedidos360.compras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Peticiones y respuestas de cupón, envío y canasta
public final class CheckoutDtos {

    private CheckoutDtos() {
    }

    public record CuponRequest(@NotBlank @Size(max = 30) String codigo, @NotNull @PositiveOrZero Double subtotal) {
    }

    public record CuponResponse(String codigo, boolean valido, int porcentaje, double descuento, String mensaje) {
    }

    public record EnvioRequest(@NotBlank @Size(max = 60) String comuna, @NotNull @PositiveOrZero Double subtotal) {
    }

    public record EnvioResponse(String comuna, double costo, boolean gratis, String plazo) {
    }

    // Producto que el usuario sacó de la canasta antes de confirmar el pedido
    public record QuitadoRequest(@NotNull Long productoId, @NotNull @PositiveOrZero Integer cantidad,
                                 @PositiveOrZero Double valor) {
    }
}
