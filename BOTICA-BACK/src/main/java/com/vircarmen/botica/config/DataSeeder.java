package com.vircarmen.botica.config;

import com.vircarmen.botica.entity.*;
import com.vircarmen.botica.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(
            UsuarioRepository usuarioRepository, 
            CategoriaRepository categoriaRepository, 
            ClienteRepository clienteRepository, 
            LaboratorioRepository laboratorioRepository,
            ProductoRepository productoRepository,
            LoteRepository loteRepository,
            PasswordEncoder passwordEncoder,
            org.springframework.context.ApplicationContext context) {
            
        return args -> {
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setNombreCompleto("Administrador del Sistema");
                admin.setRol(Rol.ADMIN);
                usuarioRepository.save(admin);
                
                Usuario cajero = new Usuario();
                cajero.setUsername("cajero");
                cajero.setPasswordHash(passwordEncoder.encode("caja123"));
                cajero.setNombreCompleto("Cajero Principal");
                cajero.setRol(Rol.CAJERO);
                usuarioRepository.save(cajero);
                
                System.out.println("====== USUARIOS CREADOS (admin / cajero) ======");
            }
            
            if (categoriaRepository.count() == 0) {
                Categoria c1 = new Categoria(); c1.setNombre("Analgsicos");
                Categoria c2 = new Categoria(); c2.setNombre("Antibticos");
                Categoria c3 = new Categoria(); c3.setNombre("Vitaminas");
                categoriaRepository.save(c1);
                categoriaRepository.save(c2);
                categoriaRepository.save(c3);
                System.out.println("====== CATEGORAS CREADAS ======");
            }
            
            if (clienteRepository.count() == 0) {
                Cliente cli = new Cliente();
                cli.setTipoDocumento("DNI");
                cli.setNumeroDocumento("00000000");
                cli.setNombreRazonSocial("Cliente Pblico General");
                cli.setDireccion("-");
                clienteRepository.save(cli);
                System.out.println("====== CLIENTE DEFAULT CREADO ======");
            }
            
            if (laboratorioRepository.count() == 0) {
                Laboratorio l1 = new Laboratorio(); l1.setNombre("Bayer"); l1.setDescripcion("Bayer Mxico");
                Laboratorio l2 = new Laboratorio(); l2.setNombre("Pfizer"); l2.setDescripcion("Pfizer Lab");
                Laboratorio l3 = new Laboratorio(); l3.setNombre("Genfar"); l3.setDescripcion("Genricos");
                laboratorioRepository.save(l1);
                laboratorioRepository.save(l2);
                laboratorioRepository.save(l3);
                System.out.println("====== LABORATORIOS CREADOS ======");
            }
            
            if (productoRepository.count() == 0) {
                Categoria catAnalg = categoriaRepository.findAll().stream().filter(c -> c.getNombre().equals("Analgsicos")).findFirst().orElse(null);
                Laboratorio labGenfar = laboratorioRepository.findAll().stream().filter(l -> l.getNombre().equals("Genfar")).findFirst().orElse(null);
                Laboratorio labBayer = laboratorioRepository.findAll().stream().filter(l -> l.getNombre().equals("Bayer")).findFirst().orElse(null);
                
                if (catAnalg != null && labGenfar != null && labBayer != null) {
                    java.util.Random rnd = new java.util.Random();
                    
                    for(int i = 1; i <= 100; i++) {
                        Producto p = new Producto();
                        p.setNombre("Producto Medicamento " + i + " 500mg");
                        p.setCodigoBarras("775" + String.format("%010d", i));
                        p.setPrincipioActivo("Principio Activo " + (i%10));
                        p.setFormaFarmaceutica(FormaFarmaceutica.TABLETA);
                        p.setUnidadMedida(UnidadMedida.CAJA);
                        p.setPrecioVenta(BigDecimal.valueOf(5.0 + (rnd.nextDouble() * 20.0)).setScale(2, java.math.RoundingMode.HALF_UP));
                        p.setStockActual(100);
                        p.setStockMinimo(10);
                        p.setCategoria(i % 2 == 0 ? catAnalg : categoriaRepository.findAll().get(0));
                        p.setLaboratorio(i % 2 == 0 ? labGenfar : labBayer);
                        p = productoRepository.save(p);
                        
                        // Generar 10 lotes
                        for(int j = 1; j <= 10; j++) {
                            Lote lote = new Lote();
                            lote.setProducto(p);
                            lote.setCodigoLote("LT-" + i + "-" + String.format("%03d", j));
                            lote.setFechaIngreso(LocalDate.now().minusDays(rnd.nextInt(60)));
                            lote.setFechaVencimiento(LocalDate.now().plusMonths(3 + rnd.nextInt(24)));
                            lote.setStockInicial(10);
                            lote.setStockActual(10);
                            lote.setPrecioCompra(p.getPrecioVenta().multiply(new BigDecimal("0.6")));
                            lote.setEstado(EstadoLote.DISPONIBLE);
                            loteRepository.save(lote);
                        }
                    }
                    
                    System.out.println("====== PRODUCTOS Y LOTES CREADOS (100+) ======");
                }
            }

            // Inyectar Caja y Ventas si no hay ventas
            com.vircarmen.botica.repository.CajaSesionRepository cajaSesionRepository = context.getBean(com.vircarmen.botica.repository.CajaSesionRepository.class);
            com.vircarmen.botica.repository.VentaRepository ventaRepository = context.getBean(com.vircarmen.botica.repository.VentaRepository.class);
            com.vircarmen.botica.repository.DetalleVentaRepository detalleVentaRepository = context.getBean(com.vircarmen.botica.repository.DetalleVentaRepository.class);
            com.vircarmen.botica.repository.PagoRepository pagoRepository = context.getBean(com.vircarmen.botica.repository.PagoRepository.class);

            if (ventaRepository.count() == 0) {
                Usuario cajero = usuarioRepository.findByUsername("cajero").orElseThrow();
                Cliente cliente = clienteRepository.findAll().get(0);
                
                // 1. Crear Caja Abierta
                CajaSesion caja = new CajaSesion();
                caja.setUsuario(cajero);
                caja.setMontoInicial(new BigDecimal("100.00"));
                caja.setFechaApertura(LocalDate.now().atStartOfDay());
                caja.setEstado(CajaSesion.EstadoCaja.ABIERTA);
                caja = cajaSesionRepository.save(caja);

                // 2. Generar 20 Ventas
                java.util.Random rnd = new java.util.Random();
                java.util.List<Producto> todosLosProductos = productoRepository.findAll();
                
                for (int i = 0; i < 20; i++) {
                    Venta venta = new Venta();
                    venta.setCajaSesion(caja);
                    venta.setUsuario(cajero);
                    venta.setCliente(cliente);
                    venta.setFechaEmision(LocalDate.now().atTime(9 + rnd.nextInt(10), rnd.nextInt(60)));
                    
                    Comprobante comp = new Comprobante();
                    comp.setVenta(venta);
                    comp.setTipoComprobante(com.vircarmen.botica.entity.TipoComprobante.BOLETA);
                    comp.setSerie("B001");
                    comp.setCorrelativo(String.format("%06d", i + 1));
                    comp.setFechaEmision(venta.getFechaEmision());
                    comp.setCliente(cliente);
                    
                    BigDecimal totalVenta = BigDecimal.ZERO;
                    
                    // Añadir 1 a 3 detalles por venta
                    int numDetalles = rnd.nextInt(3) + 1;
                    for (int j = 0; j < numDetalles; j++) {
                        Producto p = todosLosProductos.get(rnd.nextInt(todosLosProductos.size()));
                        int cantidad = rnd.nextInt(3) + 1;
                        
                        // Obtener un lote
                        Lote loteAsignado = loteRepository.findAll().stream().filter(l -> l.getProducto().getIdProducto().equals(p.getIdProducto())).findFirst().orElse(null);
                        if (loteAsignado == null) continue;

                        DetalleVenta dv = new DetalleVenta();
                        dv.setVenta(venta);
                        dv.setLote(loteAsignado);
                        dv.setCantidad(cantidad);
                        dv.setPrecioUnitario(p.getPrecioVenta());
                        BigDecimal subtotal = p.getPrecioVenta().multiply(new BigDecimal(cantidad));
                        dv.setSubtotal(subtotal);
                        venta.getDetalles().add(dv);
                        
                        totalVenta = totalVenta.add(subtotal);
                    }
                    
                    venta.setSubtotal(totalVenta.divide(new BigDecimal("1.18"), 2, java.math.RoundingMode.HALF_UP));
                    venta.setIgv(totalVenta.subtract(venta.getSubtotal()));
                    venta.setTotal(totalVenta);
                    
                    comp.setSubtotal(venta.getSubtotal());
                    comp.setIgv(venta.getIgv());
                    comp.setTotal(venta.getTotal());
                    venta.setComprobante(comp);
                    
                    venta = ventaRepository.save(venta);
                    
                    Pago pago = new Pago();
                    pago.setVenta(venta);
                    pago.setCajaSesion(caja);
                    pago.setMetodoPago(com.vircarmen.botica.entity.MetodoPago.EFECTIVO);
                    pago.setMonto(totalVenta);
                    pago.setFechaPago(venta.getFechaEmision());
                    pagoRepository.save(pago);
                }
                
                System.out.println("====== CAJA Y VENTAS GENERADAS ======");
            }
        };
    }
}
