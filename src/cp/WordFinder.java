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
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class WordFinder
{
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
	private static String filetype = "txt";
	private static String lookingfor;
	private static ArrayList results = new ArrayList<Result>();
	private static AtomicInteger threadCounter = new AtomicInteger( 0 );

	/**
	 * Finds all the (case-sensitive) occurrences of a word in a directory.
	 * Only text files should be considered (files ending with the .txt suffix).
	 *
	 * The word must be an exact match: it is case-sensitive and may contain punctuation.
	 * See https://github.com/fmontesi/cp2016/tree/master/exam for more details.
	 *
	 * The search is recursive: if the directory contains subdirectories,
	 * these are also searched and so on so forth (until there are no more
	 * subdirectories).
	 *
	 * @param word the word to find (does not contain whitespaces or punctuation)
	 * @param dir the directory to search
	 * @return a list of results ({@link Result}), which tell where the word was found
	 */
	public static List< Result > findAll( String word, Path dir )
	{
		lookingfor = word;
		directoryCrawler(dir);
		while(threadCounter.get() != 0){
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
	 *
	 * This method searches only for one (any) occurrence of the word in the
	 * directory. As soon as one such occurrence is found, the search can be
	 * stopped and the method can return immediately.
	 *
	 * As for method {@code findAll}, the search is recursive.
	 *
	 * @param word
	 * @param dir
	 * @return
	 */
	public static Result findAny( String word, Path dir )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Computes overall statistics about the occurrences of words in a directory.
	 *
	 * This method recursively searches the directory for all words and returns
	 * a {@link Stats} object containing the statistics of interest. See the
	 * documentation of {@link Stats}.
	 *
	 * @param dir the directory to search
	 * @return the statistics of occurring words in the directory
	 */
	public static Stats stats( Path dir )
	{
		throw new UnsupportedOperationException();
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
					threadCounter.incrementAndGet();
					executor.submit(
							() -> filehandler(path)
					);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
    Handles files, Spilts the file into lines and feeds the lines to the word checkers tread
     */
	private static void filehandler(Path path) {
//            System.out.println("running file halder");
//        System.out.println("checking file " + path);
		try {
			BufferedReader reader = Files.newBufferedReader(path);
//            System.out.println("opening file");
			String line;
			int linenumber = 0;
			while ((line = reader.readLine()) != null) {
				linenumber += 1;
//                    System.out.println(linenumber);
				final int finalline = linenumber;
				final String currentLine = line;
				threadCounter.incrementAndGet();
				executor.submit(
						() -> wordchecker(currentLine, path, finalline)
				);

//                    executor.shutdown();
//                    executor.awaitTermination( 1, TimeUnit.MINUTES );

			}

		} catch (IOException e) {

		}

		threadCounter.decrementAndGet();
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
                        if (lookingfor.equals(word)) synchronized (results) {
                            final int lineNumber = linenumber
                            System.out.println("Result at " + path + " on line " + linenumber);
                            results.add(new Result() {
                                @Override
                                public Path path() {
                                    return path;
                                }
                                @Override
                                public int line() {
                                    return lineNumber;
                                }
                            });
                        }                                    }            }
        } catch (IOException e) {
        }
        threadCounter.decrementAndGet();
    }
	/*
    Checks the lines for the word @param lookingfor
     */
	private static void wordchecker(String line, Path path, int linenumber) {
//            System.out.println("running work checker");
//            System.out.println(line);
		{
			String[] words = line.split("\\s+");
			for (String word : words) {
				if (lookingfor.equals(word)) synchronized (results) {
//                    System.out.println(results.size());
//                    System.out.println("Result at "+path+" on line "+linenumber);
					results.add(new Result() {
						@Override
						public Path path() {
							return path;
						}

						@Override
						public int line() {
							return linenumber;
						}
					});

//                        executor.shutdownNow();


				}
			}


		}
		threadCounter.decrementAndGet();
	}
}


