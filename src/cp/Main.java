package cp;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class is present only for helping you in testing your software.
 * It will be completely ignored in the evaluation.
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Main {
    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        //test folder is around 800 Mbyte of lorem ipsum and other random .txt files
        /**
        *     Where you you want to find the word.
        */
// File StartingDir = new File("C:/Users/Mark/Documents/test/testfolder");
        File StartingDir = new File("C:/Users/Mark/OneDrive/SDU/testfolder");
        		/**
		* The word you want to find.
        */
        String word = "ipsum";

//        find all
        List<Result> list = null;

        list = WordFinder.findAll(word,StartingDir.toPath());
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println(totalTime + " ms");
        System.out.println("Found " + list.size() + " Results");

//
//        find any test
//		Result result = WordFinder2.findAny(word,StartingDir.toPath());
//		System.out.println("Found result at "+result.path()+" on line "+result.line());
        //WordFinder.stats(StartingDir.toPath());

//        list.forEach(i -> System.out.println("Word "+lookingfor+" found in file " + i.path()+ " at line "+i.line()));

    }
}
