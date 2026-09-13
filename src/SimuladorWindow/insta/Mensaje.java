package SimuladorWindow.insta;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Mensaje implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TEXTO = "TEXTO";
    public static final String STICKER = "STICKER";

    private final String emisor;
    private final String receptor;
    private final LocalDateTime fechaHora;
    private final String texto;
    private final String tipo;
    private boolean leido = false;

    public Mensaje(String emisor, String receptor, String texto, String tipo) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.texto = texto;
        this.tipo = tipo;
        this.fechaHora = LocalDateTime.now();
    }

    public String getEmisor()          { return emisor; }
    public String getReceptor()        { return receptor; }
    public LocalDateTime getFechaHora(){ return fechaHora; }
    public String getTexto()           { return texto; }
    public String getTipo()            { return tipo; }
    public boolean isLeido()           { return leido; }
    public void setLeido(boolean l)    { this.leido = l; }

    public boolean esSticker()         { return STICKER.equals(tipo); }
}
