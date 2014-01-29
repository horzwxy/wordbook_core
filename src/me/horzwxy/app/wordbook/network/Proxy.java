package me.horzwxy.app.wordbook.network;

import me.horzwxy.app.wordbook.analyzer.WordLibrary;
import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;

import java.util.List;
import java.util.Map;

/**
 * Interface of general uploader.
 */
public abstract class Proxy {

    protected Proxy() {}

    public abstract Map<String, Word> getBasicWords();

    public abstract Map<String, Word> getIgnoredWords();

    public abstract Map<String, Word> getFamiliarWords();

    public abstract Map<String, Word> getUnfamiliarWords();

    public abstract Map<String, Word> getUnrecognizedWords();

    public abstract void updateBasicWords(Map<String, Word> basicWords);

    public abstract void updateIgnoredWords(Map<String, Word> ignoredWords);

    public abstract void updateFamiliarWords(Map<String, Word> familiarWords);

    public abstract void updateUnfamiliarWords(Map<String, Word> unfamiliarWords);

    public abstract void updateUnrecognizedWords(Map<String, Word> unrecognizedWords);

    public abstract void updateWords(WordLibrary library);
}
