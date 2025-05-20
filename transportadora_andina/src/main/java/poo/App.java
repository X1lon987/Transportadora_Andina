package poo;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;
import poo.model.Cliente;
import poo.model.Envio;
import poo.model.EstadoEnvio;

import poo.services.ClienteService;
import poo.services.EnvioService;
import poo.services.MercanciaService;
import poo.services.Service;
import poo.services.SobreService;
import poo.helpers.Controller;
import poo.helpers.Utils;
import poo.model.Sobre;
import poo.model.Paquete;
import poo.model.Mercancia;
import poo.model.Caja;
import poo.model.Bulto;

import io.javalin.http.Context;;

public final class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class); // Esto de manera resumida es un sout()

    public static void main(String[] args) throws Exception {
        int port = 8080; // puerto en el que estamos trabajando
        String message = String.format(
                "%sIniciando la API Rest de Envios y bodegaje. Use Ctrl+C para detener la ejecución%s", Utils.CYAN,
                Utils.RESET);
        LOG.info(message);
        Utils.trace = true; // no deshabilite la traza de errores hasta terminar completamente la aplicación

        int length = args.length;
        if (length > 0) {
            Utils.trace = Boolean.parseBoolean(args[0]);
            if (length >= 2) {
                port = Integer.parseInt(args[1]);
            }
        }

        if (Utils.trace) {// Estamos verificando si esta habilitada o no la traza de errores y mandar un
                          // mensaje segun corresponda
            // ver para tiempo de desarrollo: ./.vscode/launch.json
            LOG.info(String.format("%sHabilitada la traza de errores%s", Utils.YELLOW, Utils.RESET));
        } else {
            LOG.info(String.format("%sEnvíe un argumento true|false para habilitar|deshabilitar la traza de errores%s",
                    Utils.YELLOW, Utils.RESET));
        }

        // esencial para estandarizar el formato monetario con separador de punto
        // decimal, no con coma
        Locale.setDefault(Locale.of("es_CO"));// Aqui definidmos los metodos de separacion decimal

        Service<Cliente> clienteService = new ClienteService();
        Service<Mercancia> mercanciaService = new MercanciaService(clienteService);
        Service<Envio> paqueteService = new EnvioService(Paquete.class, clienteService);
        Service<Envio> sobreService = new SobreService(Sobre.class, clienteService);
        Service<Envio> cajaService = new EnvioService(Caja.class, clienteService);
        Service<Envio> bultoService = new EnvioService(Bulto.class, clienteService);

        Javalin
                .create(config -> {
                    config.http.defaultContentType = "application/json";
                    // ver https://javalin.io/plugins/cors#getting-started
                    config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));

                    config.router.apiBuilder(() -> {
                        new Controller<>(clienteService).info();
                        new Controller<>(mercanciaService).info();
                        new Controller<>(paqueteService).info();
                        new Controller<>(sobreService).info();
                        new Controller<>(cajaService).info();
                        new Controller<>(bultoService).info();
                    });
                })
                .start(port)
                .get("/", ctx -> ctx.json("{ \"data\": \"Bienvenido al servicio de ventas\", \"message\": \"ok\" }"))
                .get("/envio/estados", ctx -> ctx.json(EstadoEnvio.getAll().toString()))
                .exception(
                        Exception.class,
                        (e, ctx) -> {
                            Utils.printStackTrace(e);
                            String error = Utils.keyValueToStrJson("message", e.getMessage(), "request", ctx.fullUrl());
                            ctx.json(error).status(404);
                        })
                .afterMatched(ctx ->{
                     updateClients(ctx);
                });
        Runtime
                .getRuntime()
                .addShutdownHook(
                        new Thread(() -> {
                            LOG.info(String.format("%sEl servidor Jetty de Javalin ha sido detenido%s%n", Utils.RED,
                                    Utils.RESET));
                        }));
    }

    // Aqui se va a crear la opcion de actualizar los clientes para todas las otras
    // clases que los contengan
    private static void updateClients(@NotNull Context ctx) throws Exception {
        // si se hace una solicitud de actualizar un cliente...
        if (ctx.path().contains("cliente") && ctx.method().toString().equals("PATCH")) {
            // result es la respuesta con la instancia actualizada del cliente
            JSONObject cliente = new JSONObject(ctx.result()).getJSONObject("data");
            // Buscar instancias de mercancias y envios con clientes con id == cliente.id y
            // actualizarlos
            Utils.ifExistsUpdateFile(cliente, "cliente", "Mercancia");
            Utils.ifExistsUpdateFile(cliente, "remitente", "Paquete");
            Utils.ifExistsUpdateFile(cliente, "destinatario", "Paquete");
            Utils.ifExistsUpdateFile(cliente, "remitente", "Sobre");
            Utils.ifExistsUpdateFile(cliente, "destinatario", "Sobre");
            Utils.ifExistsUpdateFile(cliente, "remitente", "Caja");
            Utils.ifExistsUpdateFile(cliente, "destinatario", "Caja");
            Utils.ifExistsUpdateFile(cliente, "remitente", "Bulto");
            Utils.ifExistsUpdateFile(cliente, "destinatario", "Bulto");
        }
    }
}