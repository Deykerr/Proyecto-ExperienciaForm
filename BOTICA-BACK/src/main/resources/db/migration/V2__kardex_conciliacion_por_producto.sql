-- Permite auditar correcciones del saldo agregado sin atribuirlas a un lote
-- que en realidad no cambió.
ALTER TABLE detalle_movimientos
    ADD COLUMN IF NOT EXISTS producto_id INTEGER REFERENCES productos(id_producto);

UPDATE detalle_movimientos d
SET producto_id = l.producto_id
FROM lotes l
WHERE d.lote_id = l.id_lote
  AND d.producto_id IS NULL;

ALTER TABLE detalle_movimientos ALTER COLUMN producto_id SET NOT NULL;
ALTER TABLE detalle_movimientos ALTER COLUMN lote_id DROP NOT NULL;
ALTER TABLE detalle_movimientos ALTER COLUMN stock_lote_anterior DROP NOT NULL;
ALTER TABLE detalle_movimientos ALTER COLUMN stock_lote_posterior DROP NOT NULL;

CREATE INDEX IF NOT EXISTS ix_detalle_movimientos_producto
    ON detalle_movimientos(producto_id);
