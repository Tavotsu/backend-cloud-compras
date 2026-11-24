package com.pedidos360.compras.controller;

import com.pedidos360.compras.dto.CheckoutDtos.CuponRequest;
import com.pedidos360.compras.dto.CheckoutDtos.CuponResponse;
import com.pedidos360.compras.dto.CheckoutDtos.EnvioRequest;
import com.pedidos360.compras.dto.CheckoutDtos.EnvioResponse;
import com.pedidos360.compras.dto.InteraccionEvent;
import com.pedidos360.compras.messaging.producer.InteraccionPublisher;
import com.pedidos360.compras.model.Pedido;
import com.pedidos360.compras.repository.PedidoRepository;
import com.pedidos360.compras.service.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compras")
public class ComprasController {

    private final PedidoRepository repository;
    private final InteraccionPublisher interaccionPublisher;
    private final CheckoutService checkoutService;

    public ComprasController(PedidoRepository repository, InteraccionPublisher interaccionPublisher,
                             CheckoutService checkoutService) {
        this.repository = repository;
        this.interaccionPublisher = interaccionPublisher;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/carrito")
    public List<Pedido> verCarrito(Authentication auth) {
        // El email del usuario sale del JWT (preferred_username en Entra, email en Cognito)
        return repository.findByUsuarioEmail(auth.getName());
    }

    @PostMapping("/carrito")
    public Pedido agregarAlCarrito(@RequestBody Pedido pedido, Authentication auth) {
        pedido.setUsuarioEmail(auth.getName());
        Pedido guardado = repository.save(pedido);
        interaccionPublisher.publicar(InteraccionEvent.de(InteraccionEvent.CARRITO_AGREGADO, auth.getName(),
                guardado.getProductoId(), guardado.getCantidad(), guardado.getTotal()));
        return guardado;
    }

    // Quita un ítem del carrito (solo si pertenece al usuario)
    @DeleteMapping("/carrito/{id}")
    public ResponseEntity<Void> quitarDelCarrito(@PathVariable Long id, Authentication auth) {
        return repository.findById(id)
                .filter(p -> auth.getName().equals(p.getUsuarioEmail()))
                .map(p -> {
                    repository.delete(p);
                    interaccionPublisher.publicar(InteraccionEvent.de(InteraccionEvent.CARRITO_QUITADO, auth.getName(),
                            p.getProductoId(), p.getCantidad(), p.getTotal()));
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Lo llama el microservicio de órdenes una vez que la orden quedó registrada
    @DeleteMapping("/carrito")
    public ResponseEntity<Void> vaciarCarrito(Authentication auth) {
        repository.deleteByUsuarioEmail(auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cupon")
    public CuponResponse aplicarCupon(@Valid @RequestBody CuponRequest request, Authentication auth) {
        CuponResponse respuesta = checkoutService.aplicarCupon(request.codigo(), request.subtotal());
        interaccionPublisher.publicar(InteraccionEvent.de(InteraccionEvent.CUPON_APLICADO, auth.getName(),
                null, null, respuesta.valido() ? respuesta.descuento() : 0.0));
        return respuesta;
    }

    @PostMapping("/envio")
    public EnvioResponse calcularEnvio(@Valid @RequestBody EnvioRequest request, Authentication auth) {
        EnvioResponse respuesta = checkoutService.calcularEnvio(request.comuna(), request.subtotal());
        interaccionPublisher.publicar(InteraccionEvent.de(InteraccionEvent.ENVIO_CALCULADO, auth.getName(),
                null, null, respuesta.costo()));
        return respuesta;
    }
}
