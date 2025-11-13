package fernanda;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Main {

    // Clase para mapear la respuesta JSON parcial
    static class ApiResponse {
        @SerializedName("conversion_rates")
        private Map<String, Double> conversionRates;

        public Map<String, Double> getConversionRates() {
            return conversionRates;
        }

        public void setConversionRates(Map<String, Double> conversionRates) {
            this.conversionRates = conversionRates;
        }
    }

    // Tu API key
    private static final String API_KEY = "ed70e971b9c86a84bd9994d1";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/USD";

    public static void main(String[] args) {
        ApiResponse response = fetchRates();
        if (response == null || response.getConversionRates() == null) {
            System.err.println("No se pudieron cargar las tasas de conversión. Revisa la conexión o la API key.");
            return;
        }

        Scanner teclado = new Scanner(System.in);
        List<String> historial = new ArrayList<>();
        int opcion = 0;
        double cantidad = 0;

        String menu = """
                 
                 Creado por : Fernanda Flores
                 
                 Bienvenido al Conversor de Monedas
                 1 - Dólar => Peso Mexicano
                 2 - Peso Mexicano => Dólar
                 3 - Dólar => Peso Argentino
                 4 - Peso Argentino => Dólar
                 5 - Dólar => Peso Colombiano
                 6 - Peso Colombiano => Dólar
                 7 - Salir
                """;

        System.out.println(menu);
        System.out.print("Elija una opción: ");

        while (!teclado.hasNextInt()) {
            System.out.println("Por favor, ingrese un número válido para la opción.");
            teclado.next();
        }
        opcion = teclado.nextInt();

        if (opcion >= 1 && opcion <= 6) {
            System.out.print("Ingrese la cantidad: ");
            while (!teclado.hasNextDouble()) {
                System.out.println("Por favor, ingrese un número válido para la cantidad.");
                teclado.next();
            }
            cantidad = teclado.nextDouble();

            String resultado = procesarConversion(opcion, cantidad, response);
            if (resultado != null) {
                historial.add(resultado);
                System.out.println(resultado);
            }

            // Pausar antes de terminar
            System.out.println("\nPresione ENTER para finalizar...");
            try {
                System.in.read();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Mostrar historial
            System.out.println("\n===== HISTORIAL DE CONVERSIONES =====");
            if (historial.isEmpty()) {
                System.out.println("No se realizaron conversiones.");
            } else {
                for (String h : historial) {
                    System.out.println("• " + h);
                }
            }

            System.out.println("\nGracias por utilizar el conversor, ¡hasta pronto!");
        } else {
            System.out.println("Saliendo del programa.");
        }

        teclado.close();
    }

    // Método para obtener tasas
    private static ApiResponse fetchRates() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Error al consultar la API. Código HTTP: " + response.statusCode());
                System.err.println("Cuerpo: " + response.body());
                return null;
            }

            Gson gson = new Gson();
            return gson.fromJson(response.body(), ApiResponse.class);

        } catch (Exception e) {
            System.err.println("Excepción al obtener tasas: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Procesar conversión y devolver texto del resultado
    private static String procesarConversion(int opcion, double cantidad, ApiResponse response) {
        Map<String, Double> rates = response.getConversionRates();
        String resultado = null;

        switch (opcion) {
            case 1 -> resultado = convertirUSDa("MXN", cantidad, rates);
            case 2 -> resultado = convertirAUSDdesde("MXN", cantidad, rates);
            case 3 -> resultado = convertirUSDa("ARS", cantidad, rates);
            case 4 -> resultado = convertirAUSDdesde("ARS", cantidad, rates);
            case 5 -> resultado = convertirUSDa("COP", cantidad, rates);
            case 6 -> resultado = convertirAUSDdesde("COP", cantidad, rates);
            default -> System.out.println("Opción no válida.");
        }

        return resultado;
    }

    private static String convertirUSDa(String monedaDestino, double cantidad, Map<String, Double> rates) {
        if (!rates.containsKey(monedaDestino)) return "No se encontró la tasa de cambio para " + monedaDestino + ".";
        double tasa = rates.get(monedaDestino);
        double resultado = cantidad * tasa;
        return String.format("%.2f USD equivale a %.2f %s", cantidad, resultado, monedaDestino);
    }

    private static String convertirAUSDdesde(String monedaOrigen, double cantidad, Map<String, Double> rates) {
        if (!rates.containsKey(monedaOrigen)) return "No se encontró la tasa de cambio para " + monedaOrigen + ".";
        double tasa = rates.get(monedaOrigen);
        if (tasa == 0) return "Tasa inválida (0). No se puede convertir.";
        double resultado = cantidad / tasa;
        return String.format("%.2f %s equivale a %.2f USD", cantidad, monedaOrigen, resultado);
    }
}
