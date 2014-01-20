package me.horzwxy.app.wordbook.model;

import java.util.List;

/**
 * Created by horz on 1/19/14.
 */
public class WordRecord {

    private String word;
    private List<String> egSentences;

    public WordRecord(String word, List<String> egSentences) {
        this.word = word;
        this.egSentences = egSentences;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<String> getEgSentences() {
        return egSentences;
    }

    public void setEgSentences(List<String> egSentences) {
        this.egSentences = egSentences;
    }
}
