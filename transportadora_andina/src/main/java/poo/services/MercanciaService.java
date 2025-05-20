package poo.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import poo.model.Cliente;
import poo.model.Mercancia;
import poo.helpers.Utils;

import java.util.NoSuchElementException;
import java.io.IOException;

public class MercanciaService implements ServiceMercancia {
    private List<Mercancia> list;
    private final String fileName;
    private final Service<Cliente> clientes;//Aqui es la inyeccion de dependencia

    public MercanciaService(Service<Cliente>clientes) throws Exception{
        this.clientes = clientes;
        fileName = Utils.PATH + "Mercancia.json";

        if(Utils.fileExists(fileName)){ // En la clase Utils hay un metodo que me permite verificar si hay un archivo existente con ese mismo nombre fileExist(), al cual le mandamos los fileName
            load();         //Esto nos dice que si ya hay un archivo existente con este nombre de archivo, lo vamos a cargar o sobre Escribir
        }
        else{//Si el archivo no existe
            list = new ArrayList<>();//???preguntar
        }
    }

    @Override
    public JSONObject add(String strJson) throws Exception {
        Mercancia m = dataToAddOk(strJson);
        
        if (list.add(m)) {
            Utils.writeJSON(list, fileName);
        }
        return new JSONObject().put("message", "ok").put("data", m.toJSONObject());
    }

    public Mercancia dataToAddOk(String strJson)throws Exception{
        
        JSONObject json = new JSONObject(strJson);
        json.put("id", Utils.getRandomKey(8));
        updateCliente(json);
                
        Utils.stringOk("contenido", 5, json);
        Utils.doubleOk("ancho", 0.1, 2.44, json);
        Utils.doubleOk("largo", 0.1, 2.59, json);
        Utils.doubleOk("alto", 0.1, 12.19, json);
        LocalDateTime ingreso = LocalDateTime.parse(json.getString("fechaEntrada"));
        LocalDateTime salida = LocalDateTime.parse(json.getString("fechaSalida"));

        if (!ingreso.isBefore(salida)) {  //La clase LocalDateTime presenta los metodos before y after que permiten validar que fecha es previa a la otra
            throw new IllegalArgumentException("La fecha de ingreso debe ser inferior a la de salida");
        }
        Mercancia m = new Mercancia(json);
        if (list.contains(m)) {
            throw new ArrayStoreException(String.format("La mercancia %s ya existe", m.getId()));
        }
        return m; 
    }
    private void updateCliente (JSONObject json)throws Exception{
        String idCliente = json.getString("cliente");
        JSONObject jsonCliente = clientes.get(idCliente);
        json.put("cliente", jsonCliente);
    }

    @Override
    public JSONObject get(int index) {
        return list.get(index).toJSONObject();
    }

    @Override
    public JSONObject get(String id) throws Exception {
        int p = list.indexOf(new Mercancia(id)); 
        return p >-1?get(p):null; 
    }

    @Override
    public Mercancia getItem(String id) throws Exception {
        int i = list.indexOf(new Mercancia(id));
        return i>-1?list.get(i):null;
    }

    @Override
    public JSONObject getAll() {
        try {
            JSONArray data = new JSONArray();
            if(Utils.fileExists(fileName)){
                data = new JSONArray(Utils.readText(fileName));
            }
            return new JSONObject().put("message", "ok").put("data", data);
        }catch (IOException | JSONException e) {
            Utils.printStackTrace(e);
            return Utils.keyValueToJson("message", "Sin acceso a datos de mercancia", "error", e.getMessage());
        }
    }

    @Override
    public List<Mercancia> load() throws Exception {
        list = new ArrayList<>();  //estamos referenciando un Arraylist mediante list
        JSONArray jsonArr = new JSONArray(Utils.readText(fileName));//Estamos guardando en un JSONArray referenciadio por jsonArr un nuevo JSONArray que lee todo lo que hay en fileName y mediante readText lo convierte en String
        for (int i = 0; i < jsonArr.length(); i++){ // vamos a iterar el JSONArray, donde por cada iteracion lee los datos que hay dentro de este JSONArray y los mete dentro de un JSONObject, para posteriormente meterlo dentro de list(Arraylist)
            JSONObject jsonObj =  jsonArr.getJSONObject(i);
            list.add(new Mercancia(jsonObj));
        }  
      return list;
    }

