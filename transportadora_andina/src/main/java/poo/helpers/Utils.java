package poo.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // <-------- OJO
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random; // <-------- OJO

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.Property;

public class Utils {

  public static final String RESET = "\u001B[0m";
  public static final String BLACK = "\u001B[30m";
  public static final String RED = "\u001B[31m";
  public static final String GREEN = "\u001B[32m";
  public static final String YELLOW = "\u001B[33m";
  public static final String BLUE = "\u001B[34m";
  public static final String PURPLE = "\u001B[35m";
  public static final String CYAN = "\u001B[36m";
  public static final String WHITE = "\u001B[37m";

  public static final String PATH = "./data/";
  public static boolean trace = false; // Habilita o deshabilita la traza de errores de donde se utilice

  private Utils() {
  } // lo mismo en Keyboard

  public static void printStackTrace(Exception e) {
    if (Utils.trace) {
      System.out.printf("%s%s%s%s%s%n", Utils.RED, "-".repeat(30), " Reporte de excepciones ", "-".repeat(30),
          Utils.RESET);
      e.printStackTrace(System.out);
      System.out.printf("%s%s%s%s%s%n", Utils.RED, "-".repeat(30), " Fin del reporte de excepciones ", "-".repeat(30),
          Utils.RESET);
    }
  }

