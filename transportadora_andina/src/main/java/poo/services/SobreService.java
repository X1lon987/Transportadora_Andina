package poo.services;

import org.json.JSONObject;

import poo.model.Cliente;
import poo.model.Envio;

public class SobreService extends EnvioService{

    public SobreService(Class<? extends Envio> subclase, Service<Cliente> clientes) throws Exception {
        super(subclase, clientes);
    }
    public JSONObject add(String strJson)throws Exception{
        JSONObject json = new JSONObject(strJson);
        json.put("peso", 0).put("fragil", false);

        if(!json.has("contenido")){
            json.put("contenido", "Documentos");
        }

        if(!json.has("certificado")){
            json.put("certificado", false);
        }
        return super.add(json.toString());
    } 
}
