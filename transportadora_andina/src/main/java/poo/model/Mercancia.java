package poo.model;

import java.time.LocalDateTime;
import org.json.JSONObject;

import poo.helpers.Utils;

import java.time.Period;

public class Mercancia implements Costeable, Exportable{
    private String id;
    private String bodega;
    private String contenido;
    private double ancho;
    private double largo;
    private double alto;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private Cliente cliente;
    
    public Mercancia(){
        this("", "", "", 0,0,0, null, null, new Cliente());
    }
    public Mercancia(String id,String bodega, String contenido, double alto,double largo,double ancho, LocalDateTime fechaEntrada, LocalDateTime fechaSalida, Cliente cliente){
        this.setId(id);
        this.setBodega(bodega);
        this.setContenido(contenido);
        this.setAncho(ancho);
        this.setLargo(largo);
        this.setalto(alto);
        this.setFechaEntrada(fechaEntrada);
        this.setFechaSalida(fechaSalida);
        this.setCliente(cliente);
    }
//Constructor copia
    public Mercancia(Mercancia a){
        this(a.id,a.bodega, a.contenido, a.alto, a.ancho,a.largo,a.fechaEntrada, a.fechaSalida, a.cliente);
    }
//Constructor solo nombre id
    public Mercancia(String id){
        this();
        setId(id);
    }
//Constructor con idid Aleatorio
    public Mercancia(String bodega,String contenido, double alto, double ancho,double largo,  LocalDateTime fechaEntrada, LocalDateTime fechaSalida, Cliente cliente){
        this(Utils.getRandomKey(6),bodega,contenido,ancho,largo,alto,fechaEntrada,fechaSalida, cliente);
    }
//Constructor que recibe un Objeto JSON
    public Mercancia(JSONObject json){
        this(json.getString("id"),json.getString("bodega"), json.getString("contenido"), json.getDouble("alto"), json.getDouble("ancho"),json.getDouble("largo"),
        LocalDateTime.parse(json.getString("fechaEntrada")), LocalDateTime.parse(json.getString("fechaSalida")), new Cliente(json.getJSONObject("cliente")));
    }

//Accesores y Mutadores
    public String getId(){
        return id;
    }
    public final void setId(String nombreid){
        this.id = nombreid;
    }
    public String getBodega(){
        return bodega;
    }
    public final void setBodega(String bodega){
        this.bodega = bodega;
    }
    public String getContenido(){
        return contenido;
    }
    public final void  setContenido(String contenido){
        this.contenido = contenido;
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
        this.largo=largo;
    }
    public double getAlto(){
        return alto;
    }
    public final void setalto(double alto){
        this.alto = alto;
    }
    public double getVolumen(){
        return ancho*largo*alto;
    }
    public LocalDateTime getFechaEntrada(){
        return fechaEntrada;
    }
    public final void setFechaEntrada(LocalDateTime fechaIngreso){
        this.fechaEntrada = fechaIngreso;
    }
    public LocalDateTime getFechaSalida(){
        return fechaSalida;
    }
    public final void setFechaSalida(LocalDateTime fechaRetiro){
        this.fechaSalida = fechaRetiro;
    }
    public Cliente getCliente(){
        return cliente;
    }
    public final void setCliente(Cliente cliente){
        this.cliente = cliente;
    }
    public String getCiudad(){
        return this.cliente.getCiudad();
    }
    
//Metodo para calcular el costo por dias
    public int getCalcularDias(){
        int diasAlmacenados = Period.between(fechaEntrada.toLocalDate(), fechaSalida.toLocalDate()).getDays();
        
        return diasAlmacenados==0?1: diasAlmacenados;
    }

//metodo de la interface costeable
    @Override
    public double getCosto() {
        double costo = getCalcularDias() * (getVolumen()*5000);
        return costo;
    }

//otros metodos de diseño del programa
    @Override
    public String toString() {
        String formaColumn = String.format("|idId: %-10s|\n|Contenido: %-9s|\n|volumen: %21-f|\n|fechaIngre: %-30s|\n|fechaSali: %-30s", getId(),getContenido(),getVolumen(),getFechaEntrada(),getFechaSalida());
        return formaColumn;
    }
    @Override
    public String toJSON() {
        return new JSONObject(this).toString();
    }
    public int hashCode() {
        return super.hashCode(); //Sirve para darl un identificador diferente a cada copia del una instancia
    }
    @Override
    public boolean equals(Object object) { // La clase objeto que pertenece internamente a java tiene integrado en si un metodo equals()
        if(this == object){ //Esto comprueba la referencia del objeto
            return true;
        }
        if(object == null){
            return false;
        }if(this.getClass() != object.getClass()){ //Esto verifica que contengan los mismos datos
            return false;
        }
        return this.id.equals(((Mercancia)object).id);    
    }
    @Override
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    } 
}
