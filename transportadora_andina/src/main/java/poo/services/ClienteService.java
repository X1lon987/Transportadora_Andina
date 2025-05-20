package poo.services;

import java.util.ArrayList;
import java.util.List; //List viene implementada en java 

import org.json.JSONArray;
import org.json.JSONObject;

import poo.model.Cliente;
import poo.helpers.Utils;

import java.util.NoSuchElementException;
import org.json.JSONException;
import java.io.IOException; //Tenemos siempre que importar las excepciones que vayamos a utilizar en caso tal de que no pertenczcan a las excepciones normales de java 

public class ClienteService implements Service<Cliente> {

    private List<Cliente> list;
    private final String fileName;  //Este es el nombre del archivo donde vamos a guardar la lista de Clientes

    public ClienteService()throws Exception{
        fileName = Utils.PATH + "Cliente.json";

        if(Utils.fileExists(fileName)){ // En la clase Utils hay un metodo que me permite verificar si hay un archivo existente con ese mismo nombre fileExist(), al cual le mandamos los fileName
            load();         //Esto nos dice que si ya hay un archivo existente con este nombre de archivo, lo vamos a cargar o sobre Escribir
        }
        else{//Si el archivo no existe
            list = new ArrayList<>();//???preguntar
        }
    }

    @Override
    public JSONObject add(String strJson) throws Exception {
        Cliente c = dataToAddOk(strJson);
        
        if (list.add(c)) {
            Utils.writeJSON(list, fileName);
        }
        return new JSONObject().put("message", "ok").put("data", c.toJSONObject());
        
    }
//DataToAddOk--------------------------------------------------------------------- se implementaron colores para darle mas estetica dentro de la consola, por si nota algo raro al ejecutarlo y verlo en la web o en el REST Client.
//lo que aparece raro en el REST Client son los codigos de color utilizados. 
    public Cliente dataToAddOk(String strJson){
        JSONObject json = new JSONObject(strJson);

        if (!json.has("id") || json.getString("id").length() > 11) {
            json.put("id", Utils.getRandomKey(5));
        }
        Utils.stringOk("id", 5, json);
        Utils.stringOk("nombre", 1, json);
        Utils.stringOk("direccion", 10, json);
        Utils.stringOk("telefono", 10, json);
        Cliente c = new Cliente(json);
        if (list.contains(c)) {
            throw new ArrayStoreException(String.format("El cliente %s - %s ya existe", c.getId(), c.getNombre()));
        }
        return c;
    }
//--------------------------------------------------------
    @Override
    public JSONObject get(int index) { //Este me va a buscar un cliente mediante su indice
        return list.get(index).toJSONObject(); //Los arraylist tiene indices que mediante el metodo get()me permite buscar por eol indice que necesite, posteriormente lo convertimos en JSONObject
    }

    @Override
    public JSONObject get(String id) throws Exception { //Este me va a buscar un cliente mediante su id
        Cliente c = getItem(id);
        if(c == null){
            throw new NoSuchElementException(String.format("No se encontro el cliente con id: %s", id));
        }
        return c.toJSONObject();
    }

    @Override
    public Cliente getItem(String id) throws Exception { //Va a buscar un cliente por este id y va a recuperer sus datos y meterlo dentro de un Objeto java
        int i = list.indexOf(new Cliente(id));
        return i > -1 ? list.get(i):null;
    }

    @Override
    public JSONObject getAll() {
      try {
        JSONArray data = new JSONArray(Utils.readText(fileName));
        return new JSONObject().put("message", "ok").put("data", data);
        } catch (IOException | JSONException e) {
            Utils.printStackTrace(e);
            return Utils.keyValueToJson("message", "Sin acceso a datos de clientes", "error", e.getMessage());
        }
    }

    @Override
    public final List<Cliente> load() throws Exception { //Este metodo load nos permite cargar tantos clientes necesitemos, a pesar de que sea dentro del mismo archivo con el mismo nombre 
		list = new ArrayList<>();  //estamos referenciando un Arraylist mediante list
        JSONArray jsonArr = new JSONArray(Utils.readText(fileName));//Estamos guardando en un JSONArray referenciadio por jsonArr un nuevo JSONArray que lee todo lo que hay en fileName y mediante readText lo convierte en String
        for (int i = 0; i < jsonArr.length(); i++) { // vamos a iterar el JSONArray, donde por cada iteracion lee los datos que hay dentro de este JSONArray y los mete dentro de un JSONObject, para posteriormente meterlo dentro de list(Arraylist)
            JSONObject jsonObj =  jsonArr.getJSONObject(i);
            list.add(new Cliente(jsonObj));
        }  
      return list;
    }

