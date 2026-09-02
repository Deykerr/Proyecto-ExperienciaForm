import { Injectable } from '@angular/core';
import { ClienteDTO } from '../models';

@Injectable({
    providedIn: 'root'
})
export class TicketService {

    imprimirTicket(
        carrito: any[],
        total: number,
        cliente: ClienteDTO | null,
        tipoComprobante: string,
        cajero: string
    ) {
        // Formatear la fecha actual
        const fecha = new Intl.DateTimeFormat('es-PE', {
            dateStyle: 'short',
            timeStyle: 'short'
        }).format(new Date());

        // Identificar al cliente (Si es nulo, es "Cliente Varios")
        const nombreCliente = cliente ? cliente.nombreRazonSocial : 'Cliente Varios';
        const docCliente = cliente ? `${cliente.tipoDocumento}: ${cliente.numeroDocumento}` : 'DNI: 00000000';
        const subtotal = (total / 1.18).toFixed(2);
        const igv = (total - (total / 1.18)).toFixed(2);

        // Armar las filas de la tabla de productos
        let filasProductos = '';
        carrito.forEach(item => {
            const subtotalItem = (item.producto.precioVenta * item.cantidad).toFixed(2);
            filasProductos += `
        <tr>
          <td>${item.cantidad}</td>
          <td>${item.producto.nombre.substring(0, 15)}...</td>
          <td>S/ ${item.producto.precioVenta.toFixed(2)}</td>
          <td style="text-align: right;">S/ ${subtotalItem}</td>
        </tr>
      `;
        });

        // Plantilla HTML del Ticket (Ancho estándar de 80mm para ticketeras)
        const ticketHTML = `
      <html>
        <head>
          <title>Ticket de Venta</title>
          <style>
            @page { margin: 0; }
            body { 
              font-family: 'Courier New', Courier, monospace; 
              width: 72mm; /* Margen de seguridad para rollo de 80mm */
              margin: 0 auto; 
              padding: 10px; 
              font-size: 12px; 
              color: #000; 
            }
            .text-center { text-align: center; }
            .header-title { font-size: 16px; font-weight: bold; margin: 0; }
            .divider { border-top: 1px dashed #000; margin: 10px 0; }
            table { width: 100%; border-collapse: collapse; }
            th { border-bottom: 1px dashed #000; padding-bottom: 4px; text-align: left; }
            td { padding: 4px 0; vertical-align: top; }
            .totals { text-align: right; font-weight: bold; }
          </style>
        </head>
        <body>
          <div class="text-center">
            <p class="header-title">BOTICA VIRGEN DEL CARMEN</p>
            <p style="margin: 2px 0;">RUC: 20123456789</p>
            <p style="margin: 2px 0;">Av. Principal 123 - Ayacucho</p>
          </div>
          
          <div class="divider"></div>
          
          <p style="margin: 2px 0;"><strong>${tipoComprobante} DE VENTA ELECTRÓNICA</strong></p>
          <p style="margin: 2px 0;">Fecha: ${fecha}</p>
          <p style="margin: 2px 0;">Cajero: ${cajero}</p>
          
          <div class="divider"></div>
          
          <p style="margin: 2px 0;"><strong>Cliente:</strong> ${nombreCliente}</p>
          <p style="margin: 2px 0;"><strong>Doc:</strong> ${docCliente}</p>
          
          <div class="divider"></div>
          
          <table>
            <thead>
              <tr>
                <th>Cant</th>
                <th>Descripción</th>
                <th>P.U.</th>
                <th style="text-align: right;">Total</th>
              </tr>
            </thead>
            <tbody>
              ${filasProductos}
            </tbody>
          </table>
          
          <div class="divider"></div>
          
          <table style="width: 100%;">
            <tr>
              <td>Subtotal:</td>
              <td class="totals">S/ ${subtotal}</td>
            </tr>
            <tr>
              <td>IGV (18%):</td>
              <td class="totals">S/ ${igv}</td>
            </tr>
            <tr>
              <td style="font-size: 14px;"><strong>TOTAL A PAGAR:</strong></td>
              <td class="totals" style="font-size: 14px;"><strong>S/ ${total.toFixed(2)}</strong></td>
            </tr>
          </table>
          
          <div class="divider"></div>
          
          <div class="text-center">
            <p>¡Gracias por su compra!</p>
            <p>Conserve este ticket para devoluciones.</p>
          </div>
        </body>
      </html>
    `;

        // Lógica para abrir una ventana oculta, inyectar el HTML e imprimir
        const ventanaImpresion = window.open('', '_blank', 'width=400,height=600');
        if (ventanaImpresion) {
            ventanaImpresion.document.write(ticketHTML);
            ventanaImpresion.document.close();

            // Esperamos un milisegundo para asegurar que el DOM cargó antes de imprimir
            setTimeout(() => {
                ventanaImpresion.focus();
                ventanaImpresion.print();
                ventanaImpresion.close();
            }, 250);
        } else {
            alert('Por favor, permita las ventanas emergentes (pop-ups) para imprimir el ticket.');
        }
    }
}