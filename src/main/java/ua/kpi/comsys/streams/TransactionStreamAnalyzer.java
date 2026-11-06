package ua.kpi.comsys.streams;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class TransactionStreamAnalyzer {

    public Optional<Transaction> findFirstHighValueTransaction(Stream<Transaction> stream, BigDecimal threshold) {
        if (stream == null || threshold == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        return stream
                .filter(t -> t.amount().compareTo(threshold) > 0)
                .findFirst();
    }

    public List<Transaction> takeTransactionsUntilBudgetExceeded(Stream<Transaction> stream, BigDecimal maxBudget) {
        if (stream == null || maxBudget == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        BigDecimal[] currentSum = {BigDecimal.ZERO};

        return stream.takeWhile(t -> {
            currentSum[0] = currentSum[0].add(t.amount());
            return currentSum[0].compareTo(maxBudget) <= 0;
        }).toList();
    }

    public List<List<Transaction>> processBatches(Stream<Transaction> stream, int batchSize, int maxBatches) {
        if (stream == null) {
            throw new IllegalArgumentException("Stream cannot be null");
        }
        if (batchSize <= 0 || maxBatches <= 0) {
            throw new IllegalArgumentException("batchSize and maxBatches must be greater than 0");
        }

        Gatherer<Transaction, List<Transaction>, List<Transaction>> customBatchGatherer = Gatherer.ofSequential(
                () -> new ArrayList<>(),
                (state, element, downstream) -> {
                    state.add(element);
                    if (state.size() == batchSize) {
                        boolean proceed = downstream.push(List.copyOf(state));
                        state.clear();
                        return proceed;
                    }
                    return true;
                },
                (state, downstream) -> {
                    if (!state.isEmpty()) {
                        downstream.push(List.copyOf(state));
                    }
                }
        );

        return stream
                .gather(customBatchGatherer)
                .limit(maxBatches)
                .toList();
    }
}