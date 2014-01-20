package me.horzwxy.app.wordbook.network;

import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;

import java.util.List;
import java.util.Map;

/**
 * Interface of general uploader.
 */
public abstract class Uploader {

    protected Uploader() {}

    /**
     * Upload the message to remote server.
     * Uploading procedure is <em>synchronized</em>, indicating the caller should branch a new thread waiting for the method to return.
     * @return true if the upload succeeded
     */
    public abstract boolean upload(UploadMessage msg, WordState state);

    public abstract boolean remove(String word, WordState state);

    public abstract Map<String, Word> getBaseWords();

    public abstract Map<String, Word> getIgnoredWords();

    /**
     * Update the ignored words list.
     * @param msg word bundle ignored
     * @return true if the updating procedure is successful
     */
    public abstract boolean uploadIgnore(UploadMessage msg);
}
