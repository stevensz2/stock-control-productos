package com.gestionproductos.model;

import java.util.Locale;

/**
 * Clase modelo que representa un producto del sistema Stock Control.
 * Se usa como objeto de transferencia entre el servicio web y el cliente.
 *
 * Evidencia: GA7-220501096-AA3-EV01 (Codificacion de modulos del software).
 *
 * @author Steven Diaz Morales
 */
public class Producto {

    /** Identificador unico del producto (lo asigna el DAO). */
    private int id;

    /** Nombre del producto. Es obligatorio. */
    private String nombre;

    /** Descripcion corta del producto. */
    private String descripcion;

    /** Precio unitario del producto. */
    private double precio;

    /** Unidades disponibles en inventario. */
    private int cantidad;

    /**
     * Constructor vacio. Crea un producto sin datos.
     */
    public Producto() {
    }

    /**
     * Constructor completo.
     *
     * @param id          identificador del producto
     * @param nombre      nombre del producto
     * @param descripcion descripcion del producto
     * @param precio      precio unitario
     * @param cantidad    unidades disponibles
     */
    public Producto(int id, String nombre, String descripcion, double precio, int cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    /** @return el identificador del producto */
    public int getId() {
        return id;
    }

    /** @param id nuevo identificador del producto */
    public void setId(int id) {
        this.id = id;
    }

    /** @return el nombre del producto */
    public String getNombre() {
        return nombre;
    }

    /** @param nombre nuevo nombre del producto */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** @return la descripcion del producto */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion nueva descripcion del producto */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** @return el precio unitario del producto */
    public double getPrecio() {
        return precio;
    }

    /** @param precio nuevo precio unitario */
    public void setPrecio(double precio) {
        this.precio = precio;
    }

    /** @return las unidades disponibles en inventario */
    public int getCantidad() {
        return cantidad;
    }

    /** @param cantidad nuevas unidades disponibles */
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * Convierte el producto a formato JSON de forma manual,
     * evitando dependencias externas como Gson.
     * Se usa Locale.US para que el decimal lleve punto (85000.00) y el JSON sea valido
     * sin importar la configuracion regional del sistema operativo.
     *
     * @return texto JSON con los datos del producto
     */
    public String toJson() {
        return String.format(
            Locale.US,
            "{\"id\": %d, \"nombre\": \"%s\", \"descripcion\": \"%s\", \"precio\": %.2f, \"cantidad\": %d}",
            id, escapar(nombre), escapar(descripcion), precio, cantidad
        );
    }

    /**
     * Escapa los caracteres que invalidan un texto JSON
     * (barra invertida, comillas, saltos de linea y tabulaciones).
     *
     * @param texto texto original (puede ser null)
     * @return el texto escapado, o cadena vacia si el texto es null
     */
    private String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}