package poo.services;

import org.json.JSONObject;

import poo.model.Mercancia;

public interface ServiceMercancia extends Service<Mercancia> {
    
    //se crea la firma del metodo para ser implementado en mercanciaService
    public JSONObject  ciudades(String key);
}
