package poo;

import poo.model.Cliente;
import poo.model.Envio;
import poo.model.EstadoEnvio;
import poo.model.Estado;
import poo.model.Paquete;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.json.JSONObject;

public class Pruebas {
    
    public static void main(String[] args) {
        
        /*Cliente c1 = new Cliente("34102", "Ignacio", "Edificio Del Parque", "3122066479", "Manizales");
        Cliente c2 = new Cliente(c1); //Estamos copiando la instancia u1
        Cliente c3 = new Cliente("3420"); //Estamos usando el constructor para solo ingresar un valor (id) de cliente
        Cliente c4 = new Cliente("Luna", "Entrada Villamaria", "3212154916", "Bogota");
        JSONObject json = new JSONObject("{\" id :\"3432\" nombre :\"Diana\" direccion :\"Call 1b - 8a\" telefono :\"3212154916\" ciudad :\"Bogota\"}");
        */
        ArrayList<Estado> estados = pruebaestado();
        Cliente c = pruebaConstructoresClientes();
        Envio paquete = new Paquete("3452","Insumos",false, 10.0, 30000.0, estados, c, new Cliente());
        System.out.println("-".repeat(20));
        System.out.println(paquete.toJSONObject().toString(2));
        String json = """
                        {
                            "contenido": "Insumos",
                            "nroGuia": "4P5V4",
                            "peso": 10,
                            "fragil": false,
                            "remitente": {
                                "ciudad": "Pereira",
                                "direccion": "El Lago",
                                "id": "C06",
                                "telefono": "3125557777",
                                "nombre": "Andrea"
                            },
                            "valorEstimado": 30000,
                            "destinatario": {
                                "ciudad": "",
                                "direccion": "",
                                "id": "MQGHX",
                                "telefono": "",
                                "nombre": "NN"
                            },
                            "estados": [
                                {
                                "estado": "EN_PREPARACION",
                                "fechaHora": "2024-10-06T17:35:23"
                                },
                                {
                                "estado": "ENTREGADO",
                                "fechaHora": "2024-10-06T17:35:23"
                                }
                            ]
                        }
                """;
        JSONObject jsonObject = new JSONObject(json);
        Paquete p = new Paquete(jsonObject);
        System.out.println("-".repeat(20));
        System.out.println(p.toJSONObject());

        Envio pack2=new Paquete();
        pack2.setContenido("arroconhuevo");
        pack2.setRemitente(c);
        pack2.setEstados(estados);
        System.out.println(pack2.toString());
    }

    private static ArrayList<Estado> pruebaestado() {
        System.out.println();
        LocalDateTime ldt = LocalDateTime.now();
        Estado e1 = new Estado(ldt.plusDays(1),EstadoEnvio.EN_PREPARACION );
        Estado e2 = new Estado( ldt.plusDays(1),EstadoEnvio.ENTREGADO);

        System.out.println(e1.equals(e2));
        System.out.println(ldt);
        System.out.println(ldt.withNano(0));
        System.out.println(ldt.truncatedTo(ChronoUnit.SECONDS));

        ArrayList<Estado> estados = new ArrayList<>();
        estados.add(e1);
        estados.add(e2);
        return estados;
    }

    private static Cliente pruebaConstructoresClientes() {
        System.out.println();
        // constructor por defecto
        Cliente c1 = new Cliente();
        System.out.println(c1);

        // constructor parametrizado
        Cliente c2 = new Cliente("C2", "Carlos", "Av. Fundadores", "3113334444", "Manizales");
        System.out.println(c2.toJSONObject());

        // Constructor copia
        Cliente c3 = new Cliente(c2);
        System.out.println(c3.toJSONObject());

        // Constructor que recibe sólo el ID y asigna los otros datos vacíos o arbitrarios
        Cliente c4 = new Cliente("C4");
        System.out.println(c4.toJSONObject());

        // Constructor que recibe todos los datos, menos el ID y genera éste último de forma aleatoria
        Cliente c5 = new Cliente("Jorge", "El Cable", "3128887777", "Manizales");
        System.out.println(c5.toJSONObject());
        System.out.println(c5.toString());

        JSONObject jsonObject = new JSONObject("{\"ciudad\":\"Pereira\",\"direccion\":\"El Lago\",\"id\":\"C06\",\"telefono\":\"3125557777\",\"nombre\":\"Andrea\"}");
        // Constructor que recibe un JSONObject
        Cliente c6 = new Cliente(jsonObject);
        System.out.println(c6.toJSONObject());
        
        return c2;
    }   
}
