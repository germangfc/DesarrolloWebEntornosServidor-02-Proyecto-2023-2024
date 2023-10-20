package dev.joseluisgs.tiendaapispringboot.productos.repositories;

import dev.joseluisgs.tiendaapispringboot.categorias.models.Categoria;
import dev.joseluisgs.tiendaapispringboot.productos.models.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Vamos a probar el repositorio, pero moqueamos la base de datos JPA
@DataJpaTest
class ProductosRepositoryTest {

    private final Categoria categoria = new Categoria(null, "TEST", LocalDateTime.now(), LocalDateTime.now(), false);

    private final Producto producto1 = new Producto(
            null, "Adidas", "Zapatillas", "Zapatillas de deporte",
            100.0, "http://placeimg.com/640/480/people", 5,
            LocalDateTime.now(), LocalDateTime.now(), UUID.fromString("80e559b5-83c5-4555-ba0b-bb9fddb6e96c"),
            false, categoria
    );
    private final Producto producto2 = new Producto(
            null, "Nike", "Zapatillas", "Zapatillas de deporte",
            100.0, "http://placeimg.com/640/480/people", 5,
            LocalDateTime.now(), LocalDateTime.now(), UUID.fromString("542f0a0b-064b-4022-b528-3b59f8bae821"),
            false, categoria
    );
    @Autowired
    private ProductosRepository repository;
    @Autowired
    private TestEntityManager entityManager; // EntityManager para hacer las pruebas

    @BeforeEach
    void setUp() {
        // Vamos a salvar una categoria
        entityManager.merge(categoria);
        entityManager.flush();
        // Vamos a salvar dos productos
        entityManager.merge(producto1);
        entityManager.merge(producto2);
        entityManager.flush();
    }

    @Test
    void findAll() {
        // Act
        List<Producto> productos = repository.findAll();

        // Ten cuidado si estas usando datos en el script de test, ya que si no se borran, se acumulan

        // Assert
        assertAll("findAll",
                () -> assertNotNull(productos),
                () -> assertFalse(productos.isEmpty()),
                () -> assertTrue(productos.size() >= 2)
        );
    }


    @Test
    void findById_existingId_returnsOptionalWithProducto() {
        // Act
        Long id = 1L;
        Optional<Producto> optionalProducto = repository.findById(id);

        // Assert
        assertAll("findById_existingId_returnsOptionalWithProducto",
                () -> assertNotNull(optionalProducto),
                () -> assertTrue(optionalProducto.isPresent()),
                () -> assertEquals(id, optionalProducto.get().getId())
        );
    }

    @Test
    void findById_nonExistingId_returnsEmptyOptional() {
        // Act
        Long id = 100L;
        Optional<Producto> optionalProducto = repository.findById(id);

        // Assert
        assertAll("findById_nonExistingId_returnsEmptyOptional",
                () -> assertNotNull(optionalProducto),
                () -> assertTrue(optionalProducto.isEmpty())
        );
    }

    @Test
    void findByUuid_existingUuid_returnsOptionalWithProducto() {
        // Act
        UUID uuid = UUID.fromString("542f0a0b-064b-4022-b528-3b59f8bae821");
        Optional<Producto> optionalProducto = repository.findByUuid(uuid);

        // Assert
        assertAll("findByUuid_existingUuid_returnsOptionalWithProducto",
                () -> assertNotNull(optionalProducto),
                () -> assertTrue(optionalProducto.isPresent()),
                () -> assertEquals(uuid, optionalProducto.get().getUuid())
        );
    }

    @Test
    void findByUuid_nonExistingUuid_returnsEmptyOptional() {
        // Act
        UUID uuid = UUID.fromString("98bb49a6-3ae5-4e50-a606-db397a8772b2");
        Optional<Producto> optionalProducto = repository.findByUuid(uuid);

        // Assert
        assertAll("findByUuid_nonExistingUuid_returnsEmptyOptional",
                () -> assertNotNull(optionalProducto),
                () -> assertTrue(optionalProducto.isEmpty())
        );
    }

    @Test
    void existsById_existingId_returnsTrue() {
        // Act
        Long id = 1L;
        boolean exists = repository.existsById(id);

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsById_nonExistingId_returnsFalse() {
        // Act
        Long id = 100L;
        boolean exists = repository.existsById(id);

        // Assert
        assertFalse(exists);
    }


    @Test
    void saveButNotExists() {
        // Arrange
        Producto producto = new Producto(
                null, "New Brand", "New Model", "New Description",
                200.0, "http://placeimg.com/640/480/people", 5,
                LocalDateTime.now(), LocalDateTime.now(), UUID.randomUUID(),
                false, categoria
        );

        // Act
        Producto savedProducto = repository.save(producto);
        var all = repository.findAll();

        // Assert
        assertAll("save",
                () -> assertNotNull(savedProducto),
                () -> assertTrue(repository.existsById(savedProducto.getId())),
                () -> assertTrue(all.size() >= 2)
        );
    }

    @Test
    void saveExists() {
        // Arrange
        Producto producto = new Producto(
                1L, "New Brand", "New Model", "New Description",
                200.0, "http://placeimg.com/640/480/people", 5,
                LocalDateTime.now(), LocalDateTime.now(), UUID.randomUUID(), false, categoria
        );

        // Act
        Producto savedProducto = repository.save(producto);
        var all = repository.findAll();

        // Assert
        assertAll("save",
                () -> assertNotNull(savedProducto),
                () -> assertEquals(producto, savedProducto),
                () -> assertTrue(repository.existsById(1L)),
                () -> assertTrue(all.size() >= 2)
        );
    }

    @Test
    void deleteById_existingId() {
        // Act
        Long id = 1L;
        repository.deleteById(id);
        var all = repository.findAll();

        // Assert
        assertAll("deleteById_existingId",
                () -> assertFalse(repository.existsById(id)),
                () -> assertFalse(all.isEmpty())
        );
    }
}
