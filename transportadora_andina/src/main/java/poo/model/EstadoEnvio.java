package poo.model;

import org.json.JSONArray;
import org.json.JSONObject;

public enum EstadoEnvio {
    DEVUELTO("DEVUELTO"),
    EN_PREPARACION("EN_PREPARACION"),
    ENVIADO("ENVIADO"),
    EN_CAMINO("EN_CAMINO"),
    EXTRAVIADO("EXTRAVIADO"),
    ENTREGADO("ENTREGADO"),
    INDEFINIDO("INDEFINIDO"),
    REENVIADO("REENVIADO"),
    RECIBIDO("RECIBIDO");
    
    private final String value;

    private EstadoEnvio(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public static EstadoEnvio getEnum(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        for (EstadoEnvio l : values()){
            if (value.equalsIgnoreCase(l.getValue())) {
                return l;
            }
        }
        throw new IllegalArgumentException();
    }

    public static JSONObject getAll() {
        JSONArray jsonArr = new JSONArray();
        for (EstadoEnvio v : values()) {
            jsonArr.put(
                    new JSONObject()  // Aqui lo que estamos haaciendo es meter nuestro Array de Objetos JSON dentro de un Objeto JSON
                            .put("ordinal", v.ordinal()) //el metodo ordinal() tambien viene definido dentro de todos los enum
                            .put("key", v)
                            .put("value", v.value));
        }
        return new JSONObject().put("message", "ok").put("data", jsonArr);
    } 
}
