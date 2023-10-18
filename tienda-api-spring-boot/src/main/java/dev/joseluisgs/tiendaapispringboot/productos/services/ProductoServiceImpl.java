package dev.joseluisgs.tiendaapispringboot.productos.services;

import dev.joseluisgs.tiendaapispringboot.categorias.models.Categoria;
import dev.joseluisgs.tiendaapispringboot.categorias.services.CategoriasService;
import dev.joseluisgs.tiendaapispringboot.productos.dto.ProductoCreateDto;
import dev.joseluisgs.tiendaapispringboot.productos.dto.ProductoUpdateDto;
import dev.joseluisgs.tiendaapispringboot.productos.exceptions.ProductoBadUuid;
import dev.joseluisgs.tiendaapispringboot.productos.exceptions.ProductoNotFound;
import dev.joseluisgs.tiendaapispringboot.productos.mappers.ProductoMapper;
import dev.joseluisgs.tiendaapispringboot.productos.models.Producto;
import dev.joseluisgs.tiendaapispringboot.productos.repositories.ProductosRepository;
import dev.joseluisgs.tiendaapispringboot.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de nuestro servicio de productos
 * Aquí implementamos la lógica de negocio
 * Además es cacheable
 */
@Service
@CacheConfig(cacheNames = {"productos"})
@Slf4j
public class ProductoServiceImpl implements ProductosService {
    private final ProductosRepository productosRepository;
    private final CategoriasService categoriaService;
    private final ProductoMapper productosMapper;
    private final StorageService storageService;

    @Autowired
    public ProductoServiceImpl(ProductosRepository productosRepository, CategoriasService categoriaService, ProductoMapper productoMapper, StorageService storageService) {
        this.productosRepository = productosRepository;
        this.categoriaService = categoriaService;
        this.productosMapper = productoMapper;
        this.storageService = storageService;
    }

    /**
     * Busca todos los productos
     *
     * @param marca     Marca del producto
     * @param categoria Categoría del producto
     * @return Lista de productos
     */
    @Override
    public List<Producto> findAll(String marca, String categoria) {
        // Si todo está vacío o nulo, devolvemos todos los productos
        if ((marca == null || marca.isEmpty()) && (categoria == null || categoria.isEmpty())) {
            log.info("Buscando todos los productos");
            return productosRepository.findAll();
        }
        // Si la marca no está vacía, pero la categoría si, buscamos por marca
        if ((marca != null && !marca.isEmpty()) && (categoria == null || categoria.isEmpty())) {
            log.info("Buscando productos por marca: " + marca);
            return productosRepository.findByMarcaContainsIgnoreCase(marca.toLowerCase());
        }
        // Si la marca está vacía, pero la categoría no, buscamos por categoría
        if ((categoria != null && !categoria.isEmpty()) && (marca == null || marca.isEmpty())) {
            log.info("Buscando productos por categoría: " + categoria);
            return productosRepository.findByCategoriaContainsIgnoreCase(categoria.toLowerCase());
        }
        // Si la marca y la categoría no están vacías, buscamos por ambas
        log.info("Buscando productos por marca: " + marca + " y categoría: " + categoria);
        return productosRepository.findByMarcaContainsIgnoreCaseAndCategoriaIgnoreCase(marca.toLowerCase(), categoria.toLowerCase());
    }

    /**
     * Busca un producto por su id
     *
     * @param id Id del producto
     * @return Producto encontrado
     * @throws ProductoNotFound Si no lo encuentra
     */
    @Override
    @Cacheable
    public Producto findById(Long id) {
        log.info("Buscando producto por id: " + id);
        return productosRepository.findById(id).orElseThrow(() -> new ProductoNotFound(id));
    }

    /**
     * Busca un producto por su uuid
     *
     * @param uuid Uuid del producto en formato string
     * @return Producto encontrado
     * @throws ProductoNotFound Si no lo encuentra
     * @throws ProductoBadUuid  Si el uuid no es válido
     */
    @Override
    @Cacheable
    public Producto findbyUuid(String uuid) {
        log.info("Buscando producto por uuid: " + uuid);
        try {
            var myUUID = UUID.fromString(uuid);
            return productosRepository.findByUuid(myUUID).orElseThrow(() -> new ProductoNotFound(myUUID));
        } catch (IllegalArgumentException e) {
            throw new ProductoBadUuid(uuid);
        }
    }

