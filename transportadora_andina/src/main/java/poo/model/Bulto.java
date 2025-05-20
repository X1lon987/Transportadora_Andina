package poo.model;

import java.util.ArrayList;

import org.json.JSONObject;

public class Bulto extends Envio {

    public Bulto(){
        super();
    }
    public Bulto(String nroGuia,String contenido, boolean fragil, double valorEstimado, double peso,  ArrayList<Estado> historial, Cliente remitente, Cliente destinatario) {
        super( nroGuia,contenido, fragil, valorEstimado, peso, historial, remitente, destinatario);
    }
    public Bulto(Bulto e){
        super(e);
    }
    public Bulto(String nroGuia){
        super(nroGuia);
    }
    public Bulto(String contenido, boolean fragil, double valorEstimado, double peso, ArrayList <Estado> h, Cliente remitente, Cliente destinatario){
        super(contenido, fragil, valorEstimado, peso, h, remitente, destinatario);
    }

    public Bulto(JSONObject json){
        super(json); //Constructor que recibe un JSONObject
    }
    //calcular costo
    @Override
    public double getCosto() {
        double costo = 1000 * peso;
        return costo;
    }
    //formatos
    @Override
    public String toJSON() {
        return new JSONObject(this).toString();
    }
    @Override
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    }
    @Override
    public String toString() {
        return String.format("|--------------------------------------------------------------------|\n|%s  |%s",getClass().getSimpleName(), super.toString());
    }
}