    @Override
    public JSONObject update(String id, String strJson) throws Exception {
        JSONObject json = new JSONObject(strJson);
        if(json.has("cliente")){
            String idCliente = json.getString("cliente");
            JSONObject jsonClient = clientes.get(idCliente);
            json.put("cliente", jsonClient);
        }
        //buscar mercancia
        
        Mercancia mercancia = getItem(id);
      	int i = list.indexOf(mercancia);
      	if (mercancia == null) { //Si el cliente esta vacio retorna que no se encontro 
            throw new NullPointerException("No se encontró la mercancia: " + id);
        }
    
        JSONObject aux = mercancia.toJSONObject(); 
        JSONArray propiedades = json.names(); 
      	for (int k = 0; k < propiedades.length(); k++) {
          
            String propiedad = propiedades.getString(k); 
          	Object valor = json.get(propiedad);
          	aux.put(propiedad, valor);   
        }
        mercancia = new Mercancia(aux);
        list.set(i, mercancia);
        // actualizar el archivo de clientes
        Utils.writeJSON(list, fileName);
        // devolver el cliente con los cambios realizados
        return new JSONObject().put("message", "ok").put("data", mercancia.toJSONObject());
    }

    @Override
    public JSONObject remove(String id) throws Exception {
        Mercancia  m = getItem(id);
        if(m == null){
            throw new NoSuchElementException(String.format("No se encontro la mercancia %s%s%s para ser eliminada", Utils.RED,id, Utils.RESET));
        }
        if(!list.remove(m)){
            throw new Exception();
        }
        Utils.writeJSON(list, fileName);
        return new JSONObject().put("message", "ok").put("data", m.toJSONObject());
    }
    @Override
    public Class<Mercancia> getDataType() {
        return Mercancia.class;
    }

    @Override
    public Mercancia getUpdated(JSONObject newData, Mercancia current) throws Exception {
        JSONObject update = current.toJSONObject();

        if(newData.has("cliente")){
            try {
                updateCliente(newData);
                
            }catch (Exception e) {
                throw new IllegalArgumentException("Error al determinar el 'cliente' propietario de la mercancia");
                
            }   
        }
        if(newData.has("contenido")){
            update.put("contenido", Utils.stringOk("contenido", 5, newData));
        }
        if(newData.has("ancho")){//Verificamos que datos nos trae el newData y los qaue tenga los actualizamos y los que no permanecen igual
            update.put("ancho", Utils.doubleOk("ancho", 0.1, 2.44, newData));
        }
        if(newData.has("largo")){
            update.put("largo", Utils.doubleOk("largo", 0.1, 2.59, newData));
        }
        if(newData.has("alto")){
            update.put("alto", Utils.doubleOk("alto", 0.1, 12.19, newData));
        }
        if(newData.has("fechaEntrada")&&newData.has("fechaSalida")){
            LocalDateTime ingreso = LocalDateTime.parse(newData.getString("fechaEntrada"));
            LocalDateTime salida = LocalDateTime.parse(newData.getString("fechaSalida"));
            if (!ingreso.isBefore(salida)) {  //La clase LocalDateTime presenta los metodos before y after que permiten validar que fecha es previa a la otra
                throw new IllegalArgumentException("La fecha de ingreso debe ser inferior a la de salida");
            }
            update.put("fechaEntrada", newData.getString("fechaEntrada")).put("fechaSalida", newData.getString("fechaSalida"));
        }
        Mercancia m = new Mercancia(update);
        return m;
    }

    @Override
    public JSONObject size() {
        JSONObject tam = new JSONObject();
        tam.put("size", list.size());
        return tam;
    }

    @Override
    public JSONObject ciudades(String key) {
        ArrayList<Mercancia> listaMercancias = new ArrayList<>();
        for (Mercancia m : list) {
            if(m.getCiudad().equals(key)){
                listaMercancias.add(m);
            }
            
        }
        return new JSONObject().put("message", "ok").put("data",listaMercancias).put("size", listaMercancias.size());
    }  
}