  /**
   * Genera un string de caracteres alfanuméricos aleatorios de una longitud dada
   * Ver: https://www.baeldung.com/java-random-string
   * 
   * @param stringLength La longitud que se requiere para el string
   * @return Un string de caracteres alfanuméricos aleatorios de una longitud
   */
  public static String getRandomKey(int stringLength) {

    // Aca lo que estamos haciendo es limitar que caracteres vamos a tener dentro de
    // la trabla ASCII
    int leftLimit = 48; // numeral '0'
    int rightLimit = 90; // letter 'Z'
    Random random = new Random();

    String generatedString = random
        .ints(leftLimit, rightLimit + 1) // definir el rango de caracteres ['0'..'Z']
        // Para cualquier numero random debemos sumarle uno para que tome hasta el
        // ultimo dato
        .filter(i -> (i <= 57 || i >= 65)) // filtrar entre ['0'..'9'] o entre ['A'..'Z']
        .limit(stringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();

    return generatedString;
  }

  public static String strDateTime(LocalDateTime dateTime) {
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    return dateTime.format(format);
  }

  public static boolean fileExists(String fileName) {
    Path dirPath = Paths.get(fileName); // Paths es una clase que tiene java dentro de si que devuelve el PATH de ese
                                        // archivo
    return Files.exists(dirPath) && !Files.isDirectory(dirPath); // Esto nos devuelve que el archivo no exista y que no
                                                                 // sea un directorio
  }

  public static boolean pathExists(String path) {
    Path folder = Paths.get(path);
    return Files.exists(folder) && Files.isDirectory(folder);
  }

  public static void createFolderIfNotExist(String folder) throws IOException {
    // si no existe o si existe y no es una carpeta, crear la carpeta
    if (!pathExists(folder)) {
      Path dirPath = Paths.get(folder);
      Files.createDirectories(dirPath);
    }
  }

  public static String getPath(String path) {
    Path parentPath = Paths.get(path).getParent();
    return parentPath == null ? null : parentPath.toString();
  }

  /**
   * Crea la ruta padre indicada en el argumento recibido si no existe
   * 
   * @param filePath Un String que representa una ruta válida
   * @return Una instancia de Path con la ruta original
   * @throws IOException
   */
  public static Path initPath(String filePath) throws IOException {
    String path = getPath(filePath);
    createFolderIfNotExist(path);
    return Paths.get(filePath);
  }

  public static String readText(String fileName) throws IOException {
    Path path = Paths.get(fileName);
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  public static void _writeText(List<?> list, String fileName) throws Exception {
    initPath(fileName);
    try (FileWriter fw = new FileWriter(new File(fileName), StandardCharsets.UTF_8);
        BufferedWriter writer = new BufferedWriter(fw)) {
      for (int i = 0; i < list.size(); i++) {
        writer.append(list.get(i).toString());
        writer.newLine();
      }
    }
  }

  public static void writeText(List<?> list, String fileName) throws Exception {
    // https://www.tabnine.com/code/java/methods/java.nio.file.Files/newBufferedWriter
    Path path = initPath(fileName);
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      for (Object o : list) {
        writer.append(o.toString());
        writer.newLine();
      }
    }
  }

  public static void writeText(String content, String fileName) throws IOException {
    Path path = initPath(fileName);
    Files.write(path, content.getBytes(StandardCharsets.UTF_8)); // Guarda lo que manda writeJSON en formato de texto
  }

  public static void writeJSON(List<?> list, String fileName) throws IOException { // Recibe una lista de cualquier cosa
                                                                                   // <?> y el nombre del archivo
    JSONArray jsonArray = new JSONArray(list); // toma la lista y lo vuelve enun arrayJSON
    writeText(jsonArray.toString(2), fileName);// Aca formateamos el ArrayJSON y lo mandamos a otro metodo (writeText),
                                               // junto al nombre del archivo
  }

  /**
   * Convierte parámetros de una URL en una representación JSON
   * 
   * @param s Algo así como param1=value1&param2=value2...
   * @return Un String JSON con los pares paramX=valueX de s
   * @throws IOException
   */
  public static String paramsToJson(String s) throws IOException {
    s = s.replace("&", "\n");
    StringReader reader = new StringReader(s);
    Properties properties = new Properties();
    properties.load(reader);
    return Property.toJSONObject(properties).toString(2);
  }

  /**
   * Convierte un número par de strings en una representación json {key:value,
   * ...}
   * 
   * @param strings los strings (en número par) que se convierten a json
   * @return Un String JSON con los pares key=value de strings
   */
  public static JSONObject keyValueToJson(String... strings) {
    JSONObject json = new JSONObject();
    for (int i = 0; i < strings.length; i += 2) {
      json.put(strings[i], strings[i + 1]);
    }
    return json;
  }

  public static String keyValueToStrJson(String... strings) {// ??
    return keyValueToJson(strings).toString();
  }

  /**
   * Verifica en cualquier archivo de tipo JSON si un objeto está contenido en uno
   * de los objetos
   * JSON que conforman el array de objetos JSON contenido en el archivo.
   * 
   * @param fileName El nombre del archivo sin extensión, que contiene el array de
   *                 objetos JSON
   * @param key      La clave o atributo que identifica el objeto JSON a buscar
   *                 dentro de cada objeto
   * @param search   El objeto JSON a buscar
   * @return True si se encuentra que search alguno de los objetos del array
   * @throws Exception
   */
  public static boolean exists(String fileName, String key, JSONObject search) throws Exception {
    if (!Utils.fileExists(fileName + ".json")) {
      return false;
    }

    String data = readText(fileName + ".json");
    JSONArray jsonArrayData = new JSONArray(data);

    for (int i = 0; i < jsonArrayData.length(); i++) {
      // obtener el JSONObject del array en la iteración actual
      JSONObject jsonObj = jsonArrayData.getJSONObject(i);

      if (jsonObj.has(key)) {
        // De la instancia actual obtener el objeto JSON que se requiere verificar
        jsonObj = jsonObj.getJSONObject(key);
        // OJO >>> comparar los strings de ambos JSONObject
        if (jsonObj.toString().equals(search.toString())) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Verifica en cualquier archivo de tipo JSON si un objeto con una propiedad
   * determinada, está
   * contenido en uno de los objetos JSON que conforman el array de objetos JSON
   * contenido en el archivo.
   * 
   * @param fileName El nombre del archivo sin extensión, que contiene el array de
   *                 objetos JSON
   * @param key      La clave o atributo que identifica el objeto JSON a buscar
   *                 dentro de cada objeto
   * @param search   El objeto JSON a buscar
   * @param property La clave del objeto que se usa para hacer la comparación.
   *                 Ej.: "id"
   * @return True si se encuentra que search alguno de los objetos del array
   * @throws Exception
   */
  public static boolean exists(String fileName, String key, JSONObject search, String property) throws Exception {
    if (!Utils.fileExists(fileName + ".json")) {
      return false;
    }
    String data = readText(fileName + ".json");
    JSONArray jsonArrayData = new JSONArray(data);

    for (int i = 0; i < jsonArrayData.length(); i++) {
      // obtener el JSONObject del array en la iteración actual
      JSONObject jsonObj = jsonArrayData.getJSONObject(i);

      if (jsonObj.has(key)) {
        // De la instancia actual obtener el objeto JSON que se requiere verificar
        jsonObj = jsonObj.getJSONObject(key);
        // OJO >>> utilizar una de las propiedades de los objetos para hacer la
        // comparación
        if (jsonObj.optString(property).equals(search.optString(property))) {
          return true;
        }
      }
    }

    return false;
  }

  public static String MD5(String s) throws Exception {
    MessageDigest m = MessageDigest.getInstance("MD5");
    m.update(s.getBytes(), 0, s.length());
    return new BigInteger(1, m.digest()).toString(16);
  }

  public static String stringOk(String key, int n, JSONObject json) {
    if (!json.has(key)) {
      throw new IllegalArgumentException(String.format("La llave %s%s%s no existe ", YELLOW, key, RESET));
    }
    if (json.getString(key).length() < n) {
      throw new IllegalArgumentException(String.format("se espera al menos %s%s%s caracteres: %s%s%s = %s%s%s", RED, n,
          RESET, RED, key, RESET, RED, json.getString(key), RESET));
    }
    return json.getString(key);
  }

  public static double doubleOk(String key, double min, double max, JSONObject json) {
    if (json.getDouble(key) < min || json.getDouble(key) > max) {
      throw new IllegalArgumentException(String.format("Se esperaba un valor entre %s%s%s y %s%s%s: %s%s%s = %s%s%s",
          RED, min, RESET, RED, max, RESET, RED, key, RESET, RED, json.getDouble(key), RESET));
    }
    return json.getDouble(key);
  }

  // Actualizar la información de uncliente en las mercancias y envíos en los que
  // exista
  public static void ifExistsUpdateFile(JSONObject cliente, String key, String fileName) throws Exception {
    // Crear el full name con el path y el json
    String fullFileName = Utils.PATH + fileName + ".json";
    // Mirar si el archivo existe
    if (Utils.fileExists(fullFileName)) {
      String data = readText(fullFileName);
      JSONArray jsonArrayData = new JSONArray(data);
      JSONObject c = new JSONObject();

      for (int i = 0; i < jsonArrayData.length(); i++) {
        // obtener el JSONObject del array en la iteración actual
        JSONObject jsonObj = jsonArrayData.getJSONObject(i);

        if (jsonObj.has(key)) {
          // De la instancia actual obtener el objeto JSON que se requiere verificar
          c = jsonObj.getJSONObject(key);
          // OJO >>> comparar los id de ambos JSONObject
          if (c.getString("id").equals(cliente.getString("id"))) {
            // Asignarle al jsonObject la informacion nueva
            jsonArrayData.getJSONObject(i).put(key, cliente);
          }
        }
      }
      List<JSONObject> newData = new ArrayList<>();
      for (int i = 0; i < jsonArrayData.length(); i++) {
        JSONObject jsonObj = jsonArrayData.getJSONObject(i);
        newData.add((jsonObj));
      }
      Utils.writeJSON(newData, fullFileName);
    }
  }
}