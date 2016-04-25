package cp;

import java.io.File;
import java.util.List;

/**
 * This class is present only for helping you in testing your software.
 * It will be completely ignored in the evaluation.
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class main {
    public static void main(String[] args) {


        //test folder is around 800 Mbyte of lorem ipsum and other random .txt files
        /**
         *     Where you you want to find the word.
         */
// File StartingDir = new File("C:/Users/Mark/Documents/test/testfolder");
        File StartingDir = new File("C:\\Users\\mark-\\Documents\\test\\1");
        //C:\Users\mark-\Documents\test\lots of files
        /**
         * The word you want to find.
         */
        System.out.println("starting main program");
        String word = "ipsum";

        List<Result> list;
        List<String> words;
        long startTime = System.currentTimeMillis();
        list = WordFinder.findAll(word, StartingDir.toPath());
//        Stats newstarts = WordFinder.stats(StartingDir.toPath());
//        words = newstarts.wordsByOccurrences();
//        list = newstarts.foundIn(word);

//        words = WordFinder.stats(StartingDir.toPath()).words();
//        words = WordFinder.stats(StartingDir.toPath()).wordsByOccurrences();
//        Result result = WordFinder.findAny(word, StartingDir.toPath());

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println(totalTime + " ms");
        System.out.println("----------------------------------------------------");
//        System.out.println("Found result at " + result.path() + " on line " + result.line());
//        System.out.println(newstarts.leastFrequent());
//        System.out.println(newstarts.mostFrequent());
//        System.out.println(newstarts.occurrences(word));

        System.out.println("Found " + list.size() + " Results for word "+word);
//        System.out.println("found "+words.size()+" Different words in file "+StartingDir.toString());
//
//        find any test
//        Result result = WordFinder2.findAny(word, StartingDir.toPath());
//        System.out.println("Found result at " + result.path() + " on line " + result.line());
//        WordFinder.stats(StartingDir.toPath()
//        list.forEach(i -> System.out.println("Word " + word + " found in file " + i.path() + " at line " + i.line()));

    }
}
