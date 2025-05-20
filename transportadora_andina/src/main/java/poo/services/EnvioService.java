package poo.services;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import org.json.JSONArray;
import org.json.JSONObject;

import poo.model.Envio;
import poo.model.Estado;
import poo.model.EstadoEnvio;
import poo.helpers.Utils;
import poo.model.Cliente;

import java.io.IOException;
import java.util.NoSuchElementException; 
import org.json.JSONException;

public class EnvioService implements Service<Envio> {
    private final Class<? extends Envio> subclase;
    private final Service<Cliente>clientes;
    private List<Envio> list;
    private final String fileName;
 
    public EnvioService(Class<? extends Envio> subclase,Service<Cliente>clientes) throws Exception {//Class<? extends envio > nos indica que va a recibir cualquiere clase proviniente de envio como un tipo(Paquete, Caja, Sobre, Bulto)
        this.subclase = subclase;
        this.clientes = clientes;
        fileName = Utils.PATH + subclase.getSimpleName()+".json";//Para asignarel end point del archivo utilizamos la variabloe subclase.getSimpleName() para conocer aque tipo de envio es 
        
        if(Utils.fileExists(fileName)){
            load();
        }
        else{
            list = new ArrayList<>();
        }
    }

    @Override
    public JSONObject add(String strJson) throws Exception {
        Envio e = dataToAddOk(strJson);
        if (list.add(e)) {
            Utils.writeJSON(list, fileName);
        }
        return new JSONObject().put("message", "ok").put("data", e.toJSONObject());
    }
    private void updateCliente(JSONObject json, String key)throws Exception{
        String idCliente = json.getString(key);
        JSONObject jsonCliente = clientes.get(idCliente);
        json.put(key, jsonCliente);
    }

    public Envio dataToAddOk(String strJson)throws Exception {
        JSONObject json = new JSONObject(strJson);
        String aux = "nroGuia"; 
        if(!json.has(aux) || json.getString(aux).isBlank() || (json.getString(aux).length() > 14)){
            json.put("nroGuia", Utils.getRandomKey(8));
        }
        
        if(!json.has("estados")){
            JSONObject jsonO = new JSONObject();
            jsonO.put("fechayHora", LocalDateTime.now().withNano(0).toString()).put("estado", EstadoEnvio.RECIBIDO.getValue());
            
            JSONArray js = new JSONArray();

            json.put("estados", js.put(jsonO));
        }
        if(!json.has("fragil")){
            json.put("fragil", false);
        }
        json.put("contenido", Utils.stringOk("contenido", 3, json));
        
        json.put("peso", Utils.doubleOk("peso", 0, 999999999, json));
        json.put("valoreEstimado", Utils.doubleOk("valorEstimado", 0, 1000000, json));

        if(json.getString("remitente").equals(json.getString("destinatario"))){
            String idCliente = json.getString("remitente");
            throw new IllegalArgumentException(String.format("Se espera un destinatario diferente al remitente: id = %s%s%s", Utils.RED, idCliente,Utils.RESET));
        }
        updateCliente(json, "remitente");
        updateCliente(json, "destinatario");

        Envio e = subclase.getConstructor(JSONObject.class).newInstance(json);

        //Verificacion si hay algun otro Envio dentro de la list
        if (list.contains(e)) {
            throw new ArrayStoreException(String.format("El %s %s%s%s ya existe",getClass().getSimpleName(),Utils.PURPLE, e.getNroGuia(),Utils.RESET));
        } 
        return e;
    }

    @Override
    public JSONObject get(int index) {
        return list.get(index).toJSONObject();
    }

    @Override
    public JSONObject get(String nroGuia) throws Exception {
        Envio e = getItem(nroGuia);
        
        if(e == null){
            throw new Exception(String.format("No se encontro un envio con el numero de guia %s",  nroGuia));
        }
        return e.toJSONObject();
    }

    @Override
    public Envio getItem(String nroGuia) throws Exception {
        Envio e = subclase.getConstructor(String.class).newInstance(nroGuia);//pide el constructor que nos recibe un parametro que sea de tipo String
        int i = list.indexOf(e);
        return i >-1?list.get(i):null;
    }

    @Override
    public JSONObject getAll() {
        try {
            JSONArray data = new JSONArray(Utils.readText(fileName));
            return new JSONObject().put("message", "ok").put("data", data);
        }catch (IOException | JSONException e) {
            Utils.printStackTrace(e);
            return Utils.keyValueToJson("message", "Sin acceso a datos del Envio", "error", e.getMessage());
        }
    }

    @Override
    public List<Envio> load() throws Exception {
        list = new ArrayList<>();  //estamos referenciando un Arraylist mediante list
        JSONArray jsonArr = new JSONArray(Utils.readText(fileName));//Estamos guardando en un JSONArray referenciadio por jsonArr un nuevo JSONArray que lee todo lo que hay en fileName y mediante readText lo convierte en String
        for (int i = 0; i < jsonArr.length(); i++){ // vamos a iterar el JSONArray, donde por cada iteracion lee los datos que hay dentro de este JSONArray y los mete dentro de un JSONObject, para posteriormente meterlo dentro de list(Arraylist)
            JSONObject jsonObj =  jsonArr.getJSONObject(i);
            list.add(subclase.getConstructor(JSONObject.class).newInstance(jsonObj));
        }  
      return list;

    }