    /**
     * Guarda un producto
     *
     * @param productoCreateDto Producto a guardar
     * @return Producto guardado
     */
    @Override
    @CachePut
    public Producto save(ProductoCreateDto productoCreateDto) {
        log.info("Guardando producto: " + productoCreateDto);
        // Buscamos la categoría por su nombre
        var categoria = categoriaService.findByNombre(productoCreateDto.getCategoria());
        // Creamos el producto nuevo con los datos que nos vienen del dto, podríamos usar el mapper
        // Lo guardamos en el repositorio
        return productosRepository.save(productosMapper.toProduct(productoCreateDto, categoria));
    }

    /**
     * Actualiza un producto
     *
     * @param id                Id del producto a actualizar
     * @param productoUpdateDto Producto a actualizar
     * @return Producto actualizado
     * @throws ProductoNotFound Si no lo encuentra
     */
    @Override
    @CachePut
    public Producto update(Long id, ProductoUpdateDto productoUpdateDto) {
        log.info("Actualizando producto por id: " + id);
        // Si no existe lanza excepción, por eso ya llamamos a lo que hemos implementado antes
        var productoActual = this.findById(id);
        // Buscamos la categoría por su nombre
        // Si no tenemos categoría, no la actualizamos
        Categoria categoria = null;
        if (productoUpdateDto.getCategoria() != null && !productoUpdateDto.getCategoria().isEmpty()) {
            categoria = categoriaService.findByNombre(productoUpdateDto.getCategoria());
        } else {
            categoria = productoActual.getCategoria();
        }
        // Actualizamos el producto con los datos que nos vienen del dto, podríamos usar el mapper
        // Lo guardamos en el repositorio
        return productosRepository.save(productosMapper.toProduct(productoUpdateDto, productoActual, categoria));
    }

    /**
     * Borra un producto
     *
     * @param id Id del producto a borrar
     * @throws ProductoNotFound Si no lo encuentra
     */
    @Override
    @CacheEvict
    // @Transactional // Para que se haga todo o nada y no se quede a medias (por el update)
    public void deleteById(Long id) {
        log.debug("Borrando producto por id: " + id);
        // Si no existe lanza excepción, por eso ya llamamos a lo que hemos implementado antes
        var prod = this.findById(id);
        // Lo borramos del repositorio
        productosRepository.deleteById(id);
        // O lo marcamos como borrado, para evitar problemas de cascada, no podemos borrar productos en pedidos!!!
        //productosRepository.updateIsDeletedToTrueById(id);
        // Borramos la imagen del producto si existe y es distinta a la por defecto
        if (prod.getImagen() != null && !prod.getImagen().equals(Producto.IMAGE_DEFAULT)) {
            storageService.delete(prod.getImagen());
        }

    }

    /**
     * Actualiza la imagen de un producto
     *
     * @param id    Id del producto a actualizar
     * @param image Imagen a actualizar del producto en formato Multipart
     * @return Producto actualizado
     * @throws ProductoNotFound Si no lo encuentra
     */
    @Override
    @CachePut
    public Producto updateImage(Long id, MultipartFile image) {
        log.info("Actualizando imagen de producto por id: " + id);
        // Si no existe lanza excepción, por eso ya llamamos a lo que hemos implementado antes
        var productoActual = this.findById(id);
        String imageStored = storageService.store(image);
        String imageUrl = imageStored; //storageService.getUrl(imageStored); // Si quiero la url completa
        // Clonamos el producto con la nueva imagen, porque inmutabilidad de los objetos
        var productoActualizado = new Producto(
                productoActual.getId(),
                productoActual.getMarca(),
                productoActual.getModelo(),
                productoActual.getDescripcion(),
                productoActual.getPrecio(),
                imageUrl,
                productoActual.getStock(),
                productoActual.getCreatedAt(),
                LocalDateTime.now(),
                productoActual.getUuid(),
                productoActual.getIsDeleted(),
                productoActual.getCategoria()
        );
        return productosRepository.save(productoActualizado);
    }
}
