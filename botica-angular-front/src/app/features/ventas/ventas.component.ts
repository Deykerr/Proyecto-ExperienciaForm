import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../core/services/producto.service';
import { VentaService } from '../../core/services/venta.service';
import { ClienteService } from '../../core/services/cliente.service';
import { TicketService } from '../../core/services/ticket.service';
import { ProductoDTO as Producto, DetalleVentaDTO as DetalleVenta, ClienteDTO as Cliente } from '../../core/models';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ventas.component.html',
  styleUrls: ['./ventas.component.scss']
})
export class VentasComponent implements OnInit {
  private productoService = inject(ProductoService);
  private ventaService = inject(VentaService);
  private clienteService = inject(ClienteService);
  private ticketService = inject(TicketService);
  private router = inject(Router);
  public authService = inject(AuthService);

  productos: Producto[] = [];
  carrito: DetalleVenta[] = [];
  busqueda = '';
  productosFiltrados: Producto[] = [];
  
  // Datos del Cliente
  criterioBusquedaCliente = 'DNI';
  documentoCliente = '';
  resultadosClientes: Cliente[] = [];
  clienteSeleccionado: Cliente | null = null;
  buscandoCliente = false;
  errorCliente = '';

  // Modal de Pago
  mostrarModalPago = false;
  tipoComprobante = 'BOLETA';
  metodoPago = 'EFECTIVO';
  montoRecibido: number = 0;
  
  // Modal de Edición de Cliente
  mostrarModalEdicionCliente = false;
  clienteEdicion: any = {};
  guardandoCliente = false;
  
  isProcessing = false;
  ventaSuccess = false;

  ngOnInit() {
    this.cargarCatalogo();
  }

  cargarCatalogo() {
    this.productoService.listar().subscribe({
      next: (res: any) => {
        const data = res.content ? res.content : res;
        this.productos = data.filter((p: Producto) => p.activo && p.stockActual > 0);
        this.productosFiltrados = [...this.productos];
      },
      error: () => {
        // Fallback demo data
        this.productos = [
          { id: 1, nombre: 'Paracetamol 500mg', descripcion: 'Analgésico', precioVenta: 5.50, stockActual: 100, stockMinimo: 10, categoriaId: 1, laboratorioId: 1, activo: true },
          { id: 2, nombre: 'Ibuprofeno 400mg', descripcion: 'Antiinflamatorio', precioVenta: 7.20, stockActual: 50, stockMinimo: 10, categoriaId: 1, laboratorioId: 2, activo: true },
          { id: 3, nombre: 'Amoxicilina 500mg', descripcion: 'Antibiótico', precioVenta: 12.00, stockActual: 30, stockMinimo: 10, categoriaId: 2, laboratorioId: 1, activo: true }
        ];
        this.productosFiltrados = [...this.productos];
      }
    });
  }

  buscarProducto() {
    if (!this.busqueda.trim()) {
      this.productosFiltrados = this.productos;
      return;
    }
    const term = this.busqueda.toLowerCase();
    this.productosFiltrados = this.productos.filter(p => p.nombre.toLowerCase().includes(term));
  }

  buscarCliente() {
    if (!this.documentoCliente.trim()) return;
    this.buscandoCliente = true;
    this.errorCliente = '';
    this.resultadosClientes = [];
    
    this.clienteService.buscarPorCriterio(this.criterioBusquedaCliente, this.documentoCliente).subscribe({
      next: (res) => {
        if (res && res.length > 0) {
          if (this.criterioBusquedaCliente === 'DNI' || res.length === 1) {
            this.clienteSeleccionado = res[0];
          } else {
            this.resultadosClientes = res;
          }
        } else {
          this.errorCliente = 'Cliente no encontrado.';
        }
        this.buscandoCliente = false;
      },
      error: () => {
        this.errorCliente = 'Ocurrió un error al buscar.';
        this.clienteSeleccionado = null;
        this.buscandoCliente = false;
      }
    });
  }

  seleccionarDeLista(c: Cliente) {
    this.clienteSeleccionado = c;
    this.resultadosClientes = [];
  }

  removerCliente() {
    this.clienteSeleccionado = null;
    this.documentoCliente = '';
    this.errorCliente = '';
  }

  agregarAlCarrito(prod: Producto) {
    const existe = this.carrito.find(item => item.productoId === prod.id);
    if (existe) {
      if (existe.cantidad < prod.stockActual) {
        existe.cantidad++;
        existe.subtotal = existe.cantidad * existe.precioUnitario;
      }
    } else {
      this.carrito.push({
        productoId: prod.id!,
        cantidad: 1,
        precioUnitario: prod.precioVenta,
        subtotal: prod.precioVenta
      });
    }
  }

