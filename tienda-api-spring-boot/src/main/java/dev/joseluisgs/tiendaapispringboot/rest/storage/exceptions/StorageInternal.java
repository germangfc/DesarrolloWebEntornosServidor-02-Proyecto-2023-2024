package dev.joseluisgs.tiendaapispringboot.rest.storage.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

// Nos permite devolver un estado cuando salta la excepción
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class StorageInternal extends StorageException {
    // Por si debemos serializar
    @Serial
    private static final long serialVersionUID = 43876691117560211L;

    public StorageInternal(String mensaje) {
        super(mensaje);
    }
}