package cp;

import java.io.File;
import java.util.List;

/**
 * This class is present only for helping you in testing your software.
 * It will be completely ignored in the evaluation.
 * 
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Main
{
	public static void main( String[] args )
	{

		long startTime = System.currentTimeMillis();
		//test folder is around 800 Mbyte of lorem ipsum and other random .txt files
		File StartingDir = new File("C:/Users/mark/OneDrive/sdu/testfolder");
		String word = "ipsum";

		List<Result> list = WordFinder.findAll(word,StartingDir.toPath());
		System.out.println("Found " + list.size() + " Results");

//        list.forEach(i -> System.out.println("Word "+lookingfor+" found in file " + i.path()+ " at line "+i.line()));
		System.out.println("Parsing complete");

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println(totalTime + " ms");
	}
}
