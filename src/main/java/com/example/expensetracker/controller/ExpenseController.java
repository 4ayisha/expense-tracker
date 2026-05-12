package com.example.expensetracker.controller;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.ExpenseService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller exposing CRUD + dashboard endpoints under /api/expenses.
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final ExpenseService service;
    private final UserRepository userRepository;

    // ✅ Constructor Injection (FIXED)
    public ExpenseController(
            ExpenseRepository expenseRepository,
            ExpenseService service,
            UserRepository userRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.service = service;
        this.userRepository = userRepository;
    }

    /** GET /api/expenses?category=Food&from=2026-01-01&to=2026-12-31 */
    @GetMapping
    public List<Expense> list(@RequestParam String email) {

    return expenseRepository.findByUserEmail(email);
    }

    /** GET /api/expenses/filter */
    @GetMapping("/filter")
    public List<Expense> getExpensesBetween(
            @RequestParam String email,
            @RequestParam String start,
            @RequestParam String end
    ) {

        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        return expenseRepository
                .findByUserEmailAndDateBetween(
                        email,
                        startDate,
                        endDate
                );
    }

    /** GET /api/expenses/{id} */
    @GetMapping("/{id}")
    public Expense getOne(@PathVariable Long id) {
        return service.findById(id);
    }

    /** POST /api/expenses */
    @PostMapping
    public Expense addExpense(@RequestBody Expense expense) {

        // ✅ Debug checks
        if (expense.getUser() == null) {
            throw new RuntimeException("User is NULL from frontend");
        }

        User user = userRepository
                .findByEmail(expense.getUser().getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        expense.setUser(user);

        return expenseRepository.save(expense);
    }

    /** PUT /api/expenses/{id} */
    @PutMapping("/{id}")
    public Expense update(
            @PathVariable Long id,
            @Valid @RequestBody Expense expense
    ) {
        return service.update(id, expense);
    }

    /** DELETE /api/expenses/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/expenses/dashboard */
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@RequestParam String email) {
        return service.dashboard(email);
}
}