    @Override
    public JSONObject remove(String id) throws Exception {
        JSONObject busquedaId = get(id);

        if(busquedaId == null){
            throw new NoSuchElementException(String.format("No se encontro el cliente con id : %s para su eliminacion ", busquedaId));
        }
        Cliente c = new Cliente(busquedaId);
        exists(c.toJSONObject());
        list.remove(c);
        Utils.writeJSON(list, fileName);
        return new JSONObject().put("message", "ok").put("data",busquedaId);//list o c.toJSONObject?
    }
    private void exists(JSONObject cliente)throws Exception{
        String id = cliente.getString("id");
        //Va a buscar si en mercancia existe un cliente con esos datos referenciados en "cliente" dentro de mercancia
        if(Utils.exists(Utils.PATH+ "Mercancia", "cliente", cliente)){
            throw new Exception(String.format("No Eliminado, el cliente %s tiene mercancia registrada!",  id));
        }
        exists("Paquete", cliente);
        exists("Caja", cliente);
        exists("Bulto", cliente);
        exists("Sobre", cliente);
    }
    private void exists(String fileName, JSONObject json)throws Exception{//Forma de polimorfismo tenemos el mismo metodo que puede tener dos formas pero llamar a ambas mediante una 
        
        if(Utils.exists(fileName, "remitente", json)||Utils.exists(fileName, "destinatario", json)){
            throw new IllegalArgumentException(String.format("NO Eliminado!, El cliente %s tiene envios registrados", json.getString("id")));
        }
    }

    @Override
    public Class<Cliente> getDataType() {//Devuelve el tipo de clase cliente
        return Cliente.class;
    }
    
    @Override
    public JSONObject update(String id, String strJson) throws Exception { //Este me va a recibir un id pra buscar el cliente y posteriormente recibe los datos nuevos que se van a asignar 
      JSONObject json = new JSONObject(strJson); //vamos a meter dentro de este JSON LOS DATOS O VALORES DEL ORIGINAL 
      // buscar el cliente que se debe actualizar
        Cliente cliente = getItem(id);
      	int i = list.indexOf(cliente);
      	if (cliente == null) { //Si el cliente esta vacio retorna que no se encontro 
            throw new NullPointerException("No se encontró el cliente " + id);
        }
    // devolver el cliente con los cambios realizados
        cliente = getUpdated(json, cliente);
        list.set(i, cliente);//Aqui vamos a modificar nuestra lista en la el indice que pertenece cliente y le vamos a aañadir los nuevos datos que tiene cliente despues de llamar al getUpdate()
        Utils.writeJSON(list, fileName); //Sobre escribimos la lista 
        return new JSONObject().put("message", "ok").put("data", cliente.toJSONObject());
    }

    @Override
    public Cliente getUpdated(JSONObject newData, Cliente current) throws Exception {
        JSONObject update = current.toJSONObject();

        if(newData.has("nombre")){
            update.put("nombre",Utils.stringOk("nombre", 1, newData));
        }
        if(newData.has("id")){
            update.put("id", Utils.stringOk("id", 5, newData));
        }
        if(newData.has("telefono")){
            update.put("telefono", Utils.stringOk("telefono", 10, newData));
        }
        if(newData.has("direccion")){
            update.put("direccion", Utils.stringOk("direccion", 10, newData));
        }
        if(newData.has("ciudad")){
            update.put("ciudad", Utils.stringOk("ciudad", 4, newData));
        }
        Cliente c = new Cliente(update);
        return c;
    }

    @Override
    public JSONObject size() {
        JSONObject tam = new JSONObject();
        tam.put("size", list.size());
        return tam;
    }
}
//ANOTACIONES
// para agregar claves a un objeto JSON lo hacemos siempre con .put("Clave" :  "Valor")