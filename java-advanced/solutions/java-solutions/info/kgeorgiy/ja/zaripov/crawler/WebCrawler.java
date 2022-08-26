package info.kgeorgiy.ja.zaripov.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class WebCrawler implements Crawler {
    private static final int DEFAULT = 1;
    private static final int MAX_ARGS_LENGTH = 5;
    private static final int EXECUTOR_SERVICE_TERMINATION_TIMEOUT = 800;

    private final Downloader downloader;
    private final ExecutorService extractorService;
    private final ExecutorService downloaderService;
    private final int perHost;

    /**
     * Class {@link WebCrawler} constructor.
     *
     * @param downloader        allows downloading pages and extracting
     *                          links from them
     * @param downloaderCount   maximum number of simultaneously
     *                          loaded pages
     * @param extractorCount    maximum number of pages from which
     *                          links are simultaneously retrieved
     * @param perHost           the maximum number of pages that
     *                          can be simultaneously downloaded from
     *                          one host
     */
    public WebCrawler(
            final Downloader downloader,
            final int downloaderCount,
            final int extractorCount,
            final int perHost
    ) {
        this.downloader = downloader;
        this.downloaderService = Executors.newFixedThreadPool(downloaderCount);
        this.extractorService = Executors.newFixedThreadPool(extractorCount);
        this.perHost = perHost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Result download(final String url, final int depth) {
        return new ParallelDownloader(url, depth).getResult();
    }

    private final class ParallelDownloader {
        private final Set<String> processed = ConcurrentHashMap.newKeySet();
        private final Map<String, IOException> errors = new ConcurrentHashMap<>();
        private final Queue<String> buffer = new ConcurrentLinkedQueue<>();
        private final Map<String, Semaphore> limitPerHost = new ConcurrentHashMap<>();
        private final Phaser phaser = new Phaser(1);

        private ParallelDownloader(final String rootUrl, final int depth) {
            Queue<String> urls = new ConcurrentLinkedQueue<>();
            urls.add(rootUrl);

            processed.add(rootUrl);
            for (int i = depth; i > 0; i--) {
                while (!urls.isEmpty()) {
                    process(urls.poll(), i);
                }
                phaser.arriveAndAwaitAdvance();

                urls.addAll(buffer);
                buffer.clear();
            }
        }

        private void process(final String url, final int depth) {
            phaser.register();
            downloaderService.submit(() -> {
                try {
                    String hostName = URLUtils.getHost(url);
                    limitPerHost.putIfAbsent(hostName, new Semaphore(perHost));
                    try {
                        limitPerHost.get(hostName).acquire();
                        extractor(downloader.download(url), url, depth);
                    } finally {
                        limitPerHost.get(hostName).release();
                    }
                } catch (final IOException e) {
                    errors.put(url, e);
                } catch (final InterruptedException ignored) {
                    // ignored
                } finally {
                    phaser.arriveAndDeregister();
                }
            });
        }

        private void extractor(
                final Document document,
                final String url,
                final int depth
        ) {
            if (depth == 1) {
                return;
            }
            phaser.register();
            extractorService.submit(() -> {
                try {
                    document.extractLinks().forEach(link -> {
                        if (processed.add(link)) {
                            buffer.add(link);
                        }
                    });
                } catch (final IOException e) {
                    errors.put(url, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            });
        }

        private Result getResult() {
            processed.removeAll(errors.keySet());
            return new Result(new ArrayList<>(processed), errors);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        close(downloaderService);
        close(extractorService);
    }

    private void close(final ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(
                    EXECUTOR_SERVICE_TERMINATION_TIMEOUT,
                    TimeUnit.MILLISECONDS
                )
            ) {
                executorService.shutdownNow();
            }
        } catch (final InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    private static final BiFunction<String[], Integer, Integer> parseValueFunction = (arguments, index) -> {
        try {
            return arguments.length > index ? Integer.parseInt(arguments[index]) : DEFAULT;
        } catch (NumberFormatException e) {
            throw new NumberFormatException(
                    String.format("Cannot parse input argument: <%s>%n", arguments[index]) + e.getMessage()
            );
        }
    };

    /**
     * Class {@link WebCrawler} main method.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        if (!check(args)) {
            printUsage();
            return;
        }

        try (Crawler crawler = new WebCrawler(
                new CachingDownloader(),
                parseValueFunction.apply(args, 2),
                parseValueFunction.apply(args, 3),
                parseValueFunction.apply(args, 4)
            )
        ) {
            crawler.download(args[0], parseValueFunction.apply(args, 1));
        } catch (final NumberFormatException e) {
            System.err.println(e.getMessage());
            printUsage();
        } catch (final IOException e) {
            System.err.println("IOException occurred in CachingDownloader: " + e.getMessage());
        }
    }

    private static boolean check(final String[] args) {
        return !Objects.isNull(args)
                && Arrays.stream(args).noneMatch(Objects::isNull)
                && args.length >= 1
                && args.length <= MAX_ARGS_LENGTH;
    }

    private static void printUsage() {
        System.err.println("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
    }
}
