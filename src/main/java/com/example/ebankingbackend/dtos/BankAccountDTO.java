package com.example.ebankingbackend.dtos;

import lombok.Data;

@Data
public class BankAccountDTO {
    //pour connaitre de quel type de compte s'agit il depuis Api pour la partie UI
    private String type;
}
