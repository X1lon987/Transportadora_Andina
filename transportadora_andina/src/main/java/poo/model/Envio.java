package poo.model;

import java.util.ArrayList;
import poo.helpers.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Envio implements Costeable, Exportable {
     
    protected String contenido; 
    protected boolean fragil;
    protected double valorEstimado;
    protected double peso;
    protected String nroGuia;
    protected ArrayList<Estado> estados ; 
    protected Cliente remitente;
    protected Cliente destinatario;

//Constructores
    public Envio(){//por defecto
        this("","", false, 0,0,  new ArrayList<>(), new Cliente(),new Cliente());
    }
    public Envio(String nroGuia,String contenido, boolean fragil, double valorEstimado, double peso, ArrayList<Estado> estados, Cliente remitente, Cliente destinatario){
        this.setNroGuia(nroGuia);
        this.setContenido(contenido);
        this.setFragil(fragil);
        this.setValorEstimado(valorEstimado);   //parametrizado
        this.setPeso(peso);
        this.setEstados(estados);
        this.setRemitente(remitente);
        this.setDestinatario(destinatario);
    }
    public Envio(Envio e){//Constructor copia
        this(e.nroGuia,e.contenido, e.fragil,e.valorEstimado, e.peso,  e.estados, e.remitente, e.destinatario);
    }
    public Envio(String nroGuia){//Constructor que solo deja ingresar el dato de nroGuia
        this();
        setNroGuia(nroGuia);
    }
    public Envio( String contenido, boolean fragil, double valorEstimado, double peso, ArrayList <Estado> estados, Cliente remitente, Cliente destinatario){  // Constructor con dato random(nroGuia)
        this(Utils.getRandomKey(8),contenido, fragil, valorEstimado,peso,  estados, remitente, destinatario);
    }
    public Envio(JSONObject json){ // va recibir un objeto tipo JSON para hallar cada uno de los parametros osea etsamos volviendo un objeto json en un objeto java
        this(json.getString("nroGuia"),json.getString("contenido"), json.getBoolean("fragil"), json.getDouble("valorEstimado"), json.getDouble("peso"),  new ArrayList<>(), new Cliente(json.getJSONObject("remitente")), new Cliente(json.getJSONObject("destinatario")));  
        JSONArray estados = json.getJSONArray("estados");
        for (int i = 0; i < estados.length();i++){
            this.estados.add(new Estado(estados.getJSONObject(i)));  
        }             
    }

//ACCESORES Y MUTADORES 
    public String getId(){
        return getNroGuia();
    }
    public String getTipo(){
        return this.getClass().getSimpleName();  
    }
    public String getContenido(){
        return contenido;
    }
    public final void setContenido(String contenido){
        this.contenido = contenido;
    }
    public boolean getFragil(){
        return fragil;
    }
    public final void setFragil(boolean fragil){
        this.fragil = fragil;
    }
    public double getValorEstimado(){
        return valorEstimado;
    }
    public final void setValorEstimado(double valorContenido){
        this.valorEstimado = valorContenido;
    }
    public double getPeso(){
        return peso;
    }
    public final void setPeso(double peso){
        this.peso = peso;
    }
    public String getNroGuia(){
        return nroGuia;
    }
    public final void setNroGuia(String nGuia){
        this.nroGuia = nGuia;
    }
    public ArrayList<Estado> getEstados(){
        return estados;
    }
    public final void setEstados(ArrayList<Estado> estados){
        this.estados = estados;
    }
    public Cliente getRemitente(){
        return remitente;
    }
    public final void setRemitente(Cliente remitente){
        this.remitente = remitente;
    }
    public Cliente getDestinatario(){
        return destinatario;
    }
    public final void setDestinatario(Cliente destinatario){
        this.destinatario = destinatario;
    }
    public void okRestriccions(){
        for (Estado i : estados) {
            if(i.getEstado() == EstadoEnvio.RECIBIDO){
               //por implementar para validar en el frontend 
            }
        }
    }
   
//Anterior toString()
    // @Override
    // public String toString(){ //en las clases se pueden devolver metodos get que no correspondan a atributos. como lo vemos en getprecio.no tengo ningun atributo de precio
    //     return (new JSONObject(this)).toString(2); /*JSONobjet no va a devolver los atributos, sino los metodos get */

    // }
    @Override
    public String toString(){ //formato por columnas
        String isFragil = getFragil()?"si":"no";
        return String.format("\t   Parametros         |\t\tValores              |\n|--------------------------------------------------------------------|\n|----------|Contenido:\t\t      |%-20.20s          |\n|----------|Fraji?: \t\t      |%-3s                           |\n|----------|Valor Estimado: \t      |%-10.10f                  |\n|----------|Peso: \t\t      |%-10.10f                  |\n|----------|NroGuia: \t\t      |%-7.7s                       |\n|--------------------------------------------------------------------|\n|----------|estados Envio:                                         |%s\n|--------------------------------------------------------------------|\n|----------|Remitente:                                               |%s|\n|--------------------------------------------------------------------|\n|----------|Destinatario:                                            | %s|\n|--------------------------------------------------------------------|", getContenido(), isFragil, getValorEstimado(),getPeso(), getNroGuia(), estados.toString(), remitente.toString(), destinatario.toString());
         
    }
    @Override
    public boolean equals(Object object) { // La clase objeto que pertenece internamente a java tiene integrado en si un metodo equals()
        if(this == object){
            return true;
        }
        if(object == null){
            return false;
        }if(this.getClass() != object.getClass()){
            return false;
        }
        return this.nroGuia.equals(((Envio)object).nroGuia); 
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
