package com.manish.b2bmonitor.api;

import com.manish.b2bmonitor.domain.TransactionRecord;
import com.manish.b2bmonitor.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionRecord ingest(@Valid @RequestBody IngestTransactionRequest request) {
        return service.ingest(request);
    }

    @GetMapping("/{id}")
    public TransactionRecord get(@PathVariable String id) {
        return service.get(id);
    }

    @GetMapping
    public List<TransactionRecord> list() {
        return service.list();
    }

    @GetMapping("/stats")
    public TransactionStats stats() {
        return service.stats();
    }
}
