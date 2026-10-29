package com.tienda.pedidos.controller;

import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO crear(@Valid @RequestBody PedidoRequestDTO dto) {
        return service.crear(dto);
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO obtener(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
}
