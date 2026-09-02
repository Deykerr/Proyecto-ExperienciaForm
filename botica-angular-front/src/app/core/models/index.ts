// --- AUTH ---
export interface AuthRequest {
  username: string;
  password?: string;
}

export interface AuthResponse {
  token: string;
  idUsuario: number;
  username: string;
  nombreCompleto: string;
  rol: string;
}

// --- CAJA ---
export interface CajaSesionDTO {
  idCajaSesion: number;
  usuario: string;
  fechaApertura: string;
  fechaCierre?: string;
  montoInicial: number;
  montoFinal?: number;
  totalIngresos?: number;
  totalEgresos?: number;
  saldoCalculado?: number;
  estado: string;
}

export interface CajaSesionRequest {
  montoInicial: number;
}

export interface CajaSesionCierreRequest {
  montoFinal: number;
}

// --- PRODUCTOS E INVENTARIO ---
export interface ProductoDTO {
  idProducto: number;
  nombre: string;
  descripcion?: string; 
  codigoBarras: string;
  precioVenta: number;
  stockActual: number;
  stockMinimo: number;
  activo: boolean;      
  nombreCategoria: string;
  unidadesPorPresentacion: number;
  precioPresentacion?: number;
}

export interface ProductoRequest {
  nombre: string;
  codigoBarras: string;
  codigoSunat: string;
  tipoAfectacionIgv: string;
  precioCompra: number;
  precioVenta: number;
  stockMinimo: number;
  idCategoria: number;
  unidadesPorPresentacion: number;
  precioPresentacion?: number;
}

export interface LaboratorioDTO {
  idLaboratorio: number;
  nombre: string;
  descripcion: string;
  estado: string;
}

export interface LaboratorioRequest {
  nombre: string;
  descripcion: string;
}

export interface CompraRequest {
  productoId: number;
  codigoLote: string;
  cantidad: number;
  precioUnitario: number;
  fechaVencimiento: string; 
  proveedorId: number;
  motivo: string;
}

// --- VENTAS Y CLIENTES ---
export interface ClienteDTO {
  idCliente: number;
  tipoDocumento: string;
  numeroDocumento: string;
  nombreRazonSocial: string;
}

export interface ClienteRequest {
  tipoDocumento: string;
  numeroDocumento: string;
  nombreRazonSocial: string;
  direccion: string;
}

export interface DetalleVentaDTO {
  idProducto: number;
  cantidad: number;
}

export interface PagoVentaDTO {
  metodo: string; 
  monto: number;
}

export interface PagoRequest {
  metodoPago: string;
  monto: number;
}

export interface VentaRequest {
  idCliente: number;
  tipoComprobante: string;
  items: DetalleVentaDTO[];
  pagos: PagoRequest[];
}

// --- USUARIOS Y PROVEEDORES ---
export interface UsuarioDTO {
  idUsuario: number;
  nombreCompleto: string;
  username: string;
  rol: 'ADMIN' | 'CAJERO' | 'ALMACENERO';
  estado: 'A' | 'I';
}

export interface ProveedorDTO {
  idProveedor: number;
  ruc: string;
  razonSocial: string;
  telefono: string;
  correo: string;
  direccion: string;
  estado: string;
}

export interface ProveedorRequest {
  ruc: string;
  razonSocial: string;
  telefono: string;
  correo: string;
  direccion: string;
}

// --- REPORTES Y VARIOS ---
export interface CategoriaDTO {
  idCategoria: number;
  nombre: string;
  descripcion?: string;
  estado?: string;
}

export interface MetricasDTO {
  totalVentas: number;
  cantidadOperaciones: number;
  ticketPromedio: number;
}

export interface BajoStockDTO {
  codigoBarras: string;
  nombre: string;
  stockActual: number;
  stockMinimo: number;
}

export interface LoteVencerDTO {
  codigoLote: string;
  producto: string;
  fechaVencimiento: string;
  cantidad: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
