package com.gestionproductos.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gestionproductos.model.Producto;
import com.gestionproductos.model.ProductoDAO;

/**
 * Modulo de Productos del proyecto Stock Control (software web).
 * Servicio REST que implementa las operaciones basicas CRUD:
 * consultar, crear, actualizar y eliminar productos.
 *
 * Endpoints:
 *   GET    /api/productos          -> Lista todos los productos
 *   GET    /api/productos/{id}     -> Consulta un producto por id
 *   POST   /api/productos          -> Crea un producto (parametros: nombre, descripcion, precio, cantidad)
 *   PUT    /api/productos/{id}     -> Actualiza un producto existente
 *   DELETE /api/productos/{id}     -> Elimina un producto
 *
 * Evidencia: GA7-220501096-AA3-EV01 (Codificacion de modulos del software
 * stand-alone, web y movil). Este mismo modulo se reutiliza en
 * GA7-220501096-AA5-EV03 (servicios web) y se prueba con Postman en AA5-EV04.
 *
 * Estandares aplicados: nombres en camelCase, constantes en MAYUSCULAS,
 * llaves en todas las estructuras de control, Javadoc en metodos y atributos,
 * y metodos auxiliares para evitar codigo repetido.
 *
 * @author Steven Diaz Morales
 */
@WebServlet("/api/productos/*")
public class ProductoApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Valor que devuelve extraerId cuando el id recibido no es numerico. */
    private static final int ID_INVALIDO = -1;

    /** Mensaje de error reutilizado cuando el producto no existe. */
    private static final String MSG_NO_ENCONTRADO = "Producto no encontrado";

    /** Mensaje de error reutilizado cuando falta el nombre del producto. */
    private static final String MSG_NOMBRE_OBLIGATORIO = "El nombre del producto es obligatorio";

    /** Mensaje de error reutilizado cuando el id de la URL no es valido. */
    private static final String MSG_ID_INVALIDO = "El id del producto no es valido";

    /** Capa de acceso a datos: aqui se guardan y consultan los productos. */
    private final ProductoDAO productoDAO = new ProductoDAO();

    /**
     * GET: lista todos los productos o consulta uno por id.
     *
     * @param request  peticion HTTP recibida
     * @param response respuesta HTTP en formato JSON
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepararRespuesta(request, response);
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo(); // ej: "/3" cuando piden un id especifico

        if (pathInfo == null || pathInfo.equals("/")) {
            // No se especifico id: se listan todos los productos
            Collection<Producto> lista = productoDAO.listarTodos();
            StringBuilder json = new StringBuilder("[");
            int contador = 0;
            for (Producto p : lista) {
                if (contador > 0) {
                    json.append(",");
                }
                json.append(p.toJson());
                contador++;
            }
            json.append("]");
            out.print(json.toString());
        } else {
            // Se especifico un id: se busca el producto puntual
            int id = extraerId(pathInfo);
            if (id == ID_INVALIDO) {
                enviarError(response, out, HttpServletResponse.SC_BAD_REQUEST, MSG_ID_INVALIDO);
                return;
            }
            Producto p = productoDAO.buscarPorId(id);
            if (p == null) {
                enviarError(response, out, HttpServletResponse.SC_NOT_FOUND, MSG_NO_ENCONTRADO);
                return;
            }
            out.print(p.toJson());
        }
        out.flush();
    }

    /**
     * POST: crea un nuevo producto con los parametros enviados.
     * Responde 201 si se crea y 400 si falta el nombre.
     *
     * @param request  peticion HTTP con los datos del producto
     * @param response respuesta HTTP en formato JSON
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepararRespuesta(request, response);
        PrintWriter out = response.getWriter();

        Producto nuevo = leerProductoDeParametros(request);

        if (!nombreEsValido(nuevo)) {
            enviarError(response, out, HttpServletResponse.SC_BAD_REQUEST, MSG_NOMBRE_OBLIGATORIO);
            return;
        }

        Producto creado = productoDAO.crear(nuevo);
        response.setStatus(HttpServletResponse.SC_CREATED);
        out.print(creado.toJson());
        out.flush();
    }

    /**
     * PUT: actualiza un producto existente identificado por el id de la URL.
     * Responde 400 si falta el id o el nombre, y 404 si el producto no existe.
     *
     * @param request  peticion HTTP con el id en la URL y los datos nuevos
     * @param response respuesta HTTP en formato JSON
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepararRespuesta(request, response);
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            enviarError(response, out, HttpServletResponse.SC_BAD_REQUEST,
                    "Debe indicar el id del producto a actualizar");
            return;
        }

        int id = extraerId(pathInfo);
        if (id == ID_INVALIDO) {
            enviarError(response, out, HttpServletResponse.SC_BAD_REQUEST, MSG_ID_INVALIDO);
            return;
        }

        Producto datosNuevos = leerProductoDeParametros(request);

        if (!nombreEsValido(datosNuevos)) {
            enviarError(response, out, HttpServletResponse.SC_BAD_REQUEST, MSG_NOMBRE_OBLIGATORIO);
            return;
        }

        boolean actualizado = productoDAO.actualizar(id, datosNuevos);

        if (actualizado) {
            out.print(productoDAO.buscarPorId(id).toJson());
            out.flush();
        } else {
            enviarError(response, out, HttpServletResponse.SC_NOT_FOUND, MSG_NO_ENCONTRADO);
        }
    }

    /**
     * DELETE: elimina el producto identificado por el id de la URL.
     * Responde 400 si falta el id, y 404 si el producto no existe.
     *
     * @param request  peticion HTTP con el id en la URL
     * @param response respuesta HTTP en formato JSON
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepararRespuesta(request, response);
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            enviarError(response, out, HttpServletResponse.SC_BAD_REQUEST,
                    "Debe indicar el id del producto a eliminar");
            return;
        }

        int id = extraerId(pathInfo);
        if (id == ID_INVALIDO) {
            enviarError(response, out, HttpServletResponse.SC_BAD_REQUEST, MSG_ID_INVALIDO);
            return;
        }

        boolean eliminado = productoDAO.eliminar(id);

        if (eliminado) {
            out.print("{\"mensaje\": \"Producto eliminado correctamente\"}");
            out.flush();
        } else {
            enviarError(response, out, HttpServletResponse.SC_NOT_FOUND, MSG_NO_ENCONTRADO);
        }
    }

    /**
     * Configura la codificacion de la peticion y el tipo de contenido de la respuesta.
     *
     * @param request  peticion HTTP (se fija en UTF-8)
     * @param response respuesta HTTP (se fija como JSON en UTF-8)
     */
    private void prepararRespuesta(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    /**
     * Envia una respuesta de error en JSON con el codigo HTTP indicado.
     * Centraliza el codigo que antes se repetia en cada metodo.
     *
     * @param response respuesta HTTP donde se fija el codigo de estado
     * @param out      escritor de la respuesta
     * @param codigo   codigo HTTP (400, 404, etc.)
     * @param mensaje  texto del error (sin comillas ni caracteres especiales de JSON)
     */
    private void enviarError(HttpServletResponse response, PrintWriter out, int codigo, String mensaje) {
        response.setStatus(codigo);
        out.print("{\"mensaje\": \"" + mensaje + "\"}");
        out.flush();
    }

    /**
     * Valida que el producto tenga un nombre no vacio.
     *
     * @param producto producto a validar
     * @return true si el nombre existe y no esta en blanco
     */
    private boolean nombreEsValido(Producto producto) {
        return producto.getNombre() != null && !producto.getNombre().trim().isEmpty();
    }

    /**
     * Extrae el id numerico desde el pathInfo, ej: "/3" -> 3.
     *
     * @param pathInfo parte de la URL posterior a /api/productos
     * @return el id, o ID_INVALIDO si no es un numero
     */
    private int extraerId(String pathInfo) {
        try {
            return Integer.parseInt(pathInfo.replace("/", ""));
        } catch (NumberFormatException e) {
            return ID_INVALIDO;
        }
    }

    /**
     * Construye un objeto Producto a partir de los parametros enviados en la peticion.
     * Tomcat solo lee el cuerpo de formularios en peticiones POST, por eso en PUT
     * el cuerpo (nombre=...&precio=...) se lee manualmente.
     *
     * @param request peticion HTTP con los parametros
     * @return producto con id 0 y los datos recibidos
     */
    private Producto leerProductoDeParametros(HttpServletRequest request) throws IOException {
        Map<String, String> params = leerParametros(request);
        String nombre = params.get("nombre");
        String descripcion = params.get("descripcion");
        double precio = parsearDouble(params.get("precio"));
        int cantidad = parsearInt(params.get("cantidad"));
        return new Producto(0, nombre, descripcion, precio, cantidad);
    }

    /**
     * Reune los parametros de la URL, del formulario POST y del cuerpo de un PUT.
     *
     * @param request peticion HTTP
     * @return mapa nombre -> valor con todos los parametros encontrados
     */
    private Map<String, String> leerParametros(HttpServletRequest request) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        // Parametros que Tomcat ya leyo (URL y formularios POST)
        for (Map.Entry<String, String[]> e : request.getParameterMap().entrySet()) {
            if (e.getValue().length > 0) {
                params.put(e.getKey(), e.getValue()[0]);
            }
        }

        // En PUT el cuerpo no se lee solo: se procesa manualmente
        String contentType = request.getContentType();
        if ("PUT".equalsIgnoreCase(request.getMethod()) && contentType != null
                && contentType.toLowerCase().startsWith("application/x-www-form-urlencoded")) {
            StringBuilder cuerpo = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) {
                cuerpo.append(linea);
            }
            for (String par : cuerpo.toString().split("&")) {
                if (par.isEmpty()) {
                    continue;
                }
                int igual = par.indexOf('=');
                String clave = igual >= 0 ? par.substring(0, igual) : par;
                String valor = igual >= 0 ? par.substring(igual + 1) : "";
                params.put(URLDecoder.decode(clave, "UTF-8"), URLDecoder.decode(valor, "UTF-8"));
            }
        }
        return params;
    }

    /**
     * Convierte un texto a double. Acepta coma o punto como separador decimal.
     *
     * @param valor texto recibido (puede ser null)
     * @return el numero, o 0 si el texto es null o no es numerico
     */
    private double parsearDouble(String valor) {
        try {
            return valor == null ? 0 : Double.parseDouble(valor.replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Convierte un texto a entero.
     *
     * @param valor texto recibido (puede ser null)
     * @return el numero, o 0 si el texto es null o no es numerico
     */
    private int parsearInt(String valor) {
        try {
            return valor == null ? 0 : Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}