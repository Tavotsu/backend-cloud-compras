package com.pedidos360.compras.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/compras")
public class ComprasController {

    @GetMapping("/carrito")
    public String verCarrito() {
        return "Carrito: Estás viendo tu carrito de compras (Autorizado por Rol User).";
    }

    @PostMapping("/carrito")
    public String agregarAlCarrito() {
        return "Carrito: Producto agregado exitosamente.";
    }
}
