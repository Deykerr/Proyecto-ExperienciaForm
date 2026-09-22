package com.vircarmen.botica.config;

import com.vircarmen.botica.entity.Categoria;
import com.vircarmen.botica.entity.Cliente;
import com.vircarmen.botica.entity.CondicionVenta;
import com.vircarmen.botica.entity.EstadoLote;
import com.vircarmen.botica.entity.FormaFarmaceutica;
import com.vircarmen.botica.entity.Laboratorio;
import com.vircarmen.botica.entity.Lote;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.entity.Proveedor;
import com.vircarmen.botica.entity.Rol;
import com.vircarmen.botica.entity.UnidadMedida;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.repository.CategoriaRepository;
import com.vircarmen.botica.repository.ClienteRepository;
import com.vircarmen.botica.repository.LaboratorioRepository;
import com.vircarmen.botica.repository.LoteRepository;
import com.vircarmen.botica.repository.ProductoRepository;
import com.vircarmen.botica.repository.ProveedorRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.security.PasswordPolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DataSeeder {

    private static final Logger LOG = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public CommandLineRunner initData(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ClienteRepository clienteRepository,
            LaboratorioRepository laboratorioRepository,
            ProveedorRepository proveedorRepository,
            ProductoRepository productoRepository,
            LoteRepository loteRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicyService passwordPolicyService,
            @Value("${app.demo-data.admin-password:}") String adminPassword,
            @Value("${app.demo-data.cashier-password:}") String cashierPassword,
            @Value("${app.demo-data.stockkeeper-password:}") String stockkeeperPassword) {

        return args -> {
            validarPassword("DEMO_ADMIN_PASSWORD", adminPassword, passwordPolicyService);
            validarPassword("DEMO_CASHIER_PASSWORD", cashierPassword, passwordPolicyService);
            validarPassword("DEMO_STOCKKEEPER_PASSWORD", stockkeeperPassword, passwordPolicyService);

            crearUsuarioSiFalta(usuarioRepository, passwordEncoder, "admin", adminPassword,
                    "Administrador de Pruebas", Rol.ADMIN);
            crearUsuarioSiFalta(usuarioRepository, passwordEncoder, "cajero", cashierPassword,
                    "Cajero de Pruebas", Rol.CAJERO);
            crearUsuarioSiFalta(usuarioRepository, passwordEncoder, "almacenero", stockkeeperPassword,
                    "Almacenero de Pruebas", Rol.ALMACENERO);

            Categoria analgesicos = obtenerOCrearCategoria(categoriaRepository, "Analgésicos",
                    "Medicamentos para el alivio del dolor y la fiebre.");
            Categoria antibioticos = obtenerOCrearCategoria(categoriaRepository, "Antibióticos",
                    "Medicamentos sujetos a prescripción médica.");
            Categoria vitaminas = obtenerOCrearCategoria(categoriaRepository, "Vitaminas y suplementos",
                    "Suplementos nutricionales de venta libre.");

            Laboratorio genericos = obtenerOCrearLaboratorio(laboratorioRepository, "Genéricos Demo",
                    "Laboratorio ficticio para pruebas locales.");
            Laboratorio saludPeru = obtenerOCrearLaboratorio(laboratorioRepository, "Salud Perú Demo",
                    "Laboratorio ficticio para pruebas locales.");

            crearClientePublicoSiFalta(clienteRepository);
            crearProveedorSiFalta(proveedorRepository);

            LocalDate hoy = LocalDate.now();
            crearProductoConLote(productoRepository, loteRepository, analgesicos, genericos,
                    "Paracetamol 500 mg Demo", "775000000001", "MED-DEMO-001", "Caja x 20 tabletas",
                    new BigDecimal("8.50"), 60, 10, CondicionVenta.SIN_RECETA_MEDICA,
                    "L-NORMAL-001", hoy.plusMonths(18));
            crearProductoConLote(productoRepository, loteRepository, analgesicos, saludPeru,
                    "Ibuprofeno 400 mg Demo", "775000000002", "MED-DEMO-002", "Caja x 20 tabletas",
                    new BigDecimal("12.90"), 4, 10, CondicionVenta.SIN_RECETA_MEDICA,
                    "L-BAJO-001", hoy.plusMonths(10));
            crearProductoConLote(productoRepository, loteRepository, antibioticos, genericos,
                    "Amoxicilina 500 mg Demo", "775000000003", "MED-DEMO-003", "Caja x 21 cápsulas",
                    new BigDecimal("18.50"), 0, 5, CondicionVenta.CON_RECETA_MEDICA,
                    "L-AGOTADO-001", hoy.plusMonths(12));
            crearProductoConLote(productoRepository, loteRepository, vitaminas, saludPeru,
                    "Vitamina C 500 mg Demo", "775000000004", "MED-DEMO-004", "Frasco x 30 tabletas",
                    new BigDecimal("16.00"), 25, 5, CondicionVenta.SIN_RECETA_MEDICA,
                    "L-VENCE-005", hoy.plusDays(5));
            crearProductoConLote(productoRepository, loteRepository, analgesicos, genericos,
                    "Loratadina 10 mg Demo", "775000000005", "MED-DEMO-005", "Caja x 10 tabletas",
                    new BigDecimal("7.90"), 12, 5, CondicionVenta.SIN_RECETA_MEDICA,
                    "L-VENCIDO-001", hoy.minusDays(10));
            crearProductoConLote(productoRepository, loteRepository, analgesicos, saludPeru,
                    "Omeprazol 20 mg Demo", "775000000006", "MED-DEMO-006", "Caja x 14 cápsulas",
                    new BigDecimal("11.50"), 8, 10, CondicionVenta.SIN_RECETA_MEDICA,
                    "L-VENCE-020", hoy.plusDays(20));
            crearProductoConLote(productoRepository, loteRepository, analgesicos, genericos,
                    "Clonazepam 0.5 mg Demo", "775000000007", "MED-DEMO-007", "Caja x 30 tabletas",
                    new BigDecimal("21.00"), 15, 5, CondicionVenta.CON_RECETA_MEDICA_RETENIDA,
                    "L-RETENIDA-001", hoy.plusMonths(14));

            LOG.warn("Datos DEMO locales habilitados. No utilices estas cuentas ni este perfil en producción.");
        };
    }

    private void validarPassword(String variable, String password, PasswordPolicyService passwordPolicyService) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(variable + " es obligatoria cuando DEMO_DATA_ENABLED=true");
        }
        passwordPolicyService.validar(password);
    }

    private void crearUsuarioSiFalta(UsuarioRepository repository, PasswordEncoder encoder, String username,
                                     String password, String nombreCompleto, Rol rol) {
        if (repository.findByUsername(username).isPresent()) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPasswordHash(encoder.encode(password));
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setRol(rol);
        repository.save(usuario);
    }

    private Categoria obtenerOCrearCategoria(CategoriaRepository repository, String nombre, String descripcion) {
        return repository.findAll().stream()
                .filter(categoria -> nombre.equalsIgnoreCase(categoria.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(nombre);
                    categoria.setDescripcion(descripcion);
                    return repository.save(categoria);
                });
    }

    private Laboratorio obtenerOCrearLaboratorio(LaboratorioRepository repository, String nombre, String descripcion) {
        return repository.findAll().stream()
                .filter(laboratorio -> nombre.equalsIgnoreCase(laboratorio.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Laboratorio laboratorio = new Laboratorio();
                    laboratorio.setNombre(nombre);
                    laboratorio.setDescripcion(descripcion);
                    return repository.save(laboratorio);
                });
    }

    private void crearClientePublicoSiFalta(ClienteRepository repository) {
        if (repository.findByNumeroDocumento("00000000").isPresent()) {
            return;
        }
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento("DNI");
        cliente.setNumeroDocumento("00000000");
        cliente.setNombreRazonSocial("Público general - Demo");
        cliente.setDireccion("Venta de prueba local");
        repository.save(cliente);
    }

    private void crearProveedorSiFalta(ProveedorRepository repository) {
        if (repository.existsByRuc("20600000001")) {
            return;
        }
        Proveedor proveedor = new Proveedor();
        proveedor.setRuc("20600000001");
        proveedor.setRazonSocial("Distribuidora Farmacéutica Demo S.A.C.");
        proveedor.setTelefono("999000001");
        proveedor.setCorreo("compras@proveedor-demo.local");
        proveedor.setDireccion("Dirección ficticia para pruebas locales");
        repository.save(proveedor);
    }

    private void crearProductoConLote(
            ProductoRepository productoRepository,
            LoteRepository loteRepository,
            Categoria categoria,
            Laboratorio laboratorio,
            String nombre,
            String codigoBarras,
            String registroSanitario,
            String presentacion,
            BigDecimal precioVenta,
            int stockActual,
            int stockMinimo,
            CondicionVenta condicionVenta,
            String codigoLote,
            LocalDate fechaVencimiento) {

        if (productoRepository.findByCodigoBarras(codigoBarras).isPresent()) {
            return;
        }

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setCodigoBarras(codigoBarras);
        producto.setCodigoSunat("51101500");
        producto.setTipoAfectacionIgv("10");
        producto.setPrincipioActivo(nombre.replace(" Demo", ""));
        producto.setPresentacion(presentacion);
        producto.setFormaFarmaceutica(FormaFarmaceutica.TABLETA);
        producto.setUnidadMedida(UnidadMedida.CAJA);
        producto.setPrecioVenta(precioVenta);
        producto.setStockActual(stockActual);
        producto.setStockMinimo(stockMinimo);
        producto.setUnidadesPorPresentacion(1);
        producto.setPrecioPresentacion(precioVenta);
        producto.setCategoria(categoria);
        producto.setLaboratorio(laboratorio);
        producto.setCondicionVenta(condicionVenta);
        producto.setRegistroSanitario(registroSanitario);
        producto = productoRepository.save(producto);

        Lote lote = new Lote();
        lote.setProducto(producto);
        lote.setCodigoLote(codigoLote);
        lote.setFechaIngreso(LocalDate.now().minusDays(30));
        lote.setFechaVencimiento(fechaVencimiento);
        lote.setStockInicial(stockActual);
        lote.setStockActual(stockActual);
        lote.setPrecioCompra(precioVenta.multiply(new BigDecimal("0.60")));
        lote.setEstado(stockActual == 0 ? EstadoLote.AGOTADO
                : fechaVencimiento.isBefore(LocalDate.now()) ? EstadoLote.VENCIDO : EstadoLote.DISPONIBLE);
        loteRepository.save(lote);
    }
}
