package me.horzwxy.app.wordbook.analyzer;

import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;

import java.util.*;

/**
 * Word list that contains all words I know.
 * @author horz
 *
 */
public class WordLibrary {
	
	// the local storage which gets the vocabulary from permanent storage
    private Map<String, Word> library;
    private Map<WordState, Collection<Word>> stateTable;

    public void printRecords() {
        for(WordState state : WordState.values()) {
            if(state.equals(WordState.BASIC)) {
                continue;
            }
            System.out.println("state:" + state.name());
            Collection<Word> words = stateTable.get(state);
            for(Word word : words) {
                System.out.println("content:" + word.getContent());
                for(String sentence : word.getSentences()) {
                    System.out.println("sentence:" + sentence);
                }
            }
        }
    }

    public WordLibrary() {
        this.library = new HashMap<String, Word>();
        this.stateTable = new HashMap<WordState, Collection<Word>>();
        for(WordState state : WordState.values()) {
            this.stateTable.put(state, new LinkedList<Word>());
        }
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

    public Word getWord(List<String> possibleForms) {
        for(String form : possibleForms) {
            if(library.containsKey(form)) {
                return library.get(form);
            }
        }

        return null;
    }

    public Collection<Word> getWords(WordState state) {
        return stateTable.get(state);
    }

    public void addWord(Word word, WordState state) {
        library.put(word.getContent().toLowerCase(), word);
        stateTable.get(state).add(word);
    }

    public void addWords(Collection<Word> words, WordState state) {
        for(Word word : words) {
            library.put(word.getContent().toLowerCase(), word);
        }
        stateTable.get(state).addAll(words);
    }

    public void updateWordContent(Word word, String newContent) {
        library.remove(word.getContent().toLowerCase());
        word.setContent(newContent);
        library.put(newContent, word);
    }

    public void updateWordState(Word word, WordState originalState) {
        if(!word.getState().equals(originalState)) {
            stateTable.get(originalState).remove(word);
            stateTable.get(word.getState()).add(word);
        }
    }
}