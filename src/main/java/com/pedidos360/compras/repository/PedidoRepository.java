package com.pedidos360.compras.repository;

import com.pedidos360.compras.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioEmail(String usuarioEmail);

    @Transactional
    long deleteByUsuarioEmail(String usuarioEmail);
}
