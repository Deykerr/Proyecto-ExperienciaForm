import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../core/services/producto.service';
import { VentaService } from '../../core/services/venta.service';
import { ClienteService } from '../../core/services/cliente.service';
import { TicketService } from '../../core/services/ticket.service';
import { ProductoDTO as Producto, ClienteDTO as Cliente, VentaRequest } from '../../core/models';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface CarritoItem {
  productoId: number;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

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
  carrito: CarritoItem[] = [];
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
  referenciaPago = '';
  referenciaReceta = '';
  
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
        this.productos = [];
        this.productosFiltrados = [];
        alert('No se pudo cargar el catálogo. Verifica la conexión con el servidor.');
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
    const existe = this.carrito.find(item => item.productoId === prod.idProducto);
    if (existe) {
      if (existe.cantidad < prod.stockActual) {
        existe.cantidad++;
        existe.subtotal = this.calcularTotalProducto(prod, existe.cantidad);
        existe.precioUnitario = existe.subtotal / existe.cantidad;
      }
    } else {
      this.carrito.push({
        productoId: prod.idProducto,
        cantidad: 1,
        precioUnitario: prod.precioVenta,
        subtotal: prod.precioVenta
      });
    }
  }

  removerDelCarrito(index: number) {
    this.carrito.splice(index, 1);
  }

  aumentarCantidad(item: CarritoItem, prodStock: number) {
    if (item.cantidad < prodStock) {
      item.cantidad++;
      const producto = this.getProducto(item.productoId);
      if (producto) {
        item.subtotal = this.calcularTotalProducto(producto, item.cantidad);
        item.precioUnitario = item.subtotal / item.cantidad;
      }
    }
  }

  disminuirCantidad(item: CarritoItem, index: number) {
    if (item.cantidad > 1) {
      item.cantidad--;
      const producto = this.getProducto(item.productoId);
      if (producto) {
        item.subtotal = this.calcularTotalProducto(producto, item.cantidad);
        item.precioUnitario = item.subtotal / item.cantidad;
      }
    } else {
      this.removerDelCarrito(index);
    }
  }

  get totalVenta(): number {
    return this.carrito.reduce((acc, item) => acc + item.subtotal, 0);
  }

  getProducto(id: number): Producto | undefined {
    return this.productos.find(p => p.idProducto === id);
  }

  get requiereReceta(): boolean {
    return this.carrito.some(item => Boolean(this.getProducto(item.productoId)?.requiereReceta));
  }

  private calcularTotalProducto(producto: Producto, cantidad: number): number {
    const unidades = producto.unidadesPorPresentacion || 1;
    if (unidades > 1 && producto.precioPresentacion) {
      const presentaciones = Math.floor(cantidad / unidades);
      const sueltas = cantidad % unidades;
      return Number((presentaciones * producto.precioPresentacion + sueltas * producto.precioVenta).toFixed(2));
    }
    return Number((cantidad * producto.precioVenta).toFixed(2));
  }

  get subtotalVenta(): number {
    return Number(this.carrito.reduce((suma, item) => {
      const producto = this.getProducto(item.productoId);
      return suma + (producto?.tipoAfectacionIgv === '10' ? item.subtotal / 1.18 : item.subtotal);
    }, 0).toFixed(2));
  }

  get igvVenta(): number {
    return Number((this.totalVenta - this.subtotalVenta).toFixed(2));
  }

  abrirModalPago() {
    if (this.carrito.length === 0 || !this.clienteSeleccionado) return;
    this.montoRecibido = this.totalVenta;
    this.referenciaPago = '';
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
    if (this.metodoPago !== 'EFECTIVO' && !this.referenciaPago.trim()) {
      alert('Ingresa la referencia de la operación electrónica.');
      return;
    }
    if (this.requiereReceta && !this.referenciaReceta.trim()) {
      alert('Esta venta incluye productos que requieren receta. Ingresa su referencia.');
      return;
    }

    this.isProcessing = true;
    this.ventaSuccess = false;

    // Build the DTO
    const nuevaVenta: VentaRequest = {
      idCliente: this.clienteSeleccionado.idCliente,
      tipoComprobante: this.tipoComprobante,
      idempotencyKey: crypto.randomUUID(),
      referenciaReceta: this.referenciaReceta.trim() || undefined,
      items: this.carrito.map(c => ({
        idProducto: c.productoId,
        cantidad: c.cantidad
      })),
      pagos: [
        {
          metodoPago: this.metodoPago,
          montoRecibido: this.metodoPago === 'EFECTIVO' ? this.montoRecibido : this.totalVenta,
          referencia: this.metodoPago === 'EFECTIVO' ? undefined : this.referenciaPago.trim()
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
      username,
      this.subtotalVenta,
      this.igvVenta
    );

    this.carrito = [];
    this.referenciaReceta = '';
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
