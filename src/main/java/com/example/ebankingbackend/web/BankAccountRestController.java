package com.example.ebankingbackend.web;

import com.example.ebankingbackend.dtos.*;
import com.example.ebankingbackend.entities.BankAccount;
import com.example.ebankingbackend.exceptions.BalanceNotSufficient;
import com.example.ebankingbackend.exceptions.BankAccountNotFound;
import com.example.ebankingbackend.service.BankService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import java.util.List;

@RestController
@Transactional
@AllArgsConstructor
@RequestMapping("/accounts")
@CrossOrigin("*")
public class BankAccountRestController {
    private BankService bankService;
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @GetMapping("/{accountId}")
    public BankAccountDTO getBankAccount(@PathVariable String accountId) throws BankAccountNotFound {
    return bankService.getBankAccount(accountId);
    }
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @GetMapping("")
    public List<BankAccountDTO> bankAccountDTOList(){
        return bankService.bankAccountList();
    }
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @GetMapping("/{accountId}/operations")
    public List<AccountOperationDTO> getHistory(@PathVariable String accountId){
        return bankService.accountHistory(accountId);
    }
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @GetMapping("/{accountId}/pageOperations")
    public AccountHistoryDTO getAccountHistory(@PathVariable String accountId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "5") int size) throws BankAccountNotFound {
        return bankService.getAccountHistory(accountId,page,size);
    }
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping("/debit")
    public DebitDTO debiter(@RequestBody DebitDTO debitDTO) throws BalanceNotSufficient, BankAccountNotFound {
        this.bankService.debit(debitDTO.getAccountId(),
                debitDTO.getAmount(),debitDTO.getDescription());
        return debitDTO;
    }
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping("/credit")
    public CreditDTO crediter(@RequestBody CreditDTO creditDTO) throws BalanceNotSufficient, BankAccountNotFound {
        this.bankService.credit(creditDTO.getAccountId(),
                creditDTO.getAmount(),creditDTO.getDescription());
        return creditDTO;
    }
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping("/transfer")
    public void transfer(@RequestBody TransferDTO transferDTO) throws BalanceNotSufficient, BankAccountNotFound {
        this.bankService.transfert(transferDTO.getAccountSource(),
                transferDTO.getAccountDestination(), transferDTO.getAmount());
    }
}
