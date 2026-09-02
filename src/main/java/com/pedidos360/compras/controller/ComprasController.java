package com.pedidos360.compras.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/compras")
public class ComprasController {

    private final com.pedidos360.compras.repository.PedidoRepository repository;

    public ComprasController(com.pedidos360.compras.repository.PedidoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/carrito")
    public java.util.List<com.pedidos360.compras.model.Pedido> verCarrito(org.springframework.security.core.Authentication auth) {
        // Obtenemos el email del usuario logueado desde el JWT (normalmente en preferred_username o upn)
        String username = auth.getName(); 
        return repository.findByUsuarioEmail(username);
    }

    @PostMapping("/carrito")
    public com.pedidos360.compras.model.Pedido agregarAlCarrito(@org.springframework.web.bind.annotation.RequestBody com.pedidos360.compras.model.Pedido pedido, org.springframework.security.core.Authentication auth) {
        pedido.setUsuarioEmail(auth.getName());
        return repository.save(pedido);
    }
}
