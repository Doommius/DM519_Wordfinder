package cp;

import sun.security.pkcs11.wrapper.Functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class WordFinder {
    private static final Map<String, Integer> wordmap = new ConcurrentHashMap<>();
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    private static ArrayList results = new ArrayList<Result>();
    private static AtomicInteger threadCounter = new AtomicInteger(0);

    /**
     * Finds all the (case-sensitive) occurrences of a word in a directory.
     * Only text files should be considered (files ending with the .txt suffix).
     * <p>
     * The word must be an exact match: it is case-sensitive and may contain punctuation.
     * See https://github.com/fmontesi/cp2016/tree/master/exam for more details.
     * <p>
     * The search is recursive: if the directory contains subdirectories,
     * these are also searched and so on so forth (until there are no more
     * subdirectories).
     *
     * @param word the word to find (does not contain whitespaces or punctuation)
     * @param dir  the directory to search
     * @return a list of results ({@link Result}), which tell where the word was found
     *
     */

    public static List<Result> findAll(String word, Path dir) {
        //Checks if the executer is running, if its not it starts one.
        if (executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        }
        //compiles the input word into a regex pattern that matches with whitespace+word+whitespace
        Pattern lookingforpattern = Pattern.compile("\\s" + word + "\\s");
        directoryCrawler(dir, lookingforpattern);
        while (threadCounter.get() != 0) {
            //wating for queue to empty
        }
        //When all tasks are finished the threads will be shut down.
        executor.shutdown();
        try {
            executor.awaitTermination(480, TimeUnit.SECONDS); //waits here until executor is terminated or the time runs out.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Finds an occurrence of a word in a directory and returns.
     * <p>
     * This method searches only for one (any) occurrence of the word in the
     * directory. As soon as one such occurrence is found, the search can be
     * stopped and the method can return immediately.
     * <p>
     * As for method {@code findAll}, the search is recursive.
     *
     * @param word
     * @param dir
     * @return
     */
    public static Result findAny(String word, Path dir) {
        if (executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        }
        Pattern lookingforpattern = Pattern.compile("\\s" + word + "\\s");
        directoryCrawler(dir, lookingforpattern);
        while (results.isEmpty())
        //When all tasks are finished the threads will be shut down.
        executor.shutdownNow();
        Result result = (Result) results.get(0);
        //System.out.println("Found result at "+result.path()+" on line "+result.line());
        return result;
    }

    /**
     * Computes overall statistics about the occurrences of words in a directory.
     * <p>
     * This method recursively searches the directory for all words and returns
     * a {@link Stats} object containing the statistics of interest. See the
     * documentation of {@link Stats}.
     *
     * @param dir the directory to search
     * @return the statistics of occurring words in the directory
     */
    public static Stats stats(Path dir) {
        if (executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        }
        Map<String, Integer> words = WordOccurrens(dir);

        return new Stats() {
            /**
             * Returns the number of times a word was found.
             *
             * @param word the word
             * @return the number of times the word was found
             */
            @Override
            public int occurrences(String word) {
                return words.get(word);
            }

            /**
             * Returns the list of results in which a word was found.
             *
             * @param word the word
             * @return the list of results in which the word was found
             */

            @Override
            public List<Result> foundIn(String word) {
                return findAll(word, dir);
            }

            /**
             * Returns the word that was found the most times.
             *
             * @return the word that was found the most times
             */

            @Override
            public String mostFrequent() {

                Map.Entry<String, Integer> maxEntry = null;
                for (Map.Entry<String, Integer> entry : words.entrySet()) {
                    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                        maxEntry = entry;
                    }
                }
                return maxEntry.toString();
            }

            /**
             * Returns the word that was found the least times.
             *
             * @return the word that was found the least times
             */

            @Override
            public String leastFrequent() {

                Map.Entry<String, Integer> minEntry = null;

                for (Map.Entry<String, Integer> entry : words.entrySet()) {
                    if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
                        minEntry = entry;
                    }
                }
                return minEntry.toString();
            }

            /**
             * Returns a list of all the words found.
             *
             * @return a list of all the words found
             */

            @Override
            public List<String> words() {

                ArrayList<String> list = new ArrayList<>();
                for (String key : words.keySet()) {
                    list.add(key);
                }
                return list;
            }

            /**
             * Returns a list of all the words found, ordered from the least frequently occurring (first of the list)
             * to the most frequently occurring (last of the list).
             *
             * @return a list of all the words found, ordered from the least to the most frequently occurring
             */
            @Override
            public List<String> wordsByOccurrences() {
                List<String> list = new ArrayList<>();
                words.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .forEach(i -> list.add(i.getKey()));
                return list;
            }

        };
    }

    /*
        @param dir, Recursive folder crawling
        if it finds a @param filetype sends the file to filehandler.
         */
    public static void directoryCrawler(Path dir, Pattern lookingforpattern) {
        try (
                DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)
        ) {
            //                System.out.println(path);
//                    System.out.println(path);
            for (Path path : dirStream)
                if (Files.isDirectory(path)) {

                    directoryCrawler(path, lookingforpattern);

                } else if (path.toString().endsWith("txt")) {
//                        System.out.println(path.toString());
//					System.out.println(path.toFile().length());
// 						System.out.println(path);
                    threadCounter.incrementAndGet();
                    /*
                    Test with spilting all files up
					 */
//					executor.submit(
//							() -> filehandler(path)
//					);
                    /*
                    Test with giving small files < 1 MB to thread that does it all.
					 */
                    if (path.toFile().length() < ((1024 * 1024) * 3)) executor.submit(
                            () -> filechecker(path, lookingforpattern)
                    );
                    else {
                        executor.submit(() ->
                                filehandler(path, lookingforpattern)
                        );

                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void filechecker(Path path, Pattern lookingforpattern) {

        try {
            Matcher match = lookingforpattern.matcher("");
            BufferedReader reader = Files.newBufferedReader(path);
            String line;
            int linenumber = 0;
            while ((line = reader.readLine()) != null) {
                if (line != null) {
                    // System.out.println("1 " + line);
                    match.reset(line);
                    while (match.find()) {
//                        System.out.println(match.group(0));
                        synchronized (results) {
                            //System.out.println(results.size());
//                            System.out.println("Result at " + path + " on line " + linenumbers);
                            final int finallinenumber = linenumber;
                            results.add(new Result() {
                                @Override
                                public Path path() {
                                    return path;
                                }

                                @Override
                                public int line() {
                                    return finallinenumber;
                                }
                            });
                        }


                    }

                }
                linenumber++;
            }
        } catch (IOException e) {
        }
        threadCounter.decrementAndGet();
    }

    /*
    Handles files, Spilts the file into lines and feeds the lines to the word checkers tread
     */
    private static void filehandler(Path path, Pattern lookingforpattern) {
//            System.out.println("running file halder");
//        System.out.println("checking file " + path);

        try {
            BufferedReader reader = Files.newBufferedReader(path);
            int linestothread = 1000;
            int linenumber = 0;
            int n;
//            String[] lines = new String[linestothread];
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lines = new String[linestothread];
                if (line.trim().length() > 0) {
                    lines[0] = line;
                }
                n = 1;
                while (n < linestothread && ((line = reader.readLine()) != null)) {
                    if (line.trim().length() > 1) {
                        //System.out.println("adding line to file "+path+" *"+line+"*");
                        lines[n] = line;
                        n++;
                    }
                }
                linenumber += 1;
//                    System.out.println(linenumber);
                final int finalline = linenumber;
                final String[] currentLines = lines;
                threadCounter.incrementAndGet();
                executor.submit(
                        () -> wordchecker(currentLines, path, finalline, lookingforpattern)
                );
            }
        } catch (IOException e) {
        }
        threadCounter.decrementAndGet();
    }

    /*
    Checks the lines for the word @param lookingfor
     */
    private static void wordchecker(String[] lines, Path path, int linenumbers, Pattern lookingforpattern) {
        Matcher match = lookingforpattern.matcher("");
        for (String line : lines) {
            if (line != null) {
                match.reset(line);
                while (match.find()) {
                    synchronized (results) {
                        final int linenuber = linenumbers;
                        results.add(new Result() {
                            @Override
                            public Path path() {
                                return path;
                            }

                            @Override
                            public int line() {
                                return linenuber;
                            }
                        });
                    }
                }
            }
            linenumbers++;
        }

        threadCounter.decrementAndGet();
    }

    private static Map<String, Integer> WordOccurrens(Path dir) {
//        System.out.println("making map");
        WordOccurrenDirectoryCrawler(dir);
        while (threadCounter.get() != 0) {
            //wating for queue to empty
        }
        //When all tasks are finished the threads will be shut down.
        executor.shutdown();
        try {
            executor.awaitTermination(480, TimeUnit.SECONDS); //waits here until executor is terminated or the time runs out.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return wordmap;


    }

    private static void WordOccurrenDirectoryCrawler(Path dir) {
//        System.out.println(dir);
        try (
                DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)
        ) {
            for (Path path : dirStream)
                if (Files.isDirectory(path)) {
                    WordOccurrenDirectoryCrawler(path);
                } else if (path.toString().endsWith("txt")) {
                    threadCounter.incrementAndGet();
                    if (path.toFile().length() < ((1024 * 1024) * 3)) executor.submit(
                            () -> WordOccurrencesfileChecker(path)
                    );
                    else {
                        WordOccurrencesfilehandler(path);
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void WordOccurrencesfileChecker(Path path) {
        try (
                BufferedReader reader = Files.newBufferedReader(path);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                WordOccurrencescounter(words);
            }
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("There was a problem with file " + path);
        }
        threadCounter.decrementAndGet();
    }

    private static void WordOccurrencesfilehandler(Path path) {
        try {
            BufferedReader reader = Files.newBufferedReader(path);
            int linestothread = 1000;
            int n;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lines = new String[linestothread];
                if (line.trim().length() > 0) {
                    lines[0] = line;
                }
                n = 1;
                while (n < linestothread && ((line = reader.readLine()) != null)) {
                    if (line.trim().length() > 1) {
                        lines[n] = line;
                        n++;
                    }
                }
                final String[] currentLines = lines;
                threadCounter.incrementAndGet();
                executor.submit(
                        () -> WordOccurrencescounterlist(currentLines)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadCounter.decrementAndGet();
    }

    private static void WordOccurrencescounterlist(String[] lines) {
        for (String line : lines) {
            if (line != null) {
                String[] words = line.split("\\s+");
                WordOccurrencescounter(words);

            }
        }
        threadCounter.decrementAndGet();
    }

    private static void WordOccurrencescounter(String[] words) {
        for (String word : words) {
            wordmap.compute(word, (k, v) -> {
                if (v == null) {
                    return 1;
                } else {
                    return v + 1;
                }
            });
        }
    }

}
