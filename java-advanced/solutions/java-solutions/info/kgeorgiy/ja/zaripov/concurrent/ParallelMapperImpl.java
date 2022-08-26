package info.kgeorgiy.ja.zaripov.concurrent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.IntStream;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

/**
 * Class that implements an {@link ParallelMapper} interface.
 *
 * @author Zaripov Ruslan (zaripovruslan864@gmail.com)
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threads;
    private final Queue<Runnable> tasks;

    private static class ConcurrentList<T> {
        private final List<T> list;
        private int size = 0;

        ConcurrentList(final int initialCapacity) {
            this.list = new ArrayList<>(Collections.nCopies(initialCapacity, null));
        }

        public void set(final int index, final T element) {
            list.set(index, element);
            check();
        }

        private synchronized void check() {
            if (++size >= list.size()) {
                notify();
            }
        }

        public synchronized List<T> getList() throws InterruptedException {
            while (size < list.size()) {
                wait();
            }
            return list;
        }
    }

    /**
     * Constructor for class {@link ParallelMapperImpl}.
     * Creates the specified number of threads.
     *
     * @param threadCount number of threads
     */
    public ParallelMapperImpl(final int threadCount) {
        this.tasks = new ArrayDeque<>();
        this.threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final Thread thread = new Thread(worker());
            threads.add(thread);
            thread.start();
        }
    }
    // :NOTE: создать один экземпляр, а не создавать каждый раз новый
    private Runnable worker() {
        return () -> {
            try {
                while (!Thread.interrupted()) {
                    final Runnable task;
                    synchronized (tasks) {
                        while (tasks.isEmpty()) {
                            tasks.wait();
                        }
                        task = tasks.poll();
                        tasks.notifyAll();
                    }
                    task.run();
                }
            } catch (final InterruptedException ignored) {
                // ignored
            } finally {
                Thread.currentThread().interrupt();
            }
        };
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *
     * @param f stateless function to apply to each element
     * @param args arguments to which the function will be applied
     * @param <T> the initial element type
     * @param <R> the new element type
     * @return list of the new elements
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(
            final Function<? super T, ? extends R> f,
            final List<? extends T> args
    ) throws InterruptedException {
        final ConcurrentList<R> result = new ConcurrentList<>(args.size());
        IntStream.range(0, args.size())
                .<Runnable>mapToObj(i -> () -> result.set(i, f.apply(args.get(i))))
                .forEach(this::addTask);
        return result.getList();
    }

    private void addTask(final Runnable task) {
        synchronized (tasks) {
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        threads.forEach(thread -> {
            thread.interrupt();
            try {
                thread.join();
            } catch (final InterruptedException ignored) {
                // ignored
            }
        });
    }
}
