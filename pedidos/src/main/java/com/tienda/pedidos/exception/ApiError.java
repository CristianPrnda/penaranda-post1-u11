package com.tienda.pedidos.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class ApiError {

    @Schema(description = "Código de estado HTTP", example = "404")
    private int status;

    @Schema(description = "Razón o nombre del error HTTP", example = "Not Found")
    private String error;

    @Schema(description = "Mensaje detallado del error o motivo de validación", example = "Pedido con id 999 no encontrado.")
    private String mensaje;

    @Schema(description = "Marca de tiempo del error", example = "2026-10-29T10:45:00.000")
    private String timestamp;

    @Schema(description = "Ruta URI invocada", example = "/api/pedidos/999")
    private String path;

    public ApiError() {
    }

    public ApiError(int status, String error, String mensaje, String path) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now().toString();
        this.path = path;
    }

    public ApiError(int status, String error, String mensaje, String timestamp, String path) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.timestamp = timestamp;
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
