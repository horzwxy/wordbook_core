package me.horzwxy.app.wordbook.analyzer;

import me.horzwxy.app.wordbook.model.AnalyzeResult;
import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;

import java.util.ArrayList;
import java.util.List;

/**
 * English analyzer, providing methods to pick unfamiliar words from a sentence.
 */
public class SentenceAnalyzer {

    private WordLibrary wordLibrary;
    private WordRecognizer recognizer;

    public SentenceAnalyzer( WordLibrary wordLibrary,
                             WordRecognizer recognizer) {
        this.wordLibrary = wordLibrary;
        this.recognizer = recognizer;
    }

    /**
     * Pick the unfamiliar, unrecognized and untracked words from the sentence.
     * @param sentence English sentence to be analyzed
     * @return list of the three kinds of words
     */
    public List<AnalyzeResult> analyzeSentence( String sentence ) {
        List<AnalyzeResult> resultList = new ArrayList<AnalyzeResult>();
        String[] rawWords = sentence.split( " " );	// split by blanks
        for( int i = 0; i < rawWords.length; i++ ) {
            String rawWord = rawWords[ i ];

            String pw = getPurifiedWord(rawWord);
            if( pw == null )
            {
                // if it's full of symbols
                continue;
            }

            WordState state = recognizer.analyze( pw );
            List<String> possibleforms = WordRecognizer.findPossibleForms(pw);

            if(state.ordinal() <= WordState.FAMILIAR.ordinal()) {
                continue;
            }

            Word wordInstance = null;
            if( state == WordState.UNFAMILIAR ||
                    state == WordState.UNRECOGNIZED) {
                wordInstance = wordLibrary.getWord( possibleforms );

            }
            else if( state == WordState.UNTRACKED) {
                wordInstance = new Word(pw, WordState.UNTRACKED);
                wordLibrary.addWord(wordInstance, WordState.UNTRACKED);
            }

            assert wordInstance != null;
            if(!wordInstance.getSentences().contains(sentence)) {
                wordInstance.addSentence(sentence);
            }
            AnalyzeResult result = new AnalyzeResult(
                    wordInstance,
                    possibleforms,
                    sentence
            );
            resultList.add(result);
        }
        return resultList;
    }

    private String getPurifiedWord( String rawWord ) {
        int prefixLength = 0;
        for( int j = 0; j < rawWord.length(); j++ ) {
            // skip the heading symbols
            if( rawWord.charAt( j ) < 'A' || rawWord.charAt( j ) > 'z' ) {
                prefixLength++;
            }
            else break;
        }
        int suffixLength = 0;
        for( int j = rawWord.length() - 1; j >= 0; j-- ) {
            // skip the ending symbols
            if( rawWord.charAt( j ) < 'A' || rawWord.charAt( j ) > 'z' ) {
                suffixLength++;
            }
            else break;
        }
        // If the rawWord is full of symbols, skip to the next
        if( prefixLength == rawWord.length() ) {
            return null;
        }
        // get the purified word
        return rawWord.substring( prefixLength, rawWord.length() - suffixLength );
    }
}
