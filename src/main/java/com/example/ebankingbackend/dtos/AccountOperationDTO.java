package com.example.ebankingbackend.dtos;

import com.example.ebankingbackend.enums.Operationtype;
import lombok.Data;
import java.util.Date;
@Data
public class AccountOperationDTO {
    private Long id;
    private Date operationDate;
    private double amount;
    private Operationtype operationtype;
    private String description;

}
