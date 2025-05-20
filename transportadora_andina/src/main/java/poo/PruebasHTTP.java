package poo;

import java.util.Locale;

import org.json.JSONObject;

import io.javalin.Javalin;
import poo.model.Cliente;
import poo.services.ClienteService;
import poo.services.Service;
import poo.helpers.Utils;

public class PruebasHTTP {
    public static void main(String[] args) throws Exception {
        int port = 8080;

        // esencial para estandarizar el formato monetario con separador de punto decimal, no con coma
        Locale.setDefault(Locale.of("es_CO"));//Aqui definimos el formato del idioma de nuestras peticiones

        Service<Cliente> clienteService = new ClienteService();
//esto realmente es una mala practica, ya que si vamos a implementar estos mismos procedimientos(post,get,update,patch,delete) para mercania y envio, por lo que debemos implementar un Controlador en el que esten predefinidos estos procesos y este parametrizado para todos 
        Javalin
                .create(/*sin config*/)
                // Agregar clientes (Create)
                .post(
                        "/cliente",   //Parte final de una url se llama end point
                        ctx -> {
                            JSONObject response = clienteService.add(ctx.body()); //Aqui recibimos el cuerpo de la peticion POST que tenemos en el Archivo Peticione.HTTTP
                            ctx.json(response.toString()); 
                        })
                // Listar clientes (Read)
                .get(
                        "/cliente",
                        ctx -> {                //EL hace un callback para mostrar todos los datos
                            JSONObject response = clienteService.getAll(); //Para poder hacer todo eso debemos llamar al metodo get.All()
                            ctx.json(response.toString());
                        })
                // Obtener los datos de un cliente por posición o indice o id, podemos colocar dentro de param (Read)
                .get(                                        //Muestra los datos
                        "/cliente/{param}",
                        ctx -> {
                            String arg = ctx.pathParam("param");
                            JSONObject response;

                            if (arg.matches("-?\\d+")) {   
                                //Aca esta buscando si el argumento tiene solo numeros
                                // si es un número en base 10, buscar por posición en la lista
                                int i = Integer.parseInt(arg, 10); //Si son solo numeros trata de convertir el texto en un entero i
                                response = clienteService.get(i); //Posteriormente se lo mandamos al get que recibe posicion(i).
                            } else {
                                // en caso contrario, busca por ID
                                response = clienteService.get(arg); //Le manda el texto para verifiocar si hay elemenetos con ese id(arg)
                            }
                            //POR LO TANTO  NO PUEDEN HABER CLIENTES CON ID DE SOLO NUMEROS, YA QUE LO TOMARIO COMO SI FUESE UN INDICE Y NO UN ID
                            ctx.json(response.toString());
                        })
                // Actualizar los datos de un cliente, dado su ID (Update)
                .patch(
                        "/cliente/{param}",
                        ctx -> {
                            String id = ctx.pathParam("param");
                            String newData = ctx.body();
                            ctx.json(clienteService.update(id, newData).toString());
                        })
                // Eliminar un cliente
                .delete(
                        "/cliente/{param}",
                        ctx -> ctx.json(clienteService.remove(ctx.pathParam("param"))))
                .exception(
                        Exception.class,
                        (e, ctx) -> {
                            Utils.printStackTrace(e);
                            String error = Utils.keyValueToStrJson("message", e.getMessage(), "request", ctx.fullUrl());
                            ctx.json(error).status(400);
                        })
                .start(port);

    }
}