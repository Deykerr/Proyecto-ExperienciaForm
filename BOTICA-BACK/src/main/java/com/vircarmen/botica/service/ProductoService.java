package com.vircarmen.botica.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.dto.ProductoDTO;
import com.vircarmen.botica.dto.ProductoRequest;
import com.vircarmen.botica.entity.Categoria;
import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.repository.CategoriaRepository;
import com.vircarmen.botica.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoDTO buscarPorCodigoBarras(String codigoBarras) {
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new RuntimeException("No se encontró producto con el código: " + codigoBarras));
        return mapToDTO(producto);
    }

    @Transactional
    public ProductoDTO registrarProducto(ProductoRequest request) {
        Categoria categoria = categoriaRepository.findById(Integer.valueOf(request.idCategoria()))
                .orElseThrow(() -> new RuntimeException("La categoría especificada no existe."));

        Producto producto = new Producto();
        producto.setNombre(request.nombre());
        producto.setCodigoBarras(request.codigoBarras());
        producto.setCodigoSunat(request.codigoSunat());
        producto.setTipoAfectacionIgv(request.tipoAfectacionIgv());

        producto.setPrecioVenta(request.precioVenta());
        producto.setStockMinimo(request.stockMinimo());
        producto.setUnidadesPorPresentacion(request.unidadesPorPresentacion() != null ? request.unidadesPorPresentacion() : 1);
        producto.setPrecioPresentacion(request.precioPresentacion());
        producto.setCategoria(categoria);
        producto.setStockActual(0);

        return mapToDTO(productoRepository.save(producto));
    }

    public Page<ProductoDTO> listarCatalogo(Pageable pageable) {
        return productoRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    public Page<ProductoDTO> buscarProductosPorTermino(String termino, Pageable pageable) {
        return productoRepository.findByNombreContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(termino, termino, pageable)
                .map(this::mapToDTO);
    }

    public ProductoDTO buscarPorId(Integer idProducto) {
        return mapToDTO(productoRepository.findById(Integer.valueOf(idProducto))
                .orElseThrow(() -> new RuntimeException("Producto no encontrado")));
    }

    @Transactional
    public ProductoDTO actualizarProducto(Integer idProducto, ProductoRequest request) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Categoria categoria = categoriaRepository.findById(Integer.valueOf(request.idCategoria()))
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        producto.setNombre(request.nombre());
        producto.setCodigoBarras(request.codigoBarras());
        producto.setCodigoSunat(request.codigoSunat());
        producto.setTipoAfectacionIgv(request.tipoAfectacionIgv());

        producto.setPrecioVenta(request.precioVenta());
        producto.setStockMinimo(request.stockMinimo());
        producto.setUnidadesPorPresentacion(request.unidadesPorPresentacion() != null ? request.unidadesPorPresentacion() : 1);
        producto.setPrecioPresentacion(request.precioPresentacion());
        producto.setCategoria(categoria);

        return mapToDTO(productoRepository.save(producto));
    }

    @Transactional
    public void cambiarEstadoProducto(Integer idProducto, EstadoGeneral estado) {
        Producto producto = productoRepository.findById(Integer.valueOf(idProducto))
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        producto.setEstado(estado);
        productoRepository.save(producto);
    }

   private ProductoDTO mapToDTO(Producto producto) {
    return new ProductoDTO(
            producto.getIdProducto(),
            producto.getNombre(),
            producto.getPresentacion(), // Mapeado a 'descripcion'
            producto.getCodigoBarras(),
            producto.getPrecioVenta(),
            producto.getStockActual(),
            producto.getStockMinimo(), // Mapeado a 'stockMinimo'
            producto.getEstado() != null && producto.getEstado().name().equals("A"), // Mapeado a 'activo' (true si es A)
            producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
            producto.getUnidadesPorPresentacion() != null ? producto.getUnidadesPorPresentacion() : 1,
            producto.getPrecioPresentacion()
    );
}
}


