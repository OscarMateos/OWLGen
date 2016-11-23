/*
 * The Class StringSimilarityDistance with the similarity measure for matchings
 */
package owlgenerator.java.merger;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class StringSimilarityDistance with the similarity measure for matchings
 *
 * @author Oscar Mateos Lopez
 * @version: 20161103
 */

public class StringSimilarityDistance {
	
	/** The Constant SIMILARITY_THRESHOLD. */
	public static final double SIMILARITY_THRESHOLD = 0.95;
	
	/**
	 * Gets the secuential jaro wrinkler distance.
	 *
	 * @param str1, the str 1
	 * @param str2, the str 2
	 * @return the secuential jaro wrinkler distance
	 */
	public static double getSecuentialJaroWrinklerDistance(String str1, String str2) {
		String [] leafDPNameSplit = StringUtils.splitByCharacterTypeCamelCase(str1);
		String [] classNameSplit = StringUtils.splitByCharacterTypeCamelCase(str2);
		double jwd = 0.0;
		
		// If both strings are composed by the same number of literals
		if(leafDPNameSplit.length == classNameSplit.length) {
			for(int i = 0; i < leafDPNameSplit.length; i++)
				jwd += StringUtils.getJaroWinklerDistance(leafDPNameSplit[i], classNameSplit[i]);			
			jwd /= leafDPNameSplit.length;			
		}
		else
			jwd = StringUtils.getJaroWinklerDistance(str1, str2);
		return jwd;	
	}
}
