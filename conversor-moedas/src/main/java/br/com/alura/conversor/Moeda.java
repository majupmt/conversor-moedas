package br.com.alura.conversor;

import java.util.Map;

public class Moeda {
    private Map<String, Double> conversion_rates;

    // Getter e Setter para Gson
    public Map<String, Double> getConversion_rates() {
        return conversion_rates;
    }

    public void setConversion_rates(Map<String, Double> conversion_rates) {
        this.conversion_rates = conversion_rates;
    }

    // Método para pegar a taxa da chave completa, exemplo: "USD_BRL"
    public double getConversion_rate(String chave) {
        if (this.conversion_rates == null) {
            // System.err.println("Atenção: 'conversion_rates' não foi inicializado em Moeda.");
            return 0.0;
        }
        return this.conversion_rates.getOrDefault(chave, 0.0);
    }
}