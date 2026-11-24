package com.pedidos360.compras.service;

import com.pedidos360.compras.dto.CheckoutDtos.CuponResponse;
import com.pedidos360.compras.dto.CheckoutDtos.EnvioResponse;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

// Reglas de cupones y costo de despacho (mismos valores que muestra el frontend)
@Service
public class CheckoutService {

    static final double ENVIO_GRATIS_DESDE = 20000;
    static final double COSTO_ENVIO = 1990;
    static final double COSTO_ENVIO_EXTENDIDO = 3490;

    private static final Map<String, Integer> CUPONES = Map.of(
            "SUPER360", 10,
            "BIENVENIDA", 15);

    // Comunas con despacho en el día; el resto tiene recargo y llega al día siguiente
    private static final Set<String> COMUNAS_CERCANAS = Set.of(
            "santiago", "providencia", "ñuñoa", "las condes", "macul", "san miguel", "estación central");

    public CuponResponse aplicarCupon(String codigo, double subtotal) {
        String normalizado = codigo.trim().toUpperCase(Locale.ROOT);
        Integer porcentaje = CUPONES.get(normalizado);
        if (porcentaje == null) {
            return new CuponResponse(normalizado, false, 0, 0, "El cupón no existe o expiró");
        }
        double descuento = Math.round(subtotal * porcentaje / 100.0);
        return new CuponResponse(normalizado, true, porcentaje, descuento, "Cupón aplicado: " + porcentaje + "% de descuento");
    }

    public EnvioResponse calcularEnvio(String comuna, double subtotal) {
        boolean cercana = COMUNAS_CERCANAS.contains(comuna.trim().toLowerCase(Locale.ROOT));
        if (subtotal >= ENVIO_GRATIS_DESDE) {
            return new EnvioResponse(comuna, 0, true, cercana ? "Hoy" : "Mañana");
        }
        return cercana
                ? new EnvioResponse(comuna, COSTO_ENVIO, false, "Hoy")
                : new EnvioResponse(comuna, COSTO_ENVIO_EXTENDIDO, false, "Mañana");
    }
}
