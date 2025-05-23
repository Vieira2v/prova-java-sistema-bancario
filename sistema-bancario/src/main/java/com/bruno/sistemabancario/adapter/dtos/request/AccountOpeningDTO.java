package com.bruno.sistemabancario.adapter.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AccountOpeningDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "CPF is required")
    @Pattern(regexp = "\\d{11}", message = "CPF must contain 11 digits")
    private String cpf;
}
