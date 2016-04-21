package cp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class WordFinder {
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() +1);
    private static String filetype = "txt";
    private static String lookingfor;
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
     */
    public static List<Result> findAll(String word, Path dir) {
        lookingfor = word;
        directoryCrawler(dir);
        while (threadCounter.get() != 0) {
            //wating for queue to empty
        }
        //When queue is empty there should be a few tasks still in the pool that came from the queue when shutdown is called. they will be allowed to finish.
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
        lookingfor = word;
        directoryCrawler(dir);
        while (results.isEmpty()) System.out.print("");
        //When queue is empty there should be a few tasks still in the pool that came from the queue when shutdown is called. they will be allowed to finish.
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
        return new Stats() {
            /**
             * Returns the number of times a word was found.
             * @param word the word
             * @return the number of times the word was found
             */
            @Override
            public int occurrences(String word) {
                return findAll(word, dir).size();
            }

            /**
             * Returns the list of results in which a word was found.
             * @param word the word
             * @return the list of results in which the word was found
             */

            @Override
            public List<Result> foundIn(String word) {
                return findAll(word, dir);
            }

            /**
             * Returns the word that was found the most times.
             * @return the word that was found the most times
             */

            @Override
            public String mostFrequent() {
                return null;
            }

            /**
             * Returns the word that was found the least times.
             * @return the word that was found the least times
             */

            @Override
            public String leastFrequent() {
                return null;
            }

            /**
             * Returns a list of all the words found.
             * @return a list of all the words found
             */

            @Override
            public List<String> words() {
                return null;
            }

            /**
             * Returns a list of all the words found, ordered from the least frequently occurring (first of the list)
             * to the most frequently occurring (last of the list).
             * @return a list of all the words found, ordered from the least to the most frequently occurring
             */
            @Override
            public List<String> wordsByOccurrences() {
                return null;
            }
        };

    }

    /*
        @param dir, Recursive folder crawling
        if it finds a @param filetype sends the file to filehandler.
         */
    public static void directoryCrawler(Path dir) {


        try (
                DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)
        ) {
            for (Path path : dirStream) {
//                System.out.println(path);
//                    System.out.println(path);
                if (Files.isDirectory(path)) {

                    directoryCrawler(path);

                } else if (path.toString().endsWith(filetype)) {
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
                    if (path.toFile().length() < 1000000) {

                        executor.submit(
                                () -> filehandler(path)
                        );
                    } else {
                        filehandler(path);

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void filechecker(Path path) {
        try {
            BufferedReader reader = Files.newBufferedReader(path);
            String line;
            int linenumber = 0;
            while ((line = reader.readLine()) != null) {
                linenumber += 1;

                String[] words = line.split("\\s+");
                for (String word : words) {
                    if (lookingfor.equals(word)) {

                        final int lineNumber = linenumber;
                        synchronized (results) {

                            //System.out.println("Result at " + path + " on line " + linenumber);
                            results.add(new Result() {
                                @Override
                                public Path path() {
                                    return path;
                                }

                                @Override
                                public int line() {
                                    return lineNumber;
                                }

                                {
                                }
                            });
                        }
                    }
                }
            }
        } catch (IOException e) {
        }
        threadCounter.decrementAndGet();
    }

    /*
    Handles files, Spilts the file into lines and feeds the lines to the word checkers tread
     */
    private static void filehandler(Path path) {
//        System.out.println("checking file " + path);
        try {
            BufferedReader reader = Files.newBufferedReader(path);
//            System.out.println("opening file");
            String line;
            int linenumber = 0;
            int linestothread = 1000;
            ArrayList<String> lines = new ArrayList<String>();

            while ((line = reader.readLine()) != null) {
                lines.clear();
                if (line.trim().length() > 0){lines.add(line);}

                while (lines.size() < linestothread && ((line = reader.readLine()) != null)) {
                    if (line.trim().length() > 0){lines.add(line);}
                }

                final int currentline = linenumber;
                final List<String> finallines = lines;
                threadCounter.incrementAndGet();
                executor.submit(
                        () -> wordchecker(finallines, path, currentline)
                );
                linenumber += (linestothread);
            }
        } catch (IOException e) {
        }
        threadCounter.decrementAndGet();
    }



    /*
    Checks the lines for the word @param lookingfor
     */
    private static void wordchecker(List<String> lines, Path path, int linenumber) {
//            System.out.println("running work checker");
//        System.out.println(lines.length+" at line " + linenumber+" "+lines[0]);
        for (String line : lines) {

            String[] words = line.split("\\s+");
            for (String word : words) {
                if (lookingfor.equals(word)) {
//                    System.out.println("Result at "+path+" on line "+linenumber);
                    final int finalnumber = linenumber;
                    synchronized (results) {

                        results.add(new Result() {
                            @Override
                            public Path path() {
                                return path;
                            }

                            @Override
                            public int line() {
                                return finalnumber;
                            }
                        });
                    }
                }
            }
            linenumber++;
        };
        threadCounter.decrementAndGet();
//        System.out.println(linenumber);
    }
}


