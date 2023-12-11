package com.example.ebankingbackend.exceptions;

public class BalanceNotSufficient extends Exception {
    public BalanceNotSufficient(String balanceNotSifficient) {
        super(balanceNotSifficient);
    }
}
