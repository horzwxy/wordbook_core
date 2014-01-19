package me.horzwxy.app.wordbook.network;

/**
 * Message passed to uploader, containing info to be uploaded.
 */
public class UploadMessage {

    private String word;        // the word itself
    private String egSentence;  // example sentence

    public UploadMessage(String word, String egSentence) {
        this.word = word;
        this.egSentence = egSentence;
    }

    @Override
    public String toString() {
        return word + ": " + egSentence;
    }
}
