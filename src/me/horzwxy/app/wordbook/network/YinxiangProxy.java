package me.horzwxy.app.wordbook.network;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.type.*;
import com.evernote.thrift.TException;
import me.horzwxy.app.wordbook.model.NoteManipulator;
import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Proxy to Yinxiang account.
 */
public class YinxiangProxy extends Proxy {

    private static final String AUTH_TOKEN = "S=s14:U=1c1a12:E=14b01a6b9d3:C=143a9f58dd5:P=1cd:A=en-devtoken:V=2:H=b4ef3878e66c13ff644c662a62e73e5c";

    private NoteStoreClient noteStore;
    private Map<WordState, Note> noteMap;

    /**
     * It's a synchronized method in which does networking. So it's better to call it in a new method.
     */
    public YinxiangProxy() {

        noteMap = new HashMap<WordState, Note>();

        String token = System.getenv("AUTH_TOKEN");
        if (token == null) {
            token = AUTH_TOKEN;
        }

        // Set up the UserStore client and check that we can speak to the server
        EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.YINXIANG, token);
        ClientFactory factory = new ClientFactory(evernoteAuth);
        // Set up the NoteStore client
        try {
            UserStoreClient userStore = factory.createUserStoreClient();

            boolean versionOk = userStore.checkVersion("Evernote EDAMDemo (Java)",
                    com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
                    com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
            if (!versionOk) {
                System.err.println("Incompatible Evernote client protocol version");
                System.exit(1);
            }

            noteStore = factory.createNoteStoreClient();
            List<Notebook> notebooks = noteStore.listNotebooks();
            for (Notebook notebook : notebooks) {
                if (notebook.getName().equals("wordbook")) {
                    NoteFilter noteFilter = new NoteFilter();
                    noteFilter.setNotebookGuid(notebook.getGuid());

                    List<Note> notes = noteStore.findNotes(noteFilter, 0, WordState.values().length).getNotes();
                    for (Note note : notes) {
                        note.setContent(noteStore.getNoteContent(note.getGuid()));
                        noteMap.put(WordState.valueOf(note.getTitle().toUpperCase()), note);
                    }
                }
            }
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Word> getBasicWords() {
        return NoteManipulator.getWords(noteMap.get(WordState.BASIC).getContent(), WordState.BASIC);
    }

    @Override
    public Map<String, Word> getIgnoredWords() {
        return NoteManipulator.getWords(noteMap.get(WordState.IGNORED).getContent(), WordState.IGNORED);
    }

    @Override
    public Map<String, Word> getFamiliarWords() {
        return NoteManipulator.getWords(noteMap.get(WordState.FAMILIAR).getContent(), WordState.FAMILIAR);
    }

    @Override
    public Map<String, Word> getUnfamiliarWords() {
        return NoteManipulator.getWords(noteMap.get(WordState.UNFAMILIAR).getContent(), WordState.UNFAMILIAR);
    }

    @Override
    public Map<String, Word> getUnrecognizedWords() {
        return NoteManipulator.getWords(noteMap.get(WordState.UNRECOGNIZED).getContent(), WordState.UNRECOGNIZED);
    }

    @Override
    public void updateBasicWords(Map<String, Word> basicWords) {
        updateNote(noteMap.get(WordState.BASIC), NoteManipulator.getNoteContent(basicWords));
    }

    @Override
    public void updateIgnoredWords(Map<String, Word> ignoredWords) {
        updateNote(noteMap.get(WordState.IGNORED), NoteManipulator.getNoteContent(ignoredWords));
    }

    @Override
    public void updateFamiliarWords(Map<String, Word> familiarWords) {
        updateNote(noteMap.get(WordState.FAMILIAR), NoteManipulator.getNoteContent(familiarWords));
    }

    @Override
    public void updateUnfamiliarWords(Map<String, Word> unfamiliarWords) {
        updateNote(noteMap.get(WordState.UNFAMILIAR), NoteManipulator.getNoteContent(unfamiliarWords));
    }

    @Override
    public void updateUnrecognizedWords(Map<String, Word> unrecognizedWords) {
        updateNote(noteMap.get(WordState.UNRECOGNIZED), NoteManipulator.getNoteContent(unrecognizedWords));
    }

    private void updateNote(Note note, String newContent) {
        note.setContent(newContent);
        try {
            noteStore.updateNote(note);
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }
}