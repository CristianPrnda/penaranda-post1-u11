package com.tienda.pedidos.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StrategyTest {

    @Test
    @DisplayName("DescuentoRegular retorna 0 de descuento")
    void descuentoRegular_retornaCero() {
        EstrategiaDescuento estrategia = new DescuentoRegular();
        double descuento = estrategia.calcularDescuento(100000.0);
        assertThat(descuento).isEqualTo(0.0);
    }

    @Test
    @DisplayName("DescuentoVip retorna 15% de descuento")
    void descuentoVip_retornaQuincePorCiento() {
        EstrategiaDescuento estrategia = new DescuentoVip();
        double descuento = estrategia.calcularDescuento(100000.0);
        assertThat(descuento).isEqualTo(15000.0);
    }

    @Test
    @DisplayName("DescuentoMayorista retorna 25% de descuento")
    void descuentoMayorista_retornaVeinticincoPorCiento() {
        EstrategiaDescuento estrategia = new DescuentoMayorista();
        double descuento = estrategia.calcularDescuento(100000.0);
        assertThat(descuento).isEqualTo(25000.0);
    }
}
