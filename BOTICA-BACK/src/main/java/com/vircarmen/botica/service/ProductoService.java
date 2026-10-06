package com.vircarmen.botica.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.dto.ProductoDTO;
import com.vircarmen.botica.dto.ProductoRequest;
import com.vircarmen.botica.entity.Categoria;
import com.vircarmen.botica.entity.CondicionVenta;
import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.CategoriaRepository;
import com.vircarmen.botica.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public ProductoDTO buscarPorCodigoBarras(String codigoBarras) {
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new RuntimeException("No se encontró producto con el código: " + codigoBarras));
        return mapToDTO(producto);
    }

    @Transactional
    public ProductoDTO registrarProducto(ProductoRequest request) {
        validarPresentacion(request);
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
        producto.setCondicionVenta(resolverCondicionVenta(request));
        producto.setRegistroSanitario(normalizar(request.registroSanitario()));
        producto.setCategoria(categoria);
        producto.setStockActual(0);

        return mapToDTO(productoRepository.save(producto));
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarCatalogo(Pageable pageable) {
        return productoRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarProductosPorTermino(String termino, Pageable pageable) {
        return productoRepository.findByNombreContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(termino, termino, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarDestacados(int limiteSolicitado) {
        int limite = Math.max(1, Math.min(limiteSolicitado, 12));
        LocalDate hoy = LocalDate.now();
        List<Integer> idsMasVendidos = productoRepository.findIdsMasVendidosDesde(
                LocalDateTime.now().minusDays(90),
                hoy,
                PageRequest.of(0, limite));

        Map<Integer, Producto> productosPorId = productoRepository.findAllById(idsMasVendidos).stream()
                .collect(Collectors.toMap(Producto::getIdProducto, Function.identity()));
        LinkedHashMap<Integer, Producto> destacados = new LinkedHashMap<>();
        idsMasVendidos.forEach(id -> {
            Producto producto = productosPorId.get(id);
            if (producto != null) {
                destacados.put(id, producto);
            }
        });

        if (destacados.size() < limite) {
            productoRepository.findDisponiblesParaVenta(hoy, PageRequest.of(0, limite * 2)).forEach(producto -> {
                if (destacados.size() < limite) {
                    destacados.putIfAbsent(producto.getIdProducto(), producto);
                }
            });
        }

        return destacados.values().stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO buscarPorId(Integer idProducto) {
        return mapToDTO(productoRepository.findById(Integer.valueOf(idProducto))
                .orElseThrow(() -> new RuntimeException("Producto no encontrado")));
    }

    @Transactional
    public ProductoDTO actualizarProducto(Integer idProducto, ProductoRequest request) {
        validarPresentacion(request);
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
        producto.setCondicionVenta(resolverCondicionVenta(request));
        producto.setRegistroSanitario(normalizar(request.registroSanitario()));
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
            producto.getPrecioPresentacion(),
            producto.getTipoAfectacionIgv(),
            Boolean.TRUE.equals(producto.getRequiereReceta()),
            producto.getCondicionVenta().name(),
            producto.getCondicionVenta().getCodigoDigemid(),
            producto.getRegistroSanitario()
    );
}

    private CondicionVenta resolverCondicionVenta(ProductoRequest request) {
        if (request.condicionVenta() == null || request.condicionVenta().isBlank()) {
            return Boolean.TRUE.equals(request.requiereReceta())
                    ? CondicionVenta.CON_RECETA_MEDICA
                    : CondicionVenta.SIN_RECETA_MEDICA;
        }
        try {
            return CondicionVenta.valueOf(request.condicionVenta().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Condición de venta no válida.");
        }
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase();
    }

    private void validarPresentacion(ProductoRequest request) {
        int unidades = request.unidadesPorPresentacion() == null ? 1 : request.unidadesPorPresentacion();
        if (unidades > 1 && request.precioPresentacion() == null) {
            throw new BusinessException("El precio por presentación es obligatorio cuando contiene varias unidades.");
        }
    }
}


