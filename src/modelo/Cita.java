package modelo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Cita {

    // ─── Constantes de estado ────────────────────────────────────────────────
    public static final String ESTADO_PENDIENTE  = "Pendiente";
    public static final String ESTADO_ACTIVA     = "Activa";
    public static final String ESTADO_FINALIZADA = "Finalizada";
    public static final String ESTADO_CANCELADA  = "Cancelada";

    // ─── Servicios base disponibles ──────────────────────────────────────────
    public static final String SERVICIO_BANIO              = "Baño";
    public static final String SERVICIO_BANIO_CORTE        = "Baño y corte";
    public static final String SERVICIO_ESTETICA_COMPLETA  = "Estética completa";

    // ─── Adicionales disponibles ─────────────────────────────────────────────
    public static final String ADICIONAL_ANTIPULGAS    = "Antipulgas";
    public static final String ADICIONAL_DESPARASITANTE = "Desparasitante";
    public static final String ADICIONAL_CORTE_UNAS    = "Corte de uñas";
    public static final String ADICIONAL_CORTE_SANITARIO = "Corte sanitario";

    // ─── Atributos ───────────────────────────────────────────────────────────
    private int         id;
    private LocalDate   fecha;
    private LocalTime   hora;
    private int         duracion;        // en minutos
    private String      estado;

    // Datos mascota / dueño
    private String      nombreMascota;
    private String      raza;
    private double      peso;
    private String      nombreDueno;
    private String      telefono;
    private String      observacionesPrevias;

    // Servicios
    private String      servicioSolicitado;
    private List<String> adicionalesSolicitados;

    // Post-servicio
    private String      observacionesPosteriores;
    private boolean     servicioConfirmado;

    // Pago
    private boolean     pagoRealizado;
    private String      momentoPago;          // "Antes" | "Después"
    private String      nombreTransferencia;
    private String      codigoComprobante;

    // ─── Constructor completo ────────────────────────────────────────────────
    public Cita(int id, LocalDate fecha, LocalTime hora, String nombreMascota,
                String raza, double peso, String nombreDueno, String telefono,
                String observacionesPrevias, String servicioSolicitado,
                List<String> adicionalesSolicitados) {
        this.id                    = id;
        this.fecha                 = fecha;
        this.hora                  = hora;
        this.nombreMascota         = nombreMascota;
        this.raza                  = raza;
        this.peso                  = peso;
        this.nombreDueno           = nombreDueno;
        this.telefono              = telefono;
        this.observacionesPrevias  = observacionesPrevias;
        this.servicioSolicitado    = servicioSolicitado;
        this.adicionalesSolicitados = (adicionalesSolicitados != null)
                                     ? new ArrayList<>(adicionalesSolicitados)
                                     : new ArrayList<>();
        this.estado                = ESTADO_PENDIENTE;
        this.duracion              = calcularDuracion();
    }

    // ─── Constructor mínimo (usado al reconstruir desde archivo) ────────────
    public Cita() {
        this.adicionalesSolicitados = new ArrayList<>();
        this.estado = ESTADO_PENDIENTE;
    }

    // ─── Lógica de negocio: duración por peso ────────────────────────────────
    /**
     * Calcula la duración estimada en minutos según el peso del perro.
     * Rangos basados en la tabla de precios de Animarket.
     */
    public int calcularDuracion() {
        int base;
        if      (peso <= 8)  base = 60;
        else if (peso <= 20) base = 75;
        else if (peso <= 30) base = 90;
        else if (peso <= 40) base = 105;
        else                 base = 120;

        // Adicionales suman tiempo
        int extra = 0;
        if (adicionalesSolicitados != null) {
            if (adicionalesSolicitados.contains(ADICIONAL_ANTIPULGAS))     extra += 15;
            if (adicionalesSolicitados.contains(ADICIONAL_DESPARASITANTE)) extra += 10;
            if (adicionalesSolicitados.contains(ADICIONAL_CORTE_UNAS))     extra += 10;
            if (adicionalesSolicitados.contains(ADICIONAL_CORTE_SANITARIO)) extra += 15;
        }

        // Corte añade tiempo base
        if (SERVICIO_BANIO_CORTE.equals(servicioSolicitado))       extra += 30;
        if (SERVICIO_ESTETICA_COMPLETA.equals(servicioSolicitado)) extra += 45;

        this.duracion = base + extra;
        return this.duracion;
    }

    // ─── Precio por peso ─────────────────────────────────────────────────────
    /**
     * Calcula el precio total en COP según la tabla de precios 2026.
     */
    public long calcularPrecio() {
        long base;
        if      (peso <= 8)  base = 50_000;
        else if (peso <= 20) base = 65_000;
        else if (peso <= 30) base = 70_000;
        else if (peso <= 35) base = 75_000;
        else if (peso <= 40) base = 80_000;
        else                 base = 95_000;

        long extra = 0;
        if (adicionalesSolicitados != null) {
            if (adicionalesSolicitados.contains(ADICIONAL_ANTIPULGAS))      extra += 12_000;
            if (adicionalesSolicitados.contains(ADICIONAL_DESPARASITANTE))  extra += 11_000;
            if (adicionalesSolicitados.contains(ADICIONAL_CORTE_UNAS))      extra += 10_000;
            if (adicionalesSolicitados.contains(ADICIONAL_CORTE_SANITARIO)) extra += 15_000;
        }

        if (SERVICIO_BANIO_CORTE.equals(servicioSolicitado))       extra += 20_000;
        if (SERVICIO_ESTETICA_COMPLETA.equals(servicioSolicitado)) extra += 35_000;

        return base + extra;
    }

    // ─── Hora fin ────────────────────────────────────────────────────────────
    public LocalTime getHoraFin() {
        return hora.plusMinutes(duracion);
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────
    public int getId()                            { return id; }
    public void setId(int id)                     { this.id = id; }

    public LocalDate getFecha()                   { return fecha; }
    public void setFecha(LocalDate fecha)         { this.fecha = fecha; }

    public LocalTime getHora()                    { return hora; }
    public void setHora(LocalTime hora)           { this.hora = hora; }

    public int getDuracion()                      { return duracion; }
    public void setDuracion(int duracion)         { this.duracion = duracion; }

    public String getEstado()                     { return estado; }
    public void setEstado(String estado)          { this.estado = estado; }

    public String getNombreMascota()              { return nombreMascota; }
    public void setNombreMascota(String n)        { this.nombreMascota = n; }

    public String getRaza()                       { return raza; }
    public void setRaza(String raza)              { this.raza = raza; }

    public double getPeso()                       { return peso; }
    public void setPeso(double peso)              { this.peso = peso; }

    public String getNombreDueno()                { return nombreDueno; }
    public void setNombreDueno(String n)          { this.nombreDueno = n; }

    public String getTelefono()                   { return telefono; }
    public void setTelefono(String t)             { this.telefono = t; }

    public String getObservacionesPrevias()        { return observacionesPrevias; }
    public void setObservacionesPrevias(String o)  { this.observacionesPrevias = o; }

    public String getServicioSolicitado()          { return servicioSolicitado; }
    public void setServicioSolicitado(String s)    { this.servicioSolicitado = s; }

    public List<String> getAdicionalesSolicitados() { return adicionalesSolicitados; }
    public void setAdicionalesSolicitados(List<String> a) {
        this.adicionalesSolicitados = (a != null) ? new ArrayList<>(a) : new ArrayList<>();
    }

    public String getObservacionesPosteriores()       { return observacionesPosteriores; }
    public void setObservacionesPosteriores(String o) { this.observacionesPosteriores = o; }

    public boolean isServicioConfirmado()             { return servicioConfirmado; }
    public void setServicioConfirmado(boolean b)      { this.servicioConfirmado = b; }

    public boolean isPagoRealizado()                  { return pagoRealizado; }
    public void setPagoRealizado(boolean b)           { this.pagoRealizado = b; }

    public String getMomentoPago()                    { return momentoPago; }
    public void setMomentoPago(String m)              { this.momentoPago = m; }

    public String getNombreTransferencia()             { return nombreTransferencia; }
    public void setNombreTransferencia(String n)       { this.nombreTransferencia = n; }

    public String getCodigoComprobante()               { return codigoComprobante; }
    public void setCodigoComprobante(String c)         { this.codigoComprobante = c; }

    @Override
    public String toString() {
        return String.format("Cita[%d] %s %s — %s (%.1f kg) — %s",
                id, fecha, hora, nombreMascota, peso, estado);
    }
}