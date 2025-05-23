package br.com.alura.conversor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; // Importar TypeToken

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map; // Importar Map
import java.util.Scanner;

public class Principal {

    private static final String API_KEY = "e87a2693b694f1125f6f66f7"; // Sua API Key

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String[][] opcoes = {
                {"USD", "BRL"},
                {"BRL", "USD"},
                {"USD", "EUR"},
                {"EUR", "USD"},
                {"BRL", "ARS"},
                {"ARS", "BRL"}
        };

        System.out.println("=== CONVERSOR DE MOEDAS ===");
        System.out.println("Escolha uma opção:");
        for (int i = 0; i < opcoes.length; i++) {
            System.out.printf("%d. %s -> %s%n", i + 1, opcoes[i][0], opcoes[i][1]);
        }

        System.out.print("Opção: ");
        int opcao = scanner.nextInt();

        if (opcao < 1 || opcao > opcoes.length) {
            System.out.println("Opção inválida.");
            scanner.close(); // Fechar scanner ao sair
            return;
        }

        String from = opcoes[opcao - 1][0];
        String to = opcoes[opcao - 1][1];

        System.out.printf("Digite o valor em %s: ", from);
        double valor = scanner.nextDouble();

        try {
            double taxa = obterTaxaDeCambio(from, to);
            double convertido = valor * taxa;

            System.out.printf("Resultado: %.2f %s = %.2f %s%n", valor, from, convertido, to);
        } catch (Exception e) {
            System.out.println("Erro ao obter taxa de câmbio: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static double obterTaxaDeCambio(String from, String to) throws Exception {
        String urlStr = String.format(
                "https://v6.exchangerate-api.com/v6/%s/latest/%s",
                API_KEY, from
        );

        HttpURLConnection conn = null;
        InputStreamReader reader = null;

        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String errorMessage = "Falha na conexão com a API: Código " + responseCode;
                try (Scanner errorScanner = new Scanner(conn.getErrorStream())) {
                    StringBuilder errorResponse = new StringBuilder();
                    while (errorScanner.hasNextLine()) {
                        errorResponse.append(errorScanner.nextLine());
                    }
                    if (errorResponse.length() > 0) {
                        errorMessage += " - Resposta da API: " + errorResponse.toString();
                    }
                } catch (Exception e2) {
                    // Ignorar erro ao ler stream de erro
                }
                throw new Exception(errorMessage);
            }

            reader = new InputStreamReader(conn.getInputStream());
            Gson gson = new Gson();

            Map<String, Object> apiResponse = gson.fromJson(reader,
                    new TypeToken<Map<String, Object>>() {
                    }.getType());

            if (apiResponse == null || !"success".equals(apiResponse.get("result"))) {
                throw new Exception("Resposta inválida da API: " + (apiResponse != null ? apiResponse.get("error-type") : "Nenhum erro reportado"));
            }

            Map<String, Double> conversionRates = (Map<String, Double>) apiResponse.get("conversion_rates");

            if (conversionRates == null || !conversionRates.containsKey(to)) {
                throw new Exception("Resposta da API não contém a taxa de câmbio esperada para: " + to);
            }

            return conversionRates.get(to);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}