  removerDelCarrito(index: number) {
    this.carrito.splice(index, 1);
  }

  aumentarCantidad(item: DetalleVenta, prodStock: number) {
    if (item.cantidad < prodStock) {
      item.cantidad++;
      item.subtotal = item.cantidad * item.precioUnitario;
    }
  }

  disminuirCantidad(item: DetalleVenta, index: number) {
    if (item.cantidad > 1) {
      item.cantidad--;
      item.subtotal = item.cantidad * item.precioUnitario;
    } else {
      this.removerDelCarrito(index);
    }
  }

  get totalVenta(): number {
    return this.carrito.reduce((acc, item) => acc + item.subtotal, 0);
  }

  getProducto(id: number): Producto | undefined {
    return this.productos.find(p => p.id === id);
  }

  abrirModalPago() {
    if (this.carrito.length === 0 || !this.clienteSeleccionado) return;
    this.montoRecibido = this.totalVenta;
    this.mostrarModalPago = true;
  }

  cerrarModalPago() {
    this.mostrarModalPago = false;
  }

  get vuelto(): number {
    if (this.metodoPago !== 'EFECTIVO') return 0;
    const v = this.montoRecibido - this.totalVenta;
    return v > 0 ? v : 0;
  }

  confirmarVenta() {
    if (this.carrito.length === 0 || !this.clienteSeleccionado) return;
    if (this.metodoPago === 'EFECTIVO' && this.montoRecibido < this.totalVenta) {
      alert('El monto recibido es menor al total de la venta.');
      return;
    }

    this.isProcessing = true;
    this.ventaSuccess = false;

    // Build the DTO
    const nuevaVenta = {
      idCliente: this.clienteSeleccionado.idCliente,
      tipoComprobante: this.tipoComprobante,
      items: this.carrito.map(c => ({
        idProducto: c.productoId,
        cantidad: c.cantidad
      })),
      pagos: [
        {
          metodoPago: this.metodoPago,
          monto: this.totalVenta // El monto real aplicado a la venta, el vuelto no se envía
        }
      ]
    };

    this.ventaService.registrarVenta(nuevaVenta).subscribe({
      next: (res) => {
        this.cerrarModalPago();
        this.finalizarExito();
      },
      error: (err) => {
        console.error(err);
        this.isProcessing = false;
        const msg = err.error?.message || 'Hubo un error al procesar la venta en el servidor.';
        alert(msg);
        if (msg.toLowerCase().includes('caja')) {
          this.cerrarModalPago();
          this.router.navigate(['/caja']);
        }
      }
    });
  }

  private finalizarExito() {
    this.isProcessing = false;
    this.ventaSuccess = true;
    
    // Preparar datos para el ticket
    const itemsTicket = this.carrito.map(c => {
      const prod = this.getProducto(c.productoId);
      return {
        producto: prod,
        cantidad: c.cantidad
      };
    });

    const username = this.authService.getUsername() || 'Cajero';

    // Imprimir el ticket antes de limpiar
    this.ticketService.imprimirTicket(
      itemsTicket,
      this.totalVenta,
      this.clienteSeleccionado,
      this.tipoComprobante,
      username
    );

    this.carrito = [];
    this.removerCliente(); // Limpiar el cliente para la siguiente venta
    
    // Esconder mensaje después de 3 seg
    setTimeout(() => {
      this.ventaSuccess = false;
    }, 3000);
  }

  abrirModalEdicionCliente() {
    if (!this.clienteSeleccionado) return;
    this.clienteEdicion = {
      tipoDocumento: this.clienteSeleccionado.tipoDocumento,
      numeroDocumento: this.clienteSeleccionado.numeroDocumento,
      nombreRazonSocial: this.clienteSeleccionado.nombreRazonSocial,
      direccion: (this.clienteSeleccionado as any).direccion || '' // La interfaz DTO podría no tener dirección, pero el request sí
    };
    this.mostrarModalEdicionCliente = true;
  }

  cerrarModalEdicionCliente() {
    this.mostrarModalEdicionCliente = false;
  }

  guardarClienteEditado() {
    if (!this.clienteSeleccionado) return;
    this.guardandoCliente = true;
    this.clienteService.actualizarCliente(this.clienteSeleccionado.idCliente, this.clienteEdicion).subscribe({
      next: (res) => {
        this.clienteSeleccionado = res;
        this.guardandoCliente = false;
        this.cerrarModalEdicionCliente();
      },
      error: (err) => {
        console.error(err);
        this.guardandoCliente = false;
        alert(err.error?.message || 'Error al actualizar cliente.');
      }
    });
  }
}
