package com.example.expensetracker.repository;

import com.example.expensetracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Expense}.
 * Provides CRUD plus a few aggregation queries used by the dashboard.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /** Filter by category and/or date range. Any param can be null. */
    @Query("""
           SELECT e FROM Expense e
           WHERE (:category IS NULL OR e.category = :category)
             AND (:from IS NULL OR e.date >= :from)
             AND (:to   IS NULL OR e.date <= :to)
           ORDER BY e.date DESC, e.id DESC
           """)
    List<Expense> search(@Param("category") String category,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to);

    /** Sum of all expenses (returns null when there are no rows). */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal sumAll();

    /** Sum of expenses within a date range (inclusive). */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.date BETWEEN :from AND :to")
    BigDecimal sumBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Per-category totals: returns rows of [category, sum]. */
    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e GROUP BY e.category")
    List<Object[]> sumByCategory();

    /** Per-date totals between two dates: returns rows of [date, sum]. */
    @Query("""
           SELECT e.date, COALESCE(SUM(e.amount), 0)
           FROM Expense e
           WHERE e.date BETWEEN :from AND :to
           GROUP BY e.date
           ORDER BY e.date
           """)
    List<Object[]> sumByDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<Expense> findByDateBetween(LocalDate start, LocalDate end);

    List<Expense> findByUserEmailAndDateBetween(String email, LocalDate start, LocalDate end);

    List<Expense> findByUserEmail(String email);
}
