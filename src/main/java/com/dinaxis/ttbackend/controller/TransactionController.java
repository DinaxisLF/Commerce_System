package com.dinaxis.ttbackend.controller;

import com.dinaxis.ttbackend.model.dto.OutcomeRequestDTO;
import com.dinaxis.ttbackend.model.dto.TransactionResponseDTO;
import com.dinaxis.ttbackend.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transactions/outcomes")
    public ResponseEntity<TransactionResponseDTO> registerOutcome(@Valid @RequestBody OutcomeRequestDTO request){
        return new ResponseEntity<>(transactionService.registerOutcome(request), HttpStatus.CREATED);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String type){
        return new ResponseEntity<>(transactionService.getTransactions(from, to, type), HttpStatus.OK);
    }
}
