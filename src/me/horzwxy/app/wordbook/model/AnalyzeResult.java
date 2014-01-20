package me.horzwxy.app.wordbook.model;

import java.util.List;

/**
 * Created by horz on 1/20/14.
 */
public class AnalyzeResult {

    private Word word;
    private List<String> possibleForms;
    private String egSentence;

    public AnalyzeResult(Word word, List<String> possibleForms, String egSentence) {
        this.word = word;
        this.possibleForms = possibleForms;
        this.egSentence = egSentence;
    }

    public Word getWord() {
        return word;
    }

    public List<String> getPossibleForms() {
        return possibleForms;
    }

    public String getEgSentence() {
        return egSentence;
    }
}
