package info.kgeorgiy.ja.zaripov.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

/**
 * Scalar iterative parallelism support.
 *
 * @author Ruslan Zaripov (zaripovruslan864@gmail.com)
 */
public class IterativeParallelism implements ScalarIP {
    private final ParallelMapper parallelMapper;

    /**
     * Empty constructor for class {@link IterativeParallelism}.
     */
    public IterativeParallelism() {
        this(null);
    }

    /**
     * Constructor for class {@link IterativeParallelism}.
     * Initialise mapper which implements {@link ParallelMapper} interface.
     *
     * @param mapper specified object
     */
    public IterativeParallelism(final ParallelMapper mapper) {
        this.parallelMapper = mapper;
    }

    /**
     * Returns maximum value.
     *
     * @param <T> value type
     *
     * @param threadCount number or concurrent threads
     * @param values values to get maximum of
     * @param comparator value comparator
     * @return maximum of given values
     *
     * @throws InterruptedException if executing thread was interrupted
     * @throws java.util.NoSuchElementException if no values are given
     */
    @Override
    public <T> T maximum(
            final int threadCount,
            final List<? extends T> values,
            final Comparator<? super T> comparator
    ) throws InterruptedException {
        final Function<List<? extends T>, ? extends T> getMaximum = list ->
                list.stream()
                        .max(comparator)
                        .orElse(null);
        return process(threadCount, values, getMaximum, getMaximum);
    }

    /**
     * Returns minimum value. Simply calls
     * {@link #maximum(int, List, Comparator)}
     * with reversed comparator.
     *
     * @param threadCount number or concurrent threads
     * @param values values to get minimum of
     * @param comparator value comparator
     * @param <T> value type
     *
     * @return minimum of given values
     *
     * @throws InterruptedException if executing thread was interrupted
     * @throws java.util.NoSuchElementException if no values are given
     */
    @Override
    public <T> T minimum(
            final int threadCount,
            final List<? extends T> values,
            final Comparator<? super T> comparator
    ) throws InterruptedException {
        return maximum(threadCount, values, comparator.reversed());
    }

    /**
     * Returns whether all values satisfy predicate.
     *
     * @param threadCount number or concurrent threads
     * @param values values to test
     * @param predicate test predicate
     * @param <T> value type
     *
     * @return whether all values satisfy predicate
     *         or {@code true}, if no values are given
     *
     * @throws InterruptedException if executing thread was interrupted
     */
    @Override
    public <T> boolean all(
            final int threadCount,
            final List<? extends T> values,
            final Predicate<? super T> predicate
    ) throws InterruptedException {
        return process(
                threadCount,
                values,
                list -> list.stream().allMatch(predicate),
                list -> list.stream().allMatch(Boolean::booleanValue)
        );
    }

    /**
     * Returns whether any of values satisfy predicate.
     * Simply negates {@link #all(int, List, Predicate)}
     * with reversed comparator result.
     *
     * @param threadCount number or concurrent threads
     * @param values values to test
     * @param predicate test predicate
     * @param <T> value type
     *
     * @return whether any value satisfies predicate
     *         or {@code false}, if no values are given
     *
     * @throws InterruptedException if executing thread was interrupted
     */
    @Override
    public <T> boolean any(
            final int threadCount,
            final List<? extends T> values,
            final Predicate<? super T> predicate
    ) throws InterruptedException {
        return !all(threadCount, values, predicate.negate());
    }

    private <T> List<List<T>> split(
            final int threadCount,
            final List<T> values
    ) {
        final int size = values.size();

        if (threadCount > size) {
            return values.stream().map(List::of).collect(Collectors.toList());
        }

        final int partSize = size / threadCount;
        final List<List<T>> parts = new ArrayList<>();

        int tailSize = size % threadCount;
        int right = 0;
        for (int i = 0; i < threadCount; i++) {
            final int left = right;
            right = left + partSize;
            if (tailSize > 0) {
                right += 1;
                tailSize -= 1;
            }
            parts.add(values.subList(left, right));
        }

        return parts;
    }

    private <T, U> U process(
            final int threadCount, final List<? extends T> values,
            final Function<List<? extends T>, ? extends U> reducePart,
            final Function<List<? extends U>, ? extends U> reduceResults
    ) throws InterruptedException {
        if (values.isEmpty()) {
            throw new NoSuchElementException();
        }

        final var parts = split(threadCount, values);
        final int partCount = parts.size();
        if (Objects.nonNull(parallelMapper)) {
            return reduceResults.apply(parallelMapper.map(reducePart, parts));
        }

        final List<U> results = new ArrayList<>(Collections.nCopies(partCount, null));
        final var threads = IntStream.range(0, partCount)
                .mapToObj(i -> {
                    final Thread thread = new Thread(
                            () -> results.set(i, reducePart.apply(parts.get(i)))
                    );
                    thread.start();
                    return thread;
                })
                .collect(Collectors.toList());

        for (Thread thread : threads) {
            thread.join();
        }

        return reduceResults.apply(results);
    }
}
