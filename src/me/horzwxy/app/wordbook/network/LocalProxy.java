package me.horzwxy.app.wordbook.network;

import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by horz on 1/29/14.
 */
public class LocalProxy extends Proxy {

    @Override
    public Map<String, Word> getBasicWords() {
        Map<String, Word> result = new HashMap<String, Word>();
        try {
            File file = new File("/home/horz/workspace-java/wordbook_core/wordListFinal.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while(reader.ready()) {
                String line = reader.readLine();
                result.put(line, new Word(line, WordState.BASIC));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Map<String, Word> getIgnoredWords() {
        return new HashMap<String, Word>();
    }

    @Override
    public Map<String, Word> getFamiliarWords() {
        return new HashMap<String, Word>();
    }

    @Override
    public Map<String, Word> getUnfamiliarWords() {
        return new HashMap<String, Word>();

    }

    @Override
    public Map<String, Word> getUnrecognizedWords() {
        return new HashMap<String, Word>();
    }

    @Override
    public void updateBasicWords(Map<String, Word> basicWords) {

    }

    @Override
    public void updateIgnoredWords(Map<String, Word> ignoredWords) {

    }

    @Override
    public void updateFamiliarWords(Map<String, Word> familiarWords) {

    }

    @Override
    public void updateUnfamiliarWords(Map<String, Word> unfamiliarWords) {

    }

    @Override
    public void updateUnrecognizedWords(Map<String, Word> unrecognizedWords) {

    }
}
