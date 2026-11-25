package com.pedidos360.compras.controller;

import com.pedidos360.compras.dto.CheckoutDtos.QuitadoRequest;
import com.pedidos360.compras.dto.InteraccionEvent;
import com.pedidos360.compras.messaging.producer.InteraccionPublisher;
import com.pedidos360.compras.repository.PedidoRepository;
import com.pedidos360.compras.service.CheckoutService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ComprasControllerTest {

    private final PedidoRepository repository = mock(PedidoRepository.class);
    private final InteraccionPublisher publisher = mock(InteraccionPublisher.class);
    private final ComprasController controller =
            new ComprasController(repository, publisher, mock(CheckoutService.class));

    @Test
    void quitarDeLaCanastaPublicaElEventoSinTocarLaBase() {
        var auth = new TestingAuthenticationToken("ana@pedidos360.cl", null);

        var respuesta = controller.productoQuitado(new QuitadoRequest(6L, 2, 3980.0), auth);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        ArgumentCaptor<InteraccionEvent> evento = ArgumentCaptor.forClass(InteraccionEvent.class);
        verify(publisher).publicar(evento.capture());
        assertThat(evento.getValue().tipo()).isEqualTo(InteraccionEvent.CARRITO_QUITADO);
        assertThat(evento.getValue().usuarioEmail()).isEqualTo("ana@pedidos360.cl");
        assertThat(evento.getValue().productoId()).isEqualTo(6L);
        assertThat(evento.getValue().cantidad()).isEqualTo(2);
        verifyNoInteractions(repository);
    }
}
