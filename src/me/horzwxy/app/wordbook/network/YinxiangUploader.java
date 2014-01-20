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
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.*;
import com.evernote.thrift.TException;
import me.horzwxy.app.wordbook.model.EvernoteWordRecord;
import me.horzwxy.app.wordbook.model.Word;
import me.horzwxy.app.wordbook.model.WordState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uploader to Yinxiang account.
 */
public class YinxiangUploader extends Uploader {

    private static final String AUTH_TOKEN = "S=s14:U=1c1a12:E=14b01a6b9d3:C=143a9f58dd5:P=1cd:A=en-devtoken:V=2:H=b4ef3878e66c13ff644c662a62e73e5c";

    private NoteStoreClient noteStore;
    /**
     * A map from notebook name to Notebook instance, where name is UPPERCASE string.
     */
    private Map<String, Notebook> wordbooks;
    private Note ignoredNote;
    private Note baseNote;

    /**
     * It's a synchronized method in which does networking. So it's better to call it in a new method.
     */
    public YinxiangUploader() {

        wordbooks = new HashMap<String, Notebook>();

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
                if (notebook.getName().equals( WordState.UNFAMILIAR.name().toLowerCase() )
                        || notebook.getName().equals( WordState.UNRECOGNIZED.name().toLowerCase() )
                        || notebook.getName().equals( WordState.FAMILIAR.name().toLowerCase() )) {
                    wordbooks.put(notebook.getName().toUpperCase(), notebook);
                }
                else if (notebook.getName().equals("basewords")) {
                    NoteFilter noteFilter = new NoteFilter();
                    noteFilter.setNotebookGuid(notebook.getGuid());

                    List<Note> notes = noteStore.findNotes(noteFilter, 0, 2).getNotes();
                    for (Note note : notes) {
                        if (note.getTitle().equals("ignore")) {
                            note.setContent(noteStore.getNoteContent(note.getGuid()));
                            ignoredNote = note;
                        }
                        else if(note.getTitle().equals("base")) {
                            note.setContent(noteStore.getNoteContent(note.getGuid()));
                            baseNote = note;
                        }
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
    public boolean upload(final UploadMessage msg, WordState state) {
        Notebook wordbook = wordbooks.get(state.name());

        NoteFilter noteFilter = new NoteFilter();
        noteFilter.setNotebookGuid(wordbook.getGuid());

        try {
            NoteList notes = noteStore.findNotes(noteFilter, 0, 1);
            if (notes.getNotesSize() != 0) {
                Note note = notes.getNotes().get(0);
                note.setContent(noteStore.getNoteContent(note.getGuid()));
                updateNote(msg, note);
            } else {
                createNote(msg, wordbook);
            }
        } catch (EDAMUserException e) {
            e.printStackTrace();
            return false;
        } catch (EDAMSystemException e) {
            e.printStackTrace();
            return false;
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (TException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean remove(String word, WordState state) {
        Notebook wordbook = wordbooks.get(state.name());

        NoteFilter noteFilter = new NoteFilter();
        noteFilter.setNotebookGuid(wordbook.getGuid());
        noteFilter.setWords("intitle:" + word);
        try {
            List<Note> notes = noteStore.findNotes(noteFilter, 0, 1).getNotes();
            if(notes.size() != 0) {
                noteStore.deleteNote(notes.get(0).getGuid());
            }
        } catch (EDAMUserException e) {
            e.printStackTrace();
            return false;
        } catch (EDAMSystemException e) {
            e.printStackTrace();
            return false;
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (TException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean uploadIgnore(UploadMessage msg) {
        try {
            EvernoteWordRecord record = new EvernoteWordRecord("ignore", ignoredNote.getContent());
            record.getEgSentences().add(msg.getWord());
            ignoredNote.setContent(record.getNoteContent());
            noteStore.updateNote(ignoredNote);
        } catch (EDAMUserException e) {
            e.printStackTrace();
            return false;
        } catch (EDAMSystemException e) {
            e.printStackTrace();
            return false;
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (TException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void createNote(UploadMessage msg, Notebook wordbook) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        List<String> egSentences = new ArrayList<String>();
        egSentences.add(msg.getEgSentence());
        EvernoteWordRecord record = new EvernoteWordRecord(msg.getWord(), egSentences);
        Note note = new Note();
        note.setNotebookGuid(wordbook.getGuid());
        note.setTitle(msg.getWord());
        note.setContent(record.getNoteContent());
        noteStore.createNote(note);
    }

    @Override
    public Map<String, Word> getBaseWords() {
        EvernoteWordRecord record = new EvernoteWordRecord("base", baseNote.getContent());
        List<String> words = record.getEgSentences();
        Map<String, Word> result = new HashMap<String, Word>();
        for(String s: words) {
            result.put(s, new Word(s, WordState.BASIC));
        }
        return result;
    }

    @Override
    public Map<String, Word> getIgnoredWords() {
        if(ignoredNote.getContent() == null) {
            return new HashMap<String, Word>();
        }

        EvernoteWordRecord record = new EvernoteWordRecord("ignored", ignoredNote.getContent());
        List<String> words = record.getEgSentences();
        Map<String, Word> result = new HashMap<String, Word>();
        for(String s: words) {
            result.put(s, new Word(s, WordState.IGNORED));
        }
        return result;
    }

    private void updateNote(UploadMessage msg, Note originNote) throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        EvernoteWordRecord record = new EvernoteWordRecord(msg.getWord(), originNote.getContent());
        record.getEgSentences().add(msg.getEgSentence());
        originNote.setContent(record.getNoteContent());
        noteStore.updateNote(originNote);
    }
}