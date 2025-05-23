package com.bruno.sistemabancario.domain.service.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Token implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private boolean authenticated;
    private Date created;
    private Date expires;
    private String accessToken;
    private String refreshToken;
}
