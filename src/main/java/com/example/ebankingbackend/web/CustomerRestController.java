package com.example.ebankingbackend.web;

import com.example.ebankingbackend.dtos.CustomerDTO;
import com.example.ebankingbackend.entities.Customer;
import com.example.ebankingbackend.exceptions.CustomerNotFoundException;
import com.example.ebankingbackend.service.IBankService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/customers")
@CrossOrigin("*")
// webservice restful-> API
@Slf4j
public class CustomerRestController {
    private IBankService bankService;
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @GetMapping("/search")
    public List<CustomerDTO> SearchCustomers(@RequestParam(defaultValue = "") String keyword){

        return bankService.searchCustomers("%"+keyword+"%");
    }
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @GetMapping("")
    public List<CustomerDTO> customers(){

        return bankService.listCostumers();
    }
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @GetMapping("/{id}")
    public CustomerDTO getCustomer(@PathVariable(name = "id") Long customerId) throws CustomerNotFoundException {
       return bankService.getCustomer(customerId);


    }
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping("")
    public CustomerDTO saveCustomer(@RequestBody CustomerDTO req){
        return bankService.saveCustomer(req);
    }
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping("/{customerId}")
    public CustomerDTO updateCustomer(@PathVariable Long customerId,
                                     @RequestBody CustomerDTO customerDTO){
        customerDTO.setId(customerId);
        return bankService.updateCustomer(customerDTO);
    }
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{customerId}")
    public void deleteCustomer(@PathVariable Long customerId){
        bankService.deleteCustomer(customerId);

    }


}
