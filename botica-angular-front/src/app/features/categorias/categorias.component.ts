import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoriasService, Categoria } from '../../core/services/categoria.service';

@Component({
    selector: 'app-categorias',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './categorias.component.html',
    styleUrl: './categorias.component.scss' // Reutiliza el SCSS de Productos
})
export class CategoriasComponent implements OnInit {
    private fb = inject(FormBuilder);
    private categoriasService = inject(CategoriasService);

    categorias = signal<Categoria[]>([]);
    mostrandoFormulario = signal(false);
    isSubmitting = signal(false);

    categoriaForm = this.fb.nonNullable.group({
        nombre: ['', [Validators.required, Validators.minLength(3)]],
        descripcion: ['']
    });

    ngOnInit() {
        this.cargarCategorias();
    }

    cargarCategorias() {
        this.categoriasService.listarCategorias().subscribe(data => this.categorias.set(data));
    }

    guardarCategoria() {
        if (this.categoriaForm.valid) {
            this.isSubmitting.set(true);
            this.categoriasService.guardarCategoria(this.categoriaForm.getRawValue()).subscribe({
                next: () => {
                    alert('Categoría registrada.');
                    this.cargarCategorias();
                    this.mostrandoFormulario.set(false);
                    this.isSubmitting.set(false);
                },
                error: () => {
                    alert('Error: Posiblemente el nombre ya existe.');
                    this.isSubmitting.set(false);
                }
            });
        }
    }
}