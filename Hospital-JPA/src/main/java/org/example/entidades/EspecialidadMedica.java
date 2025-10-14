package org.example.entidades;

public enum EspecialidadMedica {
    CARDIOLOGIA,
    NEUROLOGIA,
    PEDIATRIA,
    TRAUMATOLOGIA,
    GINECOLOGIA,
    UROLOGIA,
    OFTALMOLOGIA,
    DERMATOLOGIA,
    PSIQUIATRIA,
    MEDICINA_GENERAL,
    CIRUGIA_GENERAL,
    ANESTESIOLOGIA;

    public String getDescripcion() { return name(); }
}