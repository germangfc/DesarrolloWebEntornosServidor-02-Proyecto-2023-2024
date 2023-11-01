package dev.joseluisgs.tiendaapispringboot.rest.pedidos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción de producto no encontrado
 * Status 404
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductoNotStock extends PedidoException {
    public ProductoNotStock(Long id) {
        super("Producto con id " + id + " no tiene stock suficiente");
    }
}
