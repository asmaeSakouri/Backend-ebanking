package com.example.ebankingbackend.dtos;

import com.example.ebankingbackend.enums.AccountStatus;
import lombok.Data;
import java.util.Date;

@Data
public class SavingAccountDTO extends BankAccountDTO {
    private String id;
    private double balance;
    private Date createdAt;
    private AccountStatus status;
    private String currency;
    private CustomerDTO customerDTO;
    private double interestRate;
}
