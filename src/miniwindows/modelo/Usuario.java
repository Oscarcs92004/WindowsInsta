package miniwindows.modelo;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Un usuario del sistema. Es una clase de datos: solo guarda informacion,
 * no tiene logica. Implementa Serializable para poder guardarse en un
 * archivo binario de un solo golpe.
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombreCompleto;
    private char genero;                 // 'M' o 'F'
    private final String username;       // unico en todo el sistema, no cambia
    private String password;
    private final LocalDate fechaRegistro;
    private int edad;
    private boolean activa = true;
    private Rol rol = Rol.ESTANDAR;
    private String fotoPerfil;           // ruta a la imagen, puede ser null

    public Usuario(String nombreCompleto, char genero, String username,
                   String password, int edad) {
        this.nombreCompleto = nombreCompleto;
        this.genero = genero;
        this.username = username;
        this.password = password;
        this.edad = edad;
        this.fechaRegistro = LocalDate.now();
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String n) {
        this.nombreCompleto = n;
    }

    public char getGenero() {
        return genero;
    }

    public void setGenero(char g) {
        this.genero = g;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String p) {
        this.password = p;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int e) {
        this.edad = e;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean a) {
        this.activa = a;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol r) {
        this.rol = r;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String ruta) {
        this.fotoPerfil = ruta;
    }

    @Override
    public String toString() {
        return username + " (" + nombreCompleto + ")";
    }
}
