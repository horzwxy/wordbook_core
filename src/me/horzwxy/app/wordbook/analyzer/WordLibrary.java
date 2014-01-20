package me.horzwxy.app.wordbook.analyzer;

import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;

import java.util.ArrayList;
import java.util.Map;

/**
 * Word list that contains all words I know.
 * @author horz
 *
 */
public class WordLibrary {
	
	// the local storage which gets the vocabulary from permanent storage
    private Map<String, Word> library;

	public WordLibrary( Map< String, Word > library ) {
		this.library = library;
	}
	
	/**
	 * Check whether a word is on the list.
	 * @param word
	 * @return
	 */
	public WordState getWordState( String word ) {
        Word wordInstance = library.get( word );
		if( wordInstance != null ) {
            return wordInstance.getState();
        }
        else{
            return WordState.UNTRACKED;
        }
	}
	
	/**
	 * Return the word instance by word string.
	 * @param word
	 * @return
	 */
	public Word getWord( String word ) {
        return library.get( word );
	}

    public ArrayList< String > getBasicWords() {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList< Word > words = new ArrayList<Word>( library.values() );
        for( Word word : words ) {
            if( word.getState() == WordState.BASIC) {
                result.add( word.getContent() );
            }
        }
        return result;
    }

    public ArrayList< String > getIgnoredWords() {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList< Word > words = new ArrayList<Word>( library.values() );
        for( Word word : words ) {
            if( word.getState() == WordState.IGNORED) {
                result.add( word.getContent() );
            }
        }
        return result;
    }

    public ArrayList< Word > getUnfamiliarWords() {
        ArrayList<Word> result = new ArrayList<Word>();
        ArrayList< Word > words = new ArrayList<Word>( library.values() );
        for( Word word : words ) {
            if( word.getState() == WordState.UNFAMILIAR) {
                result.add( word );
            }
        }
        return result;
    }

    public ArrayList< Word > getUnrecognizedWords() {
        ArrayList<Word> result = new ArrayList<Word>();
        ArrayList< Word > words = new ArrayList<Word>( library.values() );
        for( Word word : words ) {
            if( word.getState() == WordState.UNRECOGNIZED) {
                result.add( word );
            }
        }
        return result;
    }
}