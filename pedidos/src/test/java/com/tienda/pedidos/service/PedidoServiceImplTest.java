package com.tienda.pedidos.service;

import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.entity.Pedido;
import com.tienda.pedidos.exception.RecursoNoEncontradoException;
import com.tienda.pedidos.repository.PedidoRepository;
import com.tienda.pedidos.strategy.DescuentoMayorista;
import com.tienda.pedidos.strategy.DescuentoRegular;
import com.tienda.pedidos.strategy.DescuentoVip;
import com.tienda.pedidos.strategy.EstrategiaDescuento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository repo;

    @Mock
    private NotificacionService notificador;

    private PedidoServiceImpl pedidoService;

    @BeforeEach
    void setUp() {
        Map<String, EstrategiaDescuento> estrategias = new HashMap<>();
        estrategias.put("REGULAR", new DescuentoRegular());
        estrategias.put("VIP", new DescuentoVip());
        estrategias.put("MAYORISTA", new DescuentoMayorista());

        pedidoService = new PedidoServiceImpl(repo, notificador, estrategias);
    }

    @Test
    @DisplayName("crear con tipoCliente VIP aplica 15% de descuento")
    void crear_tipoClienteVIP_aplica15PorCientoDescuento() {
        PedidoRequestDTO request = new PedidoRequestDTO("ana@correo.com", "VIP", 100000.0);

        when(repo.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PedidoResponseDTO response = pedidoService.crear(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getClienteEmail()).isEqualTo("ana@correo.com");
        assertThat(response.getTipoCliente()).isEqualTo("VIP");
        assertThat(response.getSubtotal()).isEqualTo(100000.0);
        assertThat(response.getDescuento()).isEqualTo(15000.0);
        assertThat(response.getIva()).isEqualTo(16150.0); // (100000 - 15000) * 0.19
        assertThat(response.getTotal()).isEqualTo(101150.0); // 85000 + 16150
        assertThat(response.getEstado()).isEqualTo("CREADO");

        verify(notificador, times(1)).enviarConfirmacion(eq("ana@correo.com"), eq(101150.0));
    }

    @Test
    @DisplayName("crear con tipoCliente MAYORISTA aplica 25% de descuento")
    void crear_tipoClienteMayorista_aplica25PorCientoDescuento() {
        PedidoRequestDTO request = new PedidoRequestDTO("carlos@correo.com", "MAYORISTA", 100000.0);

        when(repo.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        PedidoResponseDTO response = pedidoService.crear(request);

        assertThat(response.getDescuento()).isEqualTo(25000.0);
        assertThat(response.getIva()).isEqualTo(14250.0); // (100000 - 25000) * 0.19
        assertThat(response.getTotal()).isEqualTo(89250.0); // 75000 + 14250
    }

    @Test
    @DisplayName("crear con tipoCliente REGULAR aplica 0% de descuento")
    void crear_tipoClienteRegular_aplica0PorCientoDescuento() {
        PedidoRequestDTO request = new PedidoRequestDTO("pedro@correo.com", "REGULAR", 100000.0);

        when(repo.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(3L);
            return p;
        });

        PedidoResponseDTO response = pedidoService.crear(request);

        assertThat(response.getDescuento()).isEqualTo(0.0);
        assertThat(response.getIva()).isEqualTo(19000.0);
        assertThat(response.getTotal()).isEqualTo(119000.0);
    }

    @Test
    @DisplayName("notasInternas se almacena en la entidad pero nunca se expone en PedidoResponseDTO")
    void crear_notasInternasSePersistePeroNoSeExponeEnDTO() {
        PedidoRequestDTO request = new PedidoRequestDTO("ana@correo.com", "VIP", 50000.0);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        when(repo.save(captor.capture())).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        PedidoResponseDTO response = pedidoService.crear(request);

        Pedido entidadGuardada = captor.getValue();
        assertThat(entidadGuardada.getNotasInternas())
                .isEqualTo("Pedido creado automaticamente, revisar stock antes de despachar");
        // Verificar que la clase PedidoResponseDTO ni siquiera declara el campo notasInternas
        boolean tieneCampoNotasInternas = false;
        for (var field : PedidoResponseDTO.class.getDeclaredFields()) {
            if ("notasInternas".equals(field.getName())) {
                tieneCampoNotasInternas = true;
                break;
            }
        }
        assertThat(tieneCampoNotasInternas).isFalse();
    }

    @Test
    @DisplayName("buscarPorId cuando existe retorna PedidoResponseDTO")
    void buscarPorId_cuandoExiste_retornaDTO() {
        Pedido pedido = new Pedido(1L, "ana@correo.com", "VIP", 100000.0, 15000.0, 16150.0, 101150.0,
                "CREADO", "Notas secretas", LocalDateTime.now());

        when(repo.findById(1L)).thenReturn(Optional.of(pedido));

        PedidoResponseDTO response = pedidoService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getClienteEmail()).isEqualTo("ana@correo.com");
    }

    @Test
    @DisplayName("buscarPorId cuando no existe lanza RecursoNoEncontradoException")
    void buscarPorId_cuandoNoExiste_lanzaRecursoNoEncontradoException() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> pedidoService.buscarPorId(999L)
        );

        assertThat(ex.getMessage()).contains("Pedido con id 999 no encontrado");
    }
}
