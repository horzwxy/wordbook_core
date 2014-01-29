package me.horzwxy.app.wordbook.analyzer;

import me.horzwxy.app.wordbook.model.WordState;
import java.util.ArrayList;

/**
 * This class is a new word recognizing machine. It gets a String input as a word. Then the user checks RESULT and POSSIBLE-FORMS fields by the getter methods.
 * The following forms can be dealt with:
 * [1] some kind of abbreviation: n't(not), 're(are), 've(have), 'll(will), s'(belonging, of which ends with 's'), 's(belonging), 'd(would), 'm(am)
 * [2] plural: -s, -es, -ies, -ves
 * [3] continuing tense: -ing(including doubling last letter and omitting last 'e')
 * [4] past tense: -ed(including doubling last letter and omitting last 'e'), -ied
 * [5] comparing: -er(including doubling last letter and omitting last 'e'), -ier
 * [6] greatest: -est(including doubling last letter and omitting last 'e'), -iest
 * [7] the word itself
 * The following forms are NOT treated as variants:
 * [1] -ly
 * [2] -tion, -sion
 * NOTICE: One word may match some forms independently, or together. For example, "classes'" first matches "-'s", then "-es".
 * The processing sequence is specially designed to properly deal with the TOGETHER case. So it'll find out the most ORIGINAL form.
 * The word library could be replaced. And it should be initialized with a certain WordLibrary instance before the first use.
 * 
 * @author horz
 *
 */
public class WordRecognizer {
	
	private WordLibrary wordLib;
	
	public WordRecognizer( WordLibrary wordLib ) {
		this.wordLib = wordLib;
	}

    /**
     * A private method to guess the word's state.
     * @param word
     * @return
     */
	public WordState analyze( String word ) {
		ArrayList< String > possibleForms = findPossibleForms( word );
		for( String form: possibleForms ) {
			// If one of the forms is tracked, break the loop immediately.
			if( wordLib.getWordState( form ) != WordState.UNTRACKED) {
				return wordLib.getWordState( form );
			}
		}
        return WordState.UNTRACKED;
	}
	
