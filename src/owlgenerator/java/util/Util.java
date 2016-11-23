/*
 * The Class Util.
 */
package owlgenerator.java.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.util.Pair;
import owlgenerator.java.core.CoreConfiguration;


/**
 * The Class Util.
 * 
 * @author Oscar Mateos Lopez
 * @version: 20161103
 */
public class Util {
	
	/**
	 * Gets the current time stamp.
	 *
	 * @return the current time stamp
	 */
	public static String getCurrentTimeStamp() {
	    return new SimpleDateFormat("yyyyMMdd").format(new Date());
	}
	
	/**
	 * Gets the continued table.
	 *
	 * @param continueTableNo, the continue table number
	 * @return the continued table
	 */
	public static File getContinuedTable(String continueTableNo) {
		CoreConfiguration cfg = CoreConfiguration.getInstance();
		File[] tableFiles = new File(cfg.getFiles_Path()).listFiles();

		Pattern pattern = Pattern.compile("\\[\\s*[Cc]ontinued\\s*in\\s*([Tt]able\\s*\\d+\\w?)\\s*\\]{1}");
		Matcher matcherTableNo = pattern.matcher(continueTableNo);
		String targetTable = null;

		if (matcherTableNo.matches())
			targetTable = StringUtils.capitalize(matcherTableNo.group(1));

		for (File tableFile : tableFiles) {
			String fileName = tableFile.getName();

			if (fileName.contains(targetTable))
				return tableFile;
		}

		return null;
	}
	
	/**
	 * Gets the first pair match by name.
	 *
	 * @param name, the name
	 * @param pairSet, the pair set
	 * @return the first pair match by name
	 */
	public static Pair<String, String> getFirstPairMatchByName(String name, Set<Pair<String, String>> pairSet) {
		for (Pair<String, String> pair_ : pairSet) {
			if (pair_.first().equals(name)) 
				return pair_;
		} 
		return null;
	}
	
	/**
	 * Gets the all pairs by name.
	 *
	 * @param name, the name
	 * @param pairSet, the pair set
	 * @return the all matched pairs by name
	 */
	public static Set <Pair<String, String>> getAllPairsByName(String name, Set<Pair<String, String>> pairSet) {
		Set <Pair<String, String>> pairMatches = new HashSet<Pair<String, String>>();
		
		for (Pair<String, String> pair_ : pairSet) {
			if (pair_.first().equals(name)) 
				pairMatches.add(pair_);
		} 
		return pairMatches;
	}
}