    @Override
    public JSONObject update(String nroGuia, String strJson) throws Exception {
        JSONObject newData = new JSONObject(strJson);
        Envio e = subclase.cast(getItem(nroGuia));
        if(e == null){
            throw new NullPointerException(String.format("No se pudo encontrar el envio con numero Guia : %s",  nroGuia));
        }
       
        int i = list.indexOf(e);
        e = getUpdated(newData, e);
        list.set(i, e);

        Utils.writeJSON(list, fileName);
        return new JSONObject().put("message", "ok").put("data", e.toJSONObject());
    }
    
    @Override
    public JSONObject remove(String nroGuia) throws Exception {//obtener envio segun el nroGuia, tratar de remover de la lista if(list.remove), writeJson list, return mensaje default 
       Envio e =getItem(nroGuia);
       if(e == null){
            throw new NoSuchElementException(String.format("No se encontro el envio %s para ser eliminado",  nroGuia));
        }
       if(!list.remove(e)){
            throw new Exception("no se pudo eliminar el envio");   
        }
        Utils.writeJSON(list, fileName);

        return new JSONObject().put("message", "ok").put("data", e.toJSONObject());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Class<Envio> getDataType() {
        return (Class<Envio>)subclase;
    }

    @Override
    public Envio getUpdated(JSONObject newData, Envio current) throws Exception {    
        
        JSONObject update = current.toJSONObject();
        if(newData.has("destinatario")){
            try {       
                updateCliente(newData, "destinatario"); 
                update.put("destinatario",newData.get("destinatario"));

            } catch (Exception e) {
                throw new IllegalArgumentException("Fallo algo en updateCliente");
            }
        }
        if(newData.has("remitente")){
            try {       
                updateCliente(newData, "remitente"); 
                update.put("remitente",newData.get("remitente"));

            } catch (Exception e) {
                throw new IllegalArgumentException("Fallo algo en updateCliente");
            }
        }

        if(newData.has("estados")){
            JSONArray estados = newData.getJSONArray("estados");
            int ultimo = estados.length()-1;
            Estado ultimoEstado = new Estado(estados.getJSONObject(ultimo));

            if(okEstados(estados, ultimoEstado)){
                update.put("estados",estados);
            }
            
        }
        if(newData.has("peso")){
            update.put("peso",Utils.doubleOk("peso", 0, 999, newData)); 
        }
        if(newData.has("fragil")){
            update.put("fragil", newData.getBoolean("fragil"));
        }
        if(newData.has("contenido")){
            update.put("contenido", Utils.stringOk("contenido", 3, newData));
        }
        if(newData.has("valorEstimado")){
            update.put("valorEstimado", Utils.doubleOk("valorEstimado", 0, 9999999, newData));
        }
        if(newData.has("certificado")){
            update.put("certificado", newData.getBoolean("certificado"));
        }
        if(newData.has("ancho")){
            update.put("ancho", Utils.doubleOk("ancho", 0.1, 2.44, newData));
        }
        if(newData.has("largo")){
            update.put("largo", Utils.doubleOk("largo", 0.1, 2.59, newData));
        }
        if(newData.has("alto")){
            update.put("alto", Utils.doubleOk("alto", 0.1, 12.19, newData));
        }

        Envio e = subclase.getConstructor(JSONObject.class).newInstance(update);
        return e;
    }
//Aqui van a realizrse los cambios de adicion para los estados y ser modificados dentro de los envios

    public boolean okEstados(JSONArray  listEstados, Estado nuevoEstado)throws Exception{
        String orden[] = {"RECIBIDO","EN_PREPARACION","ENVIADO","EN_CAMINO","EXTRAVIADO","REENVIADO","DEVUELTO","ENTREGADO"};
        boolean respuesta = true; 
               
        for (int i = 0; i < listEstados.length();i++){
            
            String estadoOrden = listEstados.getJSONObject(i).getString("estado");
            if(i<4){
                if(!estadoOrden.equals(orden[i])){
                    throw new IllegalArgumentException(String.format("El estado siguiente a %s tiene que ser %s", orden[i-1],orden[i]));
                }    
            }
            else{ 
                if(i==4){
                    if(!estadoOrden.equals(orden[4])&&!estadoOrden.equals(orden[5])&&!estadoOrden.equals(orden[6])&&!estadoOrden.equals(orden[7])){
                        throw new IllegalArgumentException(String.format("despues de %s el estado no puede ser diferente a EXTRAVIADO|REENVIADO|DEVUELTO|ENTREGADO", orden[3]));
                    }
                }
                if(orden[i-1].equals(orden[5])&&!estadoOrden.equals(orden[7])&&!estadoOrden.equals(orden[6])){
                    throw new IllegalArgumentException(String.format("El estado despues de ser %s debe ser %s o %s", orden[i-1],orden[6],orden[7]));
                }
            }    
        } 
        return respuesta;  
    }
    @Override
    public JSONObject size() {
        JSONObject tam = new JSONObject();
        tam.put("size", list.size());
        return tam;
    }
    
}
