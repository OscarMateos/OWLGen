package owlgenerator.tests.ontoTag.merge;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.util.EditDistance;
import owlgenerator.java.inverserelations.InferInverseRelations;

public class distanceTests {
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		// SE HA TENIDO EN CUENTA FINALMENTE(JARO-WINKLER, SEPARANDO Y MIDIENDO CADA PALABRA Y DIVIDIENDO ENTRE EL NUMERO DE PALABRAS) 
		//		TESTS SIMILARITY DISTANCE & THRESHOLD
		String st1 = InferInverseRelations.getStem("Syntactically Used As");
		String st2 = InferInverseRelations.getStem("Synt Used As");

		String st1b = "Syntactically";
		String st2b = "Syntactical";

		EditDistance d = new EditDistance();
		System.out.println(d.score(st1, st1));//0.0 -> Match
		System.out.println(d.score(st1, st2));//3.0 -> Multiword Threshold
		System.out.println(d.score(st2, st1));//3.0 -> Multiword Threshold	
		
		d = new EditDistance(false);		  //Doesn't allow transposition
		System.out.println(d.score(st1, st1));//0.0 -> Match
		System.out.println(d.score(st1, st2));//3.0 -> Multiword Threshold
		System.out.println(d.score(st2, st1));//3.0 -> Multiword Threshold	
		
		int ld1a = StringUtils.getLevenshteinDistance(st1, st2); //SameAs Edit Distance
		int ld2a = StringUtils.getLevenshteinDistance(st2, st1);
//		int ldt = StringUtils.getLevenshteinDistance(s, t, threshold);			    

		int fd0 = StringUtils.getFuzzyDistance(st1, st1, Locale.ENGLISH);//st1 longer
		int fd1 = StringUtils.getFuzzyDistance(st1, st2, Locale.ENGLISH);
		int fd2 = StringUtils.getFuzzyDistance(st2, st1, Locale.ENGLISH);

		double jwd0 = StringUtils.getJaroWinklerDistance(st1, st1);//1.0 -> Match
		double jwd1 = StringUtils.getJaroWinklerDistance(st1, st2);//0.86 -> Threshold(Synt, Syntactically)
		double jwd2 = StringUtils.getJaroWinklerDistance(st2, st1);//0.86 -> Threshold
		// Secuencial, sumando por palabra y promediado por el numero de palabras, nos daria 0,9533333333333333, muy bueno este metodo, Convence y esta NORMALIZADO.

		//OTHER TESTS
		String cp = StringUtils.getCommonPrefix(st1, st2);//Synt	

		boolean isA1 = edu.stanford.nlp.util.StringUtils.isAcronym("Syntactically");//false
		boolean isA2 = edu.stanford.nlp.util.StringUtils.isAcronym("Synt");//false
		boolean isA3 = edu.stanford.nlp.util.StringUtils.isAcronym("S.U.A.");//false
		//boolean isA4 = edu.stanford.nlp.util.StringUtils.isAcronym("SUA");//true -> ESTO NO SIRVE
				
		int common1 = edu.stanford.nlp.util.StringUtils.longestCommonSubstring("SyntacticallyUsedAs", "SyntacticallyUsedAs");//19 = length, es una medida similar a edit distance sin substituciones
		int common2 = edu.stanford.nlp.util.StringUtils.longestCommonSubstring("SyntacticallyUsedAs", "SyntUsedAs");//10, num d caracteres diferentes, no sirve porque al acortar mucho la primera palabra y el peso es global hay una diferencia muy grande
		int common3 = edu.stanford.nlp.util.StringUtils.longestCommonSubstring("SyntUsedAs", "SyntacticallyUsedAs");//10

		//Podria servir para quitar el "commonPrefix", long del final hacia atras, pero como la part comun puede ser mayor al inicio de la palabra
		int commonC1 = edu.stanford.nlp.util.StringUtils.longestCommonContiguousSubstring("SyntacticallyUsedAs", "SyntacticallyUsedAs");//19
		int commonC2 = edu.stanford.nlp.util.StringUtils.longestCommonContiguousSubstring("SyntacticallyUsedAs", "SyntUsedAs");//6
		int commonC3 = edu.stanford.nlp.util.StringUtils.longestCommonContiguousSubstring("SyntUsedAs", "SyntacticallyUsedAs");//6 -> NO SIRVE 

		int ed1 = edu.stanford.nlp.util.StringUtils.editDistance(st1, st1);//0
		int ed2 = edu.stanford.nlp.util.StringUtils.editDistance(st1, st2);//9
		int ed3 = edu.stanford.nlp.util.StringUtils.editDistance(st2, st1);//9 --> Ya visto

		int ld1 = edu.stanford.nlp.util.StringUtils.levenshteinDistance(st1, st1);//0
		int ld2 = edu.stanford.nlp.util.StringUtils.levenshteinDistance(st1, st2);//9
		int ld3 = edu.stanford.nlp.util.StringUtils.levenshteinDistance(st2, st1);//9 --> Ya visto
						
//		edu.stanford.nlp.util.StringUtils.getBaseName(fileName, suffix); // NO SIRVE
	}
}
