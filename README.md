# Stock Control - Modulo de Productos

Modulo de Productos del proyecto **Stock Control** (software web), desarrollado con Java y Servlets sobre Apache Tomcat 9.

Evidencia SENA: **GA7-220501096-AA3-EV01** - Codificacion de modulos del software stand-alone, web y movil.

## Que incluye

- **Modelo** (`Producto`): entidad con id, nombre, descripcion, precio y cantidad.
- **Acceso a datos** (`ProductoDAO`): operaciones CRUD con almacenamiento en memoria.
- **Servicio REST** (`ProductoApiServlet`): expone la API de productos.
- **Interfaz web** (`index.html`): tabla y formulario para crear, editar y eliminar productos.

## Endpoints

| Metodo | Ruta                  | Descripcion                    |
|--------|-----------------------|--------------------------------|
| GET    | `/api/productos`      | Lista todos los productos      |
| GET    | `/api/productos/{id}` | Consulta un producto por id    |
| POST   | `/api/productos`      | Crea un producto               |
| PUT    | `/api/productos/{id}` | Actualiza un producto          |
| DELETE | `/api/productos/{id}` | Elimina un producto            |

Parametros para crear y actualizar: `nombre`, `descripcion`, `precio`, `cantidad`.

## Como ejecutarlo

1. Importar el proyecto en Eclipse (Java EE) con Java 8 y Apache Tomcat 9.
2. Clic derecho en el proyecto, *Run As*, *Run on Server*.
3. Abrir `http://localhost:8080/StockControlAPI/` para la interfaz web.
4. La API responde en `http://localhost:8080/StockControlAPI/api/productos`.

## Nota

Los datos se guardan en memoria: al reiniciar el servidor se pierden y vuelven los productos de ejemplo.

## Autor

Steven Diaz Morales - Tecnologo en Analisis y Desarrollo de Software (SENA)