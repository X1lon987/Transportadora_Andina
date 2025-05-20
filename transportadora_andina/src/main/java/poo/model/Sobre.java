package poo.model;

import java.util.ArrayList;

import org.json.JSONObject;

public class Sobre extends Envio {
    private boolean certificado;

    public Sobre(){
        super();
        setValorEstimado(0);
        setCertificado(false);
    }
    public Sobre(String nroGuia, String contenido, boolean fragil, double valorEstimado, double peso, ArrayList<Estado> estados, Cliente remitente, Cliente destinatario, boolean certificado) {
        super(nroGuia,contenido, false, 0.0,0.0,  estados, remitente, destinatario);
        this.setCertificado(certificado);
    }
    public Sobre(Sobre s){
        this(s.nroGuia, s.contenido, s.fragil, s.valorEstimado, s.peso,  s.estados, s.remitente, s.destinatario, s.certificado);
    }
    public Sobre(String contenido, boolean fragil, double valorEstimado, double peso, ArrayList <Estado> estados, Cliente remitente, Cliente destinatario, boolean certificado){
        super(contenido, fragil, valorEstimado, peso, estados, remitente, destinatario);
        setCertificado(certificado);
    }
    public Sobre(JSONObject json){
        super(json);
        setCertificado(json.getBoolean("certificado"));
    }
    public Sobre(String nroGuia){
        super(nroGuia);
    }
//ACCESORES Y MUTADORES
    public boolean getCertificado(){
        return certificado;
    }
    public final void setCertificado(boolean certificado){
        this.certificado = certificado;
    }

    @Override
    public double getCosto() {
        int aux = (1300000/100) * 2;
        double costo = Double.valueOf(aux);

        if(certificado == true){
            costo *= 1.10;
        }
        return costo; 
    }  
    @Override
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    }
    @Override
    public String toJSON() {
        return new JSONObject(this).toString();
    }
    @Override
    public String toString() {
        return String.format("|--------------------------------------------------------------------|\n|%s  |%s",getClass().getSimpleName(), super.toString());
    }
}
