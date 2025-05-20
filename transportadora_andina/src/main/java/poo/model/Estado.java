package poo.model;

import java.time.LocalDateTime;
import org.json.JSONObject;

public class Estado  {
    private LocalDateTime fechayHora;
    private EstadoEnvio estado;

    public Estado(){
        super();        
    }
    public Estado(LocalDateTime fechayHora, EstadoEnvio estado){
        setFechayHora(fechayHora);
        setEstado(estado);
    }
    public Estado(Estado h){
        this(h.fechayHora, h.estado);
    }                                       //Valueof es para que nos devuelva de Estado un String y asi asignarlela clave a json
    public Estado(JSONObject json){//LocalDateTime.parse es para que nos suelte un String que json pueda usar como clave 
        this(LocalDateTime.parse(json.getString("fechayHora")), json.getEnum(EstadoEnvio.class, "estado"));
    }
    public LocalDateTime getFechayHora(){
        return fechayHora;
    }
    public final void setFechayHora(LocalDateTime fechaHora){
        this.fechayHora = fechaHora;
    }
    public EstadoEnvio getEstado(){
        return estado;
    }
    public final void setEstado(EstadoEnvio estado){
        this.estado = estado;
    }
    public String getId(){
        String m = String.valueOf(super.hashCode());
        return m;
    }
    @Override 
    public String toString() {
        return getEstado().name();
    }
    @Override
    public int hashCode() {
        return super.hashCode(); //Sirve para darl un identificador diferente a cada copia del una instancia
    }
}
