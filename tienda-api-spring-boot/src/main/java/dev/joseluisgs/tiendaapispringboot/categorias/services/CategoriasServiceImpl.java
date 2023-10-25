package dev.joseluisgs.tiendaapispringboot.categorias.services;

import dev.joseluisgs.tiendaapispringboot.categorias.dto.CategoriaDto;
import dev.joseluisgs.tiendaapispringboot.categorias.exceptions.CategoriaConflict;
import dev.joseluisgs.tiendaapispringboot.categorias.exceptions.CategoriaNotFound;
import dev.joseluisgs.tiendaapispringboot.categorias.mappers.CategoriasMapper;
import dev.joseluisgs.tiendaapispringboot.categorias.models.Categoria;
import dev.joseluisgs.tiendaapispringboot.categorias.repositories.CategoriasRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@CacheConfig(cacheNames = {"categorias"})
public class CategoriasServiceImpl implements CategoriasService {
    private final CategoriasRepository categoriasRepository;
    private final CategoriasMapper categoriasMapper;

    @Autowired
    public CategoriasServiceImpl(CategoriasRepository categoriasRepository, CategoriasMapper categoriasMapper) {
        this.categoriasRepository = categoriasRepository;
        this.categoriasMapper = categoriasMapper;
    }

    @Override
    public Page<Categoria> findAll(Optional<String> nombre, Optional<Boolean> isDeleted, Pageable pageable) {
        log.info("Buscando todos las categorias con nombre: " + nombre + " y borrados: " + isDeleted);
        // Criterio de búsqueda por nombre
        Specification<Categoria> specNombreCategoria = (root, query, criteriaBuilder) ->
                nombre.map(m -> criteriaBuilder.like(criteriaBuilder.lower(root.get("nombre")), "%" + m + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Criterio de búsqueda por borrado
        Specification<Categoria> specIsDeleted = (root, query, criteriaBuilder) ->
                isDeleted.map(m -> criteriaBuilder.equal(root.get("isDeleted"), m))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Combinamos las especificaciones
        Specification<Categoria> criterio = Specification.where(specNombreCategoria)
                .and(specIsDeleted);

        return categoriasRepository.findAll(criterio, pageable);
    }

    @Override
    public Categoria findByNombre(String nombre) {
        log.info("Buscando categoría por nombre: " + nombre);
        return categoriasRepository.findByNombreEqualsIgnoreCase(nombre).orElseThrow(() -> new CategoriaNotFound(nombre));
    }


    @Override
    @Cacheable(key = "#id")
    public Categoria findById(Long id) {
        log.info("Buscando categoría por id: " + id);
        return categoriasRepository.findById(id).orElseThrow(() -> new CategoriaNotFound(id));
    }

    @Override
    @CachePut(key = "#result.id")
    public Categoria save(CategoriaDto categoriaDto) {
        log.info("Guardando categoría: " + categoriaDto);
        // No debe existir una con el mismo nombre
        categoriasRepository.findByNombreEqualsIgnoreCase(categoriaDto.getNombre()).ifPresent(c -> {
            throw new CategoriaConflict("Ya existe una categoría con el nombre " + categoriaDto.getNombre());
        });
        return categoriasRepository.save(categoriasMapper.toCategoria(categoriaDto));
    }

    @Override
    @CachePut(key = "#result.id")
    public Categoria update(Long id, CategoriaDto categoriaDto) {
        log.info("Actualizando categoría: " + categoriaDto);
        Categoria categoriaActual = findById(id);
        // No debe existir una con el mismo nombre, y si existe soy yo mismo
        categoriasRepository.findByNombreEqualsIgnoreCase(categoriaDto.getNombre()).ifPresent(c -> {
            if (!c.getId().equals(id)) {
                throw new CategoriaConflict("Ya existe una categoría con el nombre " + categoriaDto.getNombre());
            }
        });
        // Actualizamos los datos
        return categoriasRepository.save(categoriasMapper.toCategoria(categoriaDto, categoriaActual));
    }

    @Override
    @CacheEvict(key = "#id")
    @Transactional // Para que se haga todo o nada y no se quede a medias (por el update)
    public void deleteById(Long id) {
        log.info("Borrando categoría por id: " + id);
        Categoria categoria = findById(id);
        //categoriasRepository.deleteById(id);
        // O lo marcamos como borrado, para evitar problemas de cascada, no podemos borrar categorías con productos!!!
        // La otra forma es que comprobaramos si hay productos para borrarlos antes
        // categoriasRepository.updateIsDeletedToTrueById(id);
        // Otra forma es que comprobaramos si hay productos para borrarlos antes
        if (categoriasRepository.existsProductoById(id)) {
            log.warn("No se puede borrar la categoría con id: " + id + " porque tiene productos asociados");
            throw new CategoriaConflict("No se puede borrar la categoría con id " + id + " porque tiene productos asociados");
        } else {
            categoriasRepository.deleteById(id);
        }

    }
}
