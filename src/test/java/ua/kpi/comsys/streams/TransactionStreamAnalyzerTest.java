package ua.kpi.comsys.streams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class TransactionStreamAnalyzerTest {
    private TransactionStreamAnalyzer a;
    @BeforeEach
    void setUp() {
        a = new TransactionStreamAnalyzer();
    }

    @Test
    void testExceptions() {
        assertThrows(IllegalArgumentException.class, () -> a.findFirstHighValueTransaction(null, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> a.takeTransactionsUntilBudgetExceeded(null, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> a.processBatches(null, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> a.processBatches(Stream.empty(), 0, 1));
        assertThrows(IllegalArgumentException.class, () -> a.processBatches(Stream.empty(), 1, -5));
    }

    @Test
    @Timeout(2)
    void findFirst_Inf() {
        Stream<Transaction> s = Stream.generate(() ->
                new Transaction("1", "A", new BigDecimal("50.0"), LocalDateTime.now()));
        Optional<Transaction> r = a.findFirstHighValueTransaction(s, new BigDecimal("40.0"));
        assertTrue(r.isPresent());
        assertEquals(new BigDecimal("50.0"), r.get().amount());
    }

    @Test
    void findFirst_NotFound() {
        Stream<Transaction> s = Stream.of(
                new Transaction("1", "A", new BigDecimal("10.0"), LocalDateTime.now()),
                new Transaction("2", "B", new BigDecimal("20.0"), LocalDateTime.now())
        );
        Optional<Transaction> r = a.findFirstHighValueTransaction(s, new BigDecimal("100.0"));
        assertFalse(r.isPresent());
    }

    @Test
    @Timeout(2)
    void takeUntil_Inf() {
        Stream<Transaction> s = Stream.iterate(
                new Transaction("1", "A", new BigDecimal("10"), LocalDateTime.now()),
                t -> new Transaction(t.transactionId(), t.category(), t.amount().add(BigDecimal.TEN), LocalDateTime.now())
        );
        List<Transaction> r = a.takeTransactionsUntilBudgetExceeded(s, new BigDecimal("60"));
        assertEquals(3, r.size());
        assertEquals(new BigDecimal("30"), r.get(2).amount());
    }

    @Test
    @Timeout(2)
    void processBatches_Inf() {
        Stream<Transaction> s = Stream.generate(() ->
                new Transaction("1", "A", new BigDecimal("5"), LocalDateTime.now()));
        List<List<Transaction>> b = a.processBatches(s, 3, 2);
        assertEquals(2, b.size());
        assertEquals(3, b.get(0).size());
        assertEquals(3, b.get(1).size());
    }

    @Test
    void processBatches_Fin() {
        Stream<Transaction> s = Stream.of(
                new Transaction("1", "A", BigDecimal.ONE, LocalDateTime.now()),
                new Transaction("2", "A", BigDecimal.ONE, LocalDateTime.now()),
                new Transaction("3", "A", BigDecimal.ONE, LocalDateTime.now()),
                new Transaction("4", "A", BigDecimal.ONE, LocalDateTime.now())
        );
        List<List<Transaction>> b = a.processBatches(s, 3, 5);
        assertEquals(2, b.size());
        assertEquals(3, b.get(0).size());
        assertEquals(1, b.get(1).size());
    }
}