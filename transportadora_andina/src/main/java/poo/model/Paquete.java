package poo.model;

import java.util.ArrayList;

import org.json.JSONObject;

public class Paquete extends Envio { //Clase hija de Envio
    public Paquete(){
        super();
    }
    public Paquete(String nroGuia,String contenido, boolean fragil, double valorEstimado, double peso,  ArrayList<Estado> estados, Cliente remitente, Cliente destinatario) {
        super( nroGuia,contenido, fragil, valorEstimado, peso, estados, remitente, destinatario);
    }
    public Paquete(Paquete e){
        super(e);
    }
    public Paquete(String nroGuia){
        super(nroGuia);
    }
    public Paquete(String contenido, boolean fragil, double valorEstimado, double peso, ArrayList <Estado> estados, Cliente remitente, Cliente destinatario){
        super(contenido, fragil, valorEstimado, peso, estados, remitente, destinatario);
    }

    public Paquete(JSONObject json){
        super(json); //Constructor que recibe un JSONObject
    }

//Implementacion costo
    @Override
    public double getCosto() {
        double totalEnvio = 1000*(peso/10);
        return totalEnvio;
    }
//Modificadores esteticos
    @Override
    public String toString() {
        return String.format("|--------------------------------------------------------------------|\n|%s  |%s",getClass().getSimpleName(), super.toString());
    }
    @Override
    public String toJSON() {
        return new JSONObject(this).toString();
    }
    @Override
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    }   
}
