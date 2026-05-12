package com.example.expensetracker.service;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> findAll(
            String category,
            LocalDate from,
            LocalDate to
    ) {
        return expenseRepository.search(category, from, to);
    }

    public Expense findById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow();
    }

    @Transactional
    public Expense create(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense update(Long id, Expense updated) {

        Expense existing = findById(id);

        existing.setTitle(updated.getTitle());
        existing.setAmount(updated.getAmount());
        existing.setCategory(updated.getCategory());
        existing.setDate(updated.getDate());
        existing.setNotes(updated.getNotes());

        return expenseRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        expenseRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> dashboard(String email) {

        List<Expense> userExpenses =
                expenseRepository.findByUserEmail(email);

        BigDecimal total = userExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = new HashMap<>();

        for (Expense e : userExpenses) {

            byCategory.merge(
                    e.getCategory(),
                    e.getAmount(),
                    BigDecimal::add
            );
        }

        Map<String, Object> result = new HashMap<>();

        result.put("totalMonth", total);
        result.put("totalAll", total);
        result.put("byCategory", byCategory);
        result.put("last7Days", List.of());

        return result;
    }
}