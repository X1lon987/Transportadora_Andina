package poo.model;

import java.util.ArrayList;

import org.json.JSONObject;

public class Caja extends Envio {
     
    private double alto;
    private double ancho;
    private double largo;

    public Caja(){
        super();
        setAlto(0.0);
        setAncho(0.0);
        setLargo(0.0);
    }
    public Caja(String nroGuia, String contenido, boolean fragil, double valorEstimado, double peso, ArrayList<Estado> estados, Cliente remitente, Cliente destinatario, double alto, double ancho, double largo) {
        super(nroGuia,contenido, false, 0.0,0.0,  estados, remitente, destinatario);
        this.setAlto(alto);
        this.setAncho(ancho);
        this.setLargo(largo);
    }
    public Caja(Caja c){
        this(c.nroGuia, c.contenido, c.fragil, c.valorEstimado, c.peso,  c.estados, c.remitente, c.destinatario, c.alto, c.ancho, c.largo);
    }
    public Caja(String contenido, boolean fragil, double valorEstimado, double peso, ArrayList <Estado> h, Cliente remitente, Cliente destinatario, double alto, double ancho, double largo){
        super(contenido, fragil, valorEstimado, peso, h, remitente, destinatario);
        setAlto(alto);
        setAncho(ancho);
        setLargo(largo);
    }
    public Caja(JSONObject json){
        super(json);
        setAlto(json.getDouble("alto"));
        setAncho(json.getDouble("ancho"));
        setLargo(json.getDouble("largo"));
    }
    public Caja(String nroGuia){
        super(nroGuia);
    }
    //Acesores
    public double getAlto(){
        return alto;
    }
    public final void setAlto(double alto){
        this.alto = alto;
    }
    public double getAncho(){
        return ancho;
    }
    public final void setAncho(double ancho){
        this.ancho = ancho;
    }
    public double getLargo(){
        return largo;
    }
    public final void setLargo(double largo){
        this.largo = largo; 
    }
    public double getVolumen(){
        return alto* ancho * largo;
    }
    //calcular costo
    @Override
    public double getCosto() {
        double costo = 0;
        if(getVolumen() <= 0.5){
            costo = 10000 + (500 * peso);
        }
        else if(getVolumen() <=1.0){
            costo = 12000 + (500 * peso);
        }   
        else if(getVolumen() <= 3.0){
            costo = 15000 + (500 * peso);
        }   
        else if(getVolumen() <= 6.0){
            costo = 25000 + (500 * peso);
        }   
        else if(getVolumen() <= 10.0 ){
           costo  = 30000 + (500 * peso);
        }
        else if (getVolumen() > 10.0){
            costo = 10000 * (getVolumen() / 10) + (500 * peso);
        }
        return costo;
    }
    @Override
    public String toJSON() {
        return new JSONObject(this).toString();//Va a devolver el objeto JSON en formato String 
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
