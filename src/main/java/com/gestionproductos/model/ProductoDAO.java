package com.gestionproductos.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase de acceso a datos (DAO) para el manejo de productos.
 *
 * Para esta evidencia (GA7-220501096-AA3-EV01) los datos se guardan en memoria,
 * por lo que se pierden cuando se reinicia el servidor. Las firmas de los metodos
 * publicos estan pensadas para que, mas adelante, la logica interna pueda
 * reemplazarse por consultas SQL (JDBC) sin modificar el servlet que la usa.
 *
 * Se usan estructuras seguras para varios hilos (ConcurrentHashMap y AtomicInteger),
 * porque Tomcat atiende varias peticiones al mismo tiempo.
 *
 * @author Steven Diaz Morales
 */
public class ProductoDAO {

    /** Almacen en memoria: clave = id del producto, valor = el producto. */
    private static final Map<Integer, Producto> PRODUCTOS = new ConcurrentHashMap<>();

    /** Contador que genera los ids autoincrementales, empezando en 1. */
    private static final AtomicInteger CONTADOR = new AtomicInteger(1);

    // Datos de ejemplo precargados para poder probar la API de inmediato
    static {
        agregar(new Producto(0, "Teclado mecanico", "Teclado USB retroiluminado", 85000, 20));
        agregar(new Producto(0, "Mouse inalambrico", "Mouse optico 2.4GHz", 45000, 35));
    }

    /**
     * Metodo auxiliar estatico: asigna un id autoincremental y guarda el producto.
     * Lo usan el bloque estatico (datos de ejemplo) y el metodo crear().
     *
     * @param producto producto a guardar (su id se sobreescribe)
     * @return el mismo producto, ya con su id asignado
     */
    private static Producto agregar(Producto producto) {
        int nuevoId = CONTADOR.getAndIncrement();
        producto.setId(nuevoId);
        PRODUCTOS.put(nuevoId, producto);
        return producto;
    }

    /**
     * Crea un nuevo producto y le asigna un id autoincremental.
     *
     * @param producto datos del producto a crear
     * @return el producto creado, con su id
     */
    public Producto crear(Producto producto) {
        return agregar(producto);
    }

    /**
     * Retorna todos los productos registrados.
     *
     * @return copia de la lista de productos (puede estar vacia)
     */
    public Collection<Producto> listarTodos() {
        return new ArrayList<>(PRODUCTOS.values());
    }

    /**
     * Busca un producto por su id.
     *
     * @param id identificador del producto
     * @return el producto, o null si no existe
     */
    public Producto buscarPorId(int id) {
        return PRODUCTOS.get(id);
    }

    /**
     * Actualiza un producto existente conservando su id.
     *
     * @param id          identificador del producto a actualizar
     * @param datosNuevos nuevos datos del producto
     * @return true si el producto existia y se actualizo, false si no existe
     */
    public boolean actualizar(int id, Producto datosNuevos) {
        if (!PRODUCTOS.containsKey(id)) {
            return false;
        }
        datosNuevos.setId(id);
        PRODUCTOS.put(id, datosNuevos);
        return true;
    }

    /**
     * Elimina un producto por su id.
     *
     * @param id identificador del producto a eliminar
     * @return true si existia y fue eliminado, false si no existia
     */
    public boolean eliminar(int id) {
        return PRODUCTOS.remove(id) != null;
    }
}