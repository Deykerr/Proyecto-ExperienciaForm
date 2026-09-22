// --- AUTH ---
export interface AuthRequest {
  username: string;
  password?: string;
}

export interface AuthResponse {
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
  ventasEfectivo?: number;
  ventasYape?: number;
  ventasPlin?: number;
  ventasTarjeta?: number;
  diferencia?: number;
  requiereRevision?: boolean;
  estado: string;
}

export interface CajaSesionRequest {
  montoInicial: number;
}

export interface CajaSesionCierreRequest {
  montoFinal?: number;
  observaciones?: string;
  denominaciones?: { denominacion: number; cantidad: number }[];
}

export interface MovimientoCajaRequest {
  tipoMovimiento: 'INGRESO' | 'EGRESO';
  monto: number;
  motivo: string;
  idempotencyKey: string;
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
  tipoAfectacionIgv: string;
  requiereReceta: boolean;
  condicionVenta: 'SIN_RECETA_MEDICA' | 'CON_RECETA_MEDICA' | 'CON_RECETA_MEDICA_RETENIDA';
  codigoCondicionVentaDigemid?: string;
  registroSanitario?: string;
}

export interface ProductoRequest {
  nombre: string;
  codigoBarras: string;
  codigoSunat: string;
  tipoAfectacionIgv: string;
  precioVenta: number;
  stockMinimo: number;
  idCategoria: number;
  unidadesPorPresentacion: number;
  precioPresentacion?: number;
  requiereReceta: boolean;
  condicionVenta: string;
  registroSanitario?: string;
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

export interface DetalleCompraRequest {
  idProducto: number;
  cantidad: number;
  costoUnitario: number;
  codigoLote?: string;
  fechaVencimiento?: string;
}

export interface CompraRequest {
  idProveedor: number;
  documento: string;
  idempotencyKey: string;
  fechaEsperada?: string;
  observaciones?: string;
  detalles: DetalleCompraRequest[];
}

export interface CompraDTO {
  idCompra: number;
  idProveedor: number;
  proveedor: string;
  documento: string;
  fechaCompra: string;
  fechaEsperada?: string;
  observaciones?: string;
  total: number;
  estado: string;
  detalles: CompraDetalleDTO[];
}

export interface CompraDetalleDTO {
  idDetalleCompra: number;
  idProducto: number;
  producto: string;
  cantidad: number;
  cantidadRecibida: number;
  cantidadPendiente: number;
  costoUnitario: number;
  subtotal: number;
}

export interface RecepcionCompraDTO {
  idRecepcion: number;
  idCompra: number;
  documentoProveedor?: string;
  fecha: string;
  estado: string;
  observaciones?: string;
  detalles: { idDetalleCompra: number; idProducto: number; producto: string; idLote: number; lote: string; cantidad: number; costoUnitario: number }[];
}

export interface DevolucionProveedorDTO {
  idDevolucionProveedor: number;
  idCompra: number;
  idProveedor: number;
  fecha: string;
  motivo: string;
  documentoReferencia?: string;
  estado: string;
  total: number;
  detalles: { idDetalleCompra: number; idProducto: number; producto: string; idLote: number; lote: string; cantidad: number; costoUnitario: number; subtotal: number }[];
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
  montoRecibido: number;
  referencia?: string;
}

export interface VentaRequest {
  idCliente?: number;
  tipoComprobante: string;
  idempotencyKey: string;
  referenciaReceta?: string;
  idReceta?: number;
  items: DetalleVentaDTO[];
  pagos: PagoRequest[];
}

export interface VentaResumenDTO {
  idVenta: number;
  fechaEmision: string;
  cliente: string;
  estado: string;
  subtotal: number;
  igv: number;
  total: number;
  tipoComprobante?: string;
  numeroComprobante?: string;
  idReceta?: number;
  numeroReceta?: string;
  detalles: VentaDetalleResumenDTO[];
}

export interface VentaDetalleResumenDTO {
  idDetalleVenta: number;
  idProducto: number;
  producto: string;
  idLote: number;
  lote: string;
  cantidad: number;
  cantidadDevuelta: number;
  cantidadDisponibleDevolucion: number;
  subtotal: number;
}

export interface RecetaDTO {
  idReceta: number;
  numero: string;
  idCliente?: number;
  pacienteNombre: string;
  pacienteDocumento?: string;
  medicoNombre: string;
  medicoColegiatura: string;
  fechaEmision: string;
  fechaVencimiento?: string;
  condicionVenta: string;
  codigoCondicionVentaDigemid: string;
  estado: string;
  retenidaEn?: string;
  observaciones?: string;
  detalles: { idRecetaDetalle: number; idProducto: number; producto: string; cantidadAutorizada: number; cantidadDispensada: number; cantidadDisponible: number; indicaciones?: string }[];
}

export interface DocumentoElectronicoDTO {
  idDocumento: number;
  idComprobante?: number;
  idDevolucionVenta?: number;
  tipoDocumento: string;
  serie: string;
  correlativo: string;
  fechaEmision: string;
  estado: string;
  hashXml?: string;
  codigoRespuesta?: string;
  descripcionRespuesta?: string;
  intentos: number;
  ultimoIntento?: string;
  siguienteIntento?: string;
}

export interface ResumenDiarioSunatDTO {
  idResumen: number;
  identificador: string;
  fechaReferencia: string;
  fechaGeneracion: string;
  estado: string;
  hashXml?: string;
  ticket?: string;
  codigoRespuesta?: string;
  descripcionRespuesta?: string;
  intentos: number;
  cantidadDocumentos: number;
}

export interface ReporteDigemidDTO {
  idReporte: number;
  periodo: string;
  fechaGeneracion: string;
  estado: string;
  cantidadProductos: number;
  hashArchivo: string;
  fechaEnvio?: string;
  constancia?: string;
  observaciones?: string;
}

export interface IngresoInventarioRequest {
  idProducto: number;
  codigoLote: string;
  fechaVencimiento: string;
  cantidad: number;
  costoUnitario: number;
  motivo: string;
  idempotencyKey: string;
}

export interface KardexDTO {
  idDetalle: number;
  fecha: string;
  tipoMovimiento: string;
  idProducto: number;
  producto: string;
  idLote: number | null;
  codigoLote: string | null;
  cantidad: number;
  stockLoteAnterior: number | null;
  stockLotePosterior: number | null;
  stockProductoAnterior: number;
  stockProductoPosterior: number;
  precioUnitario: number;
  referenciaTipo: string | null;
  referenciaId: number | null;
  motivo: string;
  usuario: string;
}

export interface LoteDTO {
  idLote: number;
  codigoLote: string;
  fechaIngreso: string;
  fechaVencimiento: string;
  stockInicial: number;
  stockActual: number;
  precioCompra: number;
  estado: string;
  idProducto: number;
  producto: string;
  codigoBarras?: string;
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

export type TipoAlertaInventario = 'AGOTADO' | 'STOCK_BAJO' | 'LOTE_VENCIDO' | 'LOTE_POR_VENCER';
export type SeveridadAlertaInventario = 'CRITICA' | 'ADVERTENCIA';

export interface AlertaInventarioDTO {
  idAlerta: string;
  tipo: TipoAlertaInventario;
  severidad: SeveridadAlertaInventario;
  idProducto: number;
  producto: string;
  codigoBarras?: string;
  stockActual: number;
  stockMinimo: number;
  cantidadSugeridaReposicion?: number;
  idLote?: number;
  codigoLote?: string;
  fechaVencimiento?: string;
  diasParaVencer?: number;
  mensaje: string;
  accionSugerida: string;
}

export interface ResumenAlertasInventarioDTO {
  total: number;
  criticas: number;
  advertencias: number;
  agotados: number;
  bajoStock: number;
  vencidosConStock: number;
  proximosAVencer: number;
  diasVencimientoConsultados: number;
  generadoEn: string;
  alertas: AlertaInventarioDTO[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
