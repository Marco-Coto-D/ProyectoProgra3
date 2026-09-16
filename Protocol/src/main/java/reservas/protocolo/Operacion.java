package reservas.protocolo;

/**
 * Catálogo de operaciones que el Frontend puede pedirle al Backend.
 * Cada valor corresponde a un método del BackendProxy / un caso del switch
 * en el manejador de peticiones del servidor.
 */
public enum Operacion {

    // Sesión
    AUTENTICAR,
    CAMBIAR_CLAVE,

    // Consultas
    LISTAR_CATEGORIAS,
    LISTAR_RECURSOS,
    LISTAR_FUNCIONARIOS,
    BUSCAR_FUNCIONARIOS,
    BUSCAR_RECURSOS,
    BUSCAR_RECURSOS_POR_CATEGORIA,
    BUSCAR_CATEGORIAS,

    // CRUD administrativo
    GUARDAR_FUNCIONARIO,
    BORRAR_FUNCIONARIO,
    GUARDAR_RECURSO,
    BORRAR_RECURSO,
    GUARDAR_CATEGORIA,
    BORRAR_CATEGORIA,

    // Reservas
    CREAR_RESERVA,
    CANCELAR_RESERVA,
    LISTAR_MIS_RESERVAS,
    LISTAR_RESERVAS_ENTRE,

    // Inteligencia artificial
    EXTRAER_IA
}
