package controlador;

import modelo.Usuario;
import persistencia.GestorArchivo;

import java.util.List;

/**
 * AuthController
 * Gestiona el inicio de sesión del recepcionista.
 * Bloquea el acceso tras 3 intentos fallidos consecutivos.
 */
public class AuthControlador {

    private static final int MAX_INTENTOS = 3;

    private final GestorArchivo gestorArchivo;
    private Usuario usuarioActivo;
    private int     intentosFallidos;

    public AuthControlador(GestorArchivo gestorArchivo) {
        this.gestorArchivo  = gestorArchivo;
        this.intentosFallidos = 0;
    }

    /**
     * Intenta autenticar al usuario.
     * @return  0 → autenticado correctamente
     *          1 → credenciales incorrectas (queda(n) intentos)
     *          2 → sistema bloqueado por demasiados intentos
     */
    public int autenticar(String usuario, String password) {
        if (intentosFallidos >= MAX_INTENTOS) return 2;

        List<Usuario> usuarios = gestorArchivo.leerUsuarios();
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equals(usuario) && u.autenticar(password)) {
                usuarioActivo    = u;
                intentosFallidos = 0;
                return 0;
            }
        }
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) return 2;
        return 1;
    }

    public void cerrarSesion() { usuarioActivo = null; }

    public boolean haySesionActiva()       { return usuarioActivo != null; }
    public Usuario getUsuarioActivo()      { return usuarioActivo; }
    public int     getIntentosFallidos()   { return intentosFallidos; }
    public int     getIntentosRestantes()  { return MAX_INTENTOS - intentosFallidos; }
    public void    resetearBloqueo()       { intentosFallidos = 0; }
}