	/**
	 * A public tool method to find the possible variants of a word.
	 * It's used in the recognizing process. But it can also be used independently.
	 * The order of the checking block cannot be arbitrarily changed. First omit the abbreviation suffix. Then others.
	 * NOTICE: two kinds of quote mark, ' and ` will be treated as the same.
	 * @param word
	 * @return
	 */
	public static ArrayList< String > findPossibleForms( String word ) {
		ArrayList< String > result = new ArrayList< String >();
		
		try{
		
			// unify the 'quote' mark style
			if( word.contains( "`" ) ) {
				word = word.replace( '`', '\'' );
			}
			
			/* The word itself must be a possible result. */
			// For convenience, only the lower case form is under consideration.
			word = word.toLowerCase();
			result.add( word );
			
			/* 
			 * Omit the additional suffix.
			 * Suppose at most one of them can be matched.
			 */
			// -n't form
			if( word.endsWith( "n't" ) ) {
				word = word.substring( 0, word.length() - 3 );
				if( word.equals( "ca" ) ) {
					word = word + "n";	// "can't" is a special case
				}
				result.add( word );
			}
			
			// -'re form
			else if( word.endsWith( "'re" ) ) {
				word = word.substring( 0, word.length() - 3 );
				result.add( word );
			}
			
			// -'ve form
			else if( word.endsWith( "'ve" ) ) {
				word = word.substring( 0, word.length() - 3 );
				result.add( word );
			}
			
			// -'ll form
			else if( word.endsWith( "'ll" ) ) {
				word = word.substring( 0, word.length() - 3 );
				result.add( word );
			}
			
			// -s' form
			else if( word.endsWith( "s'" ) ) {
				word = word.substring( 0, word.length() - 1 );	// only remove the last '
				result.add( word );
			}
			
			// -'s form
			else if( word.endsWith( "'s" ) ) {
				word = word.substring( 0, word.length() - 2 );
				result.add( word );
			}
			
			// -'d form
			else if( word.endsWith( "'d" ) ) {
				word = word.substring( 0, word.length() - 2 );
				result.add( word );
			}
			
			// -'m form
			else if( word.endsWith( "'m" ) ) {
				word = word.substring( 0, word.length() - 2 );
				result.add( word );
			}
			
			/*
			 * The changed form is not necessarily the right answer.
			 * The following logic will not change the word on itself.
			 * Suppose at most one of them will be matched.
			 */
			// -s form
			if( word.endsWith( "s" ) ) {
				result.add( word.substring( 0, word.length() - 1 ) );	// no -s
				
				// -es, -ies, and -ves form
				if( word.endsWith( "es" ) ) {
					result.add( word.substring( 0, word.length() - 2 ) );	// no -es
					
					if( word.endsWith( "ies" ) ) {
						result.add( word.substring( 0, word.length() - 3 ) + "y" );	// no -ies and recover the 'y'
					}
					else if( word.endsWith( "ves" ) ) {
						result.add( word.substring( 0, word.length() - 3 ) + "f" );	// no -ves and recover 'f'
						result.add( word.substring( 0, word.length() - 3 ) + "fe" );	// no -ves and recover "fe"
					}
				}
			}
			
			// -ing form
			else if( word.endsWith( "ing" ) ) {
				result.add( word.substring( 0, word.length() - 3 ) );	// no -ing
				result.add( word.substring( 0, word.length() - 3 ) + "e" );	// no -ing and add an 'e'
				
				// if the last letter was doubled
				if( word.length() > 4 && word.charAt( word.length() - 4 ) == word.charAt( word.length() - 5 ) ) {
					result.add( word.substring( 0, word.length() - 4 ) );	// no double letter
				}
			}
			
			// -ed form
			else if( word.endsWith( "ed" ) ) {
				result.add( word.substring( 0, word.length() - 2 ) );	// no -ed
				result.add( word.substring( 0, word.length() - 1 ) );	// no -ed and add an 'e'
				
				// -ied form
				if( word.endsWith( "ied" ) ) {
					result.add( word.substring( 0, word.length() - 3 ) + "y" );	// no -ied and add 'y'
				}
				// if the last letter was doubled
				// 'led' is a special word
				if( word.length() > 3 && word.charAt( word.length() - 3 ) == word.charAt( word.length() - 4 ) ) {
					result.add( word.substring( 0, word.length() - 3 ) );	// no double letter
				}
			}
			
			// -er form
			else if( word.endsWith( "er" ) ) {
				result.add( word.substring( 0, word.length() - 2 ) );	// no -er
				result.add( word.substring( 0, word.length() - 1 ) );	// no -er and add an 'e'
				
				// -ier form
				if( word.endsWith( "ier" ) ) {
					result.add( word.substring( 0, word.length() - 3 ) + "y" );	// no -ier and add 'y'
				}
				// if the last letter was doubled
				if( word.charAt( word.length() - 3 ) == word.charAt( word.length() - 4 ) ) {
					result.add( word.substring( 0, word.length() - 3 ) );	// no double letter
				}
			}
			
			// -est form
			else if( word.endsWith( "est" ) ) {
				result.add( word.substring( 0, word.length() - 3 ) );	// no -est
				result.add( word.substring( 0, word.length() - 2 ) );	// no -est and add an 'e'
				result.add( word.substring( 0, word.length() - 1 ) );	// no -est and add "es"
				
				// -iest form
				if( word.endsWith( "iest" ) ) {
					result.add( word.substring( 0, word.length() - 4 ) + "y" );	// no -iest and add 'y'
				}
				// if the last letter was doubled
				if( word.length() > 4 && word.charAt( word.length() - 4 ) == word.charAt( word.length() -5 ) ) {
					result.add( word.substring( 0, word.length() - 4 ) );	// no double letter
				}
			}
		
		} catch( StringIndexOutOfBoundsException e ) {
			// I cannot find out which word will trigger it. But for easy debugging, I make this block.
			System.out.println( "Recognizer finds a special word:" + word );
			//throw new StringIndexOutOfBoundsException();
		}
		
		return result;
	}
}
