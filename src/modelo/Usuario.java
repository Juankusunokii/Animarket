package modelo;

public class Usuario {

    private String nombreUsuario;
    private String contrasena;
    private String rol;

    public Usuario(String nombreUsuario, String contrasena, String rol) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena    = contrasena;
        this.rol           = rol;
    }

    /** Verifica si la contraseña ingresada coincide con la almacenada. */
    public boolean autenticar(String passwordIngresado) {
        return this.contrasena.equals(passwordIngresado);
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String n) { this.nombreUsuario = n; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String c) { this.contrasena = c; }

    public String getRol() { return rol; }
    public void setRol(String r) { this.rol = r; }
}