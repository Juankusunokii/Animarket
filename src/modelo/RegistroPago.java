package modelo;

import java.time.LocalDate;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// RegistroPago: datos de la transacción de pago asociada a una cita
// ─────────────────────────────────────────────────────────────────────────────
class RegistroPago {

    private int     idCita;
    private boolean pagoRealizado;
    private String  momentoPago;          // "Antes" | "Después"
    private String  nombreTransferencia;
    private String  codigoComprobante;

    public RegistroPago(int idCita) { this.idCita = idCita; }

    public String generarComprobante(Cita cita) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== COMPROBANTE ANIMARKET ===\n");
        sb.append("Mascota : ").append(cita.getNombreMascota()).append("\n");
        sb.append("Dueño   : ").append(cita.getNombreDueno()).append("\n");
        sb.append("Fecha   : ").append(cita.getFecha()).append("\n");
        sb.append("Servicio: ").append(cita.getServicioSolicitado()).append("\n");
        if (!cita.getAdicionalesSolicitados().isEmpty())
            sb.append("Adicionales: ").append(String.join(", ", cita.getAdicionalesSolicitados())).append("\n");
        sb.append("Total   : $").append(String.format("%,d", cita.calcularPrecio())).append("\n");
        sb.append("Pago    : ").append(pagoRealizado ? "Realizado" : "Pendiente").append("\n");
        if (momentoPago != null)
            sb.append("Momento : ").append(momentoPago).append("\n");
        if (codigoComprobante != null && !codigoComprobante.isEmpty())
            sb.append("Comprobante transferencia: ").append(codigoComprobante).append("\n");
        sb.append("============================");
        return sb.toString();
    }

    public int     getIdCita()                     { return idCita; }
    public boolean isPagoRealizado()               { return pagoRealizado; }
    public void    setPagoRealizado(boolean b)     { this.pagoRealizado = b; }
    public String  getMomentoPago()                { return momentoPago; }
    public void    setMomentoPago(String m)        { this.momentoPago = m; }
    public String  getNombreTransferencia()        { return nombreTransferencia; }
    public void    setNombreTransferencia(String n){ this.nombreTransferencia = n; }
    public String  getCodigoComprobante()          { return codigoComprobante; }
    public void    setCodigoComprobante(String c)  { this.codigoComprobante = c; }
}

// ─────────────────────────────────────────────────────────────────────────────
// RegistroServicio: novedades post-servicio reportadas por el peluquero
// ─────────────────────────────────────────────────────────────────────────────
class RegistroServicio {

    private int          idCita;
    private List<String> serviciosRealizados;
    private List<String> adicionalesAplicados;
    private String       observacionesPosteriores;
    private LocalDate    fechaRegistro;

    public RegistroServicio(int idCita) {
        this.idCita        = idCita;
        this.fechaRegistro = LocalDate.now();
    }

    public int          getIdCita()                          { return idCita; }
    public List<String> getServiciosRealizados()             { return serviciosRealizados; }
    public void         setServiciosRealizados(List<String> s) { this.serviciosRealizados = s; }
    public List<String> getAdicionalesAplicados()            { return adicionalesAplicados; }
    public void         setAdicionalesAplicados(List<String> a){ this.adicionalesAplicados = a; }
    public String       getObservacionesPosteriores()         { return observacionesPosteriores; }
    public void         setObservacionesPosteriores(String o) { this.observacionesPosteriores = o; }
    public LocalDate    getFechaRegistro()                    { return fechaRegistro; }
}