package com.pedidos360.compras.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutServiceTest {

    private final CheckoutService service = new CheckoutService();

    @Test
    void cuponValidoCalculaDescuento() {
        var r = service.aplicarCupon(" super360 ", 10000);
        assertThat(r.valido()).isTrue();
        assertThat(r.porcentaje()).isEqualTo(10);
        assertThat(r.descuento()).isEqualTo(1000);
    }

    @Test
    void cuponInexistenteNoDescuenta() {
        var r = service.aplicarCupon("GRATIS", 10000);
        assertThat(r.valido()).isFalse();
        assertThat(r.descuento()).isZero();
    }

    @Test
    void envioGratisDesdeVeinteMil() {
        assertThat(service.calcularEnvio("Maipú", 20000).gratis()).isTrue();
    }

    @Test
    void envioConRecargoFueraDeComunasCercanas() {
        assertThat(service.calcularEnvio("Providencia", 5000).costo()).isEqualTo(CheckoutService.COSTO_ENVIO);
        assertThat(service.calcularEnvio("Puente Alto", 5000).costo()).isEqualTo(CheckoutService.COSTO_ENVIO_EXTENDIDO);
    }
}
