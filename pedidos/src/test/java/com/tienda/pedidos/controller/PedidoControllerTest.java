package com.tienda.pedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.exception.GlobalExceptionHandler;
import com.tienda.pedidos.exception.RecursoNoEncontradoException;
import com.tienda.pedidos.service.PedidoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PedidoController.class)
@Import(GlobalExceptionHandler.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    @Test
    @DisplayName("POST /api/pedidos válido retorna 201 Created y PedidoResponseDTO")
    void crearPedido_valido_retorna201() throws Exception {
        PedidoRequestDTO request = new PedidoRequestDTO("ana@correo.com", "VIP", 100000.0);
        PedidoResponseDTO response = new PedidoResponseDTO(1L, "ana@correo.com", "VIP", 100000.0,
                15000.0, 16150.0, 101150.0, "CREADO");

        when(pedidoService.crear(any(PedidoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteEmail").value("ana@correo.com"))
                .andExpect(jsonPath("$.tipoCliente").value("VIP"))
                .andExpect(jsonPath("$.subtotal").value(100000.0))
                .andExpect(jsonPath("$.descuento").value(15000.0))
                .andExpect(jsonPath("$.iva").value(16150.0))
                .andExpect(jsonPath("$.total").value(101150.0))
                .andExpect(jsonPath("$.estado").value("CREADO"))
                .andExpect(jsonPath("$.notasInternas").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/pedidos con datos inválidos retorna 400 Bad Request y ApiError")
    void crearPedido_invalido_retorna400ConApiError() throws Exception {
        PedidoRequestDTO requestInvalido = new PedidoRequestDTO("", "", -50.0);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value(containsString("El email del cliente es obligatorio")))
                .andExpect(jsonPath("$.mensaje").value(containsString("El tipo de cliente es obligatorio")))
                .andExpect(jsonPath("$.mensaje").value(containsString("El subtotal debe ser mayor a cero")))
                .andExpect(jsonPath("$.path").value("/api/pedidos"));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} cuando existe retorna 200 OK y PedidoResponseDTO")
    void obtenerPedido_existe_retorna200() throws Exception {
        PedidoResponseDTO response = new PedidoResponseDTO(1L, "ana@correo.com", "VIP", 100000.0,
                15000.0, 16150.0, 101150.0, "CREADO");

        when(pedidoService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteEmail").value("ana@correo.com"))
                .andExpect(jsonPath("$.notasInternas").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} inexistente retorna 404 Not Found y ApiError")
    void obtenerPedido_inexistente_retorna404ConApiError() throws Exception {
        when(pedidoService.buscarPorId(eq(999L)))
                .thenThrow(new RecursoNoEncontradoException("Pedido", 999L));

        mockMvc.perform(get("/api/pedidos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("Pedido con id 999 no encontrado."))
                .andExpect(jsonPath("$.path").value("/api/pedidos/999"));
    }
}
