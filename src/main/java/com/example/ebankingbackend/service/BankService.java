package com.example.ebankingbackend.service;

import com.example.ebankingbackend.dtos.*;
import com.example.ebankingbackend.entities.*;
import com.example.ebankingbackend.enums.AccountStatus;
import com.example.ebankingbackend.enums.Operationtype;
import com.example.ebankingbackend.exceptions.BalanceNotSufficient;
import com.example.ebankingbackend.exceptions.BankAccountNotFound;
import com.example.ebankingbackend.exceptions.CustomerNotFoundException;
import com.example.ebankingbackend.mappers.BankAccountMapper;
import com.example.ebankingbackend.repositories.AccountOperationRepository;
import com.example.ebankingbackend.repositories.BankAccountRepository;
import com.example.ebankingbackend.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankService implements IBankService {
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private CustomerRepository customerRepository;
    private BankAccountMapper mapper;
    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("saving new customer");
        Customer customer=mapper.fromCustomerDTO(customerDTO);
        Customer saveCustomer = customerRepository.save(customer);
        return mapper.fromCustomer(saveCustomer);
    }
@Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("updating customer");
        Customer customer=mapper.fromCustomerDTO(customerDTO);
        Customer saveCustomer = customerRepository.save(customer);
        return mapper.fromCustomer(saveCustomer);
    }
    @Override
public void deleteCustomer(Long customerId){
        customerRepository.deleteById(customerId);
}
    @Override
    public CurrentAccountDTO saveCurrentAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) throw new CustomerNotFoundException("customer not found");
        CurrentAccount bankAccount = new CurrentAccount();
        bankAccount.setId(UUID.randomUUID().toString());
        bankAccount.setCurrency("£");
        bankAccount.setBalance(initialBalance);
        bankAccount.setCreatedAt(new Date());
        bankAccount.setCustomer(customer);
        bankAccount.setStatus(AccountStatus.CREATED);
        bankAccount.setOverDraft(overDraft);
        bankAccountRepository.save(bankAccount);
        return mapper.fromCurrentAccount(bankAccount);
    }
    @Override
    public SavingAccountDTO saveSavingAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null)  throw new CustomerNotFoundException("customer not found");
        SavingAccount bankAccount = new SavingAccount ();
        bankAccount.setId(UUID.randomUUID().toString());
        bankAccount.setCurrency("£");
        bankAccount.setBalance(initialBalance);
        bankAccount.setCreatedAt(new Date());
        bankAccount.setCustomer(customer);
        bankAccount.setStatus(AccountStatus.CREATED);
        bankAccount.setInterestRate(interestRate);
        bankAccountRepository.save(bankAccount);
        return mapper.fromSavingAccount(bankAccount);
    }

    @Override
    public List<CustomerDTO> listCostumers() {
        List<Customer> customers=customerRepository.findAll();
       List<CustomerDTO> customerDTOS=
               customers.stream()
                       .map(customer -> mapper.fromCustomer(customer)).collect(Collectors.toList());
        return customerDTOS ;
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFound {
        BankAccount bankAccount=bankAccountRepository.
                findById(accountId)
                .orElseThrow(()->new BankAccountNotFound("bank account not found"));
        if (bankAccount instanceof SavingAccount){
            SavingAccount savingAccount = (SavingAccount) bankAccount;
            return mapper.fromSavingAccount(savingAccount);
        }else {
            CurrentAccount currentAccount = (CurrentAccount) bankAccount;
            return mapper.fromCurrentAccount(currentAccount);
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFound, BalanceNotSufficient {
        BankAccount bankAccount=bankAccountRepository.
                findById(accountId)
                .orElseThrow(()->new BankAccountNotFound("bank account not found"));        if(bankAccount.getBalance()<amount)
            throw new BalanceNotSufficient("balance not sifficient");
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setOperationtype(Operationtype.DEBIT);
        accountOperation.setOperationDate(new Date());
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()-amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFound {
        BankAccount bankAccount=bankAccountRepository.
                findById(accountId)
                .orElseThrow(()->new BankAccountNotFound("bank account not found"));        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setOperationtype(Operationtype.CREDIT);
        accountOperation.setOperationDate(new Date());
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()+amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfert(String accountSourceId, String accountDestinationId, double amount) throws BalanceNotSufficient, BankAccountNotFound {
        debit(accountSourceId,amount, "virement to "+accountDestinationId);
        credit(accountDestinationId,amount,"virement from "+accountSourceId);

    }
    @Override
    public List<BankAccountDTO> bankAccountList(){
        List<BankAccount> bankAccounts=bankAccountRepository.findAll();
        List<BankAccountDTO> collect = bankAccounts.stream().map(acc -> {
            if (acc instanceof SavingAccount) {
                SavingAccount savingAccount = (SavingAccount) acc;
                return mapper.fromSavingAccount(savingAccount);
            } else {
                CurrentAccount currentAccount = (CurrentAccount) acc;
                return mapper.fromCurrentAccount(currentAccount);
            }
        }).collect(Collectors.toList());
        return collect;
    }
    @Override
    public  CustomerDTO getCustomer(Long id) throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(id).orElseThrow(()->new CustomerNotFoundException("customer not found"));
        return mapper.fromCustomer(customer);
    }
    @Override
    public List<AccountOperationDTO> accountHistory(String accountId){
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
       return accountOperations.stream().map(op->mapper.fromAccountOperation(op)).collect(Collectors.toList());
    }
    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFound {
       BankAccount bankAccount=bankAccountRepository.findById(accountId).orElseThrow(()->new BankAccountNotFound("account not found"));
        Page<AccountOperation> accountOperationPage = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO=new AccountHistoryDTO();
        List<AccountOperationDTO> operationDTOS = accountOperationPage.getContent().stream().map(op -> mapper.fromAccountOperation(op)).collect(Collectors.toList());
        accountHistoryDTO.setOperationDTOList(operationDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setTotalPages(accountOperationPage.getTotalPages());
    return accountHistoryDTO;
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        List<Customer> customers=customerRepository.searchCustomer(keyword);
        List<CustomerDTO> customerDTOS = customers.stream().map(customer -> mapper.fromCustomer(customer)).collect(Collectors.toList());
        return customerDTOS;
    }

}
