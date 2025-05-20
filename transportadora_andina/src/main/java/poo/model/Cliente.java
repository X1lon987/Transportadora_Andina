package poo.model;
import org.json.JSONObject;

import poo.helpers.Utils;

public class Cliente implements Exportable  {
    private String id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String ciudad;

    public Cliente(){
        this(null, "nn","00-00","+57","MM");
    }
    public Cliente(String id,String nombre, String direccion, String telefono, String ciudad){
        setId(id);
        setNombre(nombre);
        setDireccion(direccion);
        setTelefono(telefono);
        setCiudad(ciudad);
    }

//Constructores solicitados

   //CONSTRUCTOR COPIA   
    public Cliente(Cliente c){//Constructor copia, consiste en crear una instancia con la informacion de otro constructor por defecto, OSEA QUE RECIBE UNA INSTANCIA DE SU MISMO TIPO
        this(c.id, c.nombre, c.direccion,c.telefono, c.ciudad); // lo usamos para crear un clon o copia de la instancia del mismo tipo, ambos son espacios distintos de memoria, por lo que al compararlos siempre nos va a salir que son diferentes
    }

    public Cliente(String id){
        this();                     //Aqui estamos creando un constructor que solo recibe id, utilizando el constructor por defecto y rellenando solo id, NADA MAS EL ID    
        setId(id);                      
    }
    public Cliente( String nombre, String direccion, String telefono, String ciudad){ // constructor con dato aleatorio usando .utils en el dato que necesitemos, este constructor nos va a mostrar toda la informacion que dpodemos manipular menos el dato que estamos estableciendo en aleatorio o bajo cuerda que es id
        this(Utils.getRandomKey(5), nombre , direccion, telefono, ciudad );
    }

    public Cliente(JSONObject json){ // va recibir un objeto tipo JSON para hallar cada uno de los parametros, osea estamos volviendo un objeto json en un objeto java
        this(json.getString("id"), json.getString("nombre"), json.getString("direccion"), 
        json.getString("telefono"), json.getString("ciudad"));                
    }

//Accesores y Mutadores
    public String getId(){
        return id;
    }
    public final void setId(String identificacion){
        this.id = identificacion;
    }
    public String getNombre(){
        return nombre;
    }
    public final void setNombre(String nombre){
        this.nombre = nombre;
    }
    public String getDireccion(){
        return direccion;
    } 
    public final void setDireccion(String direccion){
        this.direccion = direccion;
    }
    public String getTelefono(){
        return telefono;
    }
    public final void setTelefono(String num){
        this.telefono = num;
    }
    public String getCiudad(){
        return ciudad;
    }
    public final void setCiudad(String ciudad){
        this.ciudad = ciudad;
    }
    @Override
    public String toString(){ //en las clases se pueden devolver metodos get que no correspondan a atributos. como lo vemos en getprecio.no tengo ningun atributo de precio 
        String infColumnas = String.format("\n|"+"----------"+"|Datos Cliente:                                           |\n|--------------------------------------------------------------------|\n|----------------------|  Parametros  |\t\tValores              |\n|----------------------|---------------------------------------------|\n|----------------------|id: \t      |%-7.7s                       |\n|----------------------|Nombre:       |%-10.10s                    |\n|----------------------|Direccion:    |%-9.9s                     |\n|----------------------|Telefono:     |%-10.10s                    |\n|----------------------|Ciudad:       |%-10.10s                    |\n"+"|------------------------------------------", 
        getId(),getNombre(), getDireccion(), getTelefono(),getCiudad());
        return infColumnas;
    }
    @Override
    public int hashCode() {
        return super.hashCode(); //Sirve para darl un identificador diferente a cada copia del una instancia
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
        return this.id.equals(((Cliente)object).id);  
    }
    @Override
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    }
}
