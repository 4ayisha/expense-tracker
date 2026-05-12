
package com.example.expensetracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.expensetracker.model.User;

/**
 * JPA entity representing a single expense record.
 * Stored in the H2 database table {@code expenses}.
 */
@Entity
@Table(name = "expenses")
public class Expense {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short title / description of what was bought. Required. */
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    @Column(nullable = false, length = 100)
    private String title;

    /** Amount spent. Must be strictly positive. */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimals")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** Category — one of: Food, Transport, Bills, Entertainment, Shopping, Other. */
    @NotBlank(message = "Category is required")
    @Pattern(
        regexp = "Food|Transport|Bills|Entertainment|Shopping|Other",
        message = "Category must be one of: Food, Transport, Bills, Entertainment, Shopping, Other"
    )
    @Column(nullable = false, length = 32)
    private String category;

    /** Date the expense occurred. Cannot be far in the future. */
    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate date;

    /** Optional free-text notes. */
    @Size(max = 500, message = "Notes must be at most 500 characters")
    @Column(length = 500)
    private String notes;

    /** Server-side creation timestamp (set automatically). */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 🔐 Link expense to a user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    // ===== Constructors =====
    public Expense() {}

    public Expense(String title, BigDecimal amount, String category, LocalDate date, String notes) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.notes = notes;
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}
}
