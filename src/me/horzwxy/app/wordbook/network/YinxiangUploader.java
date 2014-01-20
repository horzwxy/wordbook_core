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

import java.util.ArrayList;
import java.util.List;

/**
 * Uploader to Yinxiang account.
 */
public class YinxiangUploader extends Uploader {

    private static final String AUTH_TOKEN = "S=s14:U=1c1a12:E=14b01a6b9d3:C=143a9f58dd5:P=1cd:A=en-devtoken:V=2:H=b4ef3878e66c13ff644c662a62e73e5c";

    private UserStoreClient userStore;
    private NoteStoreClient noteStore;
    private Notebook wordbook;

    public YinxiangUploader(UploaderListener listener) {
        super(listener);

        String token = System.getenv("AUTH_TOKEN");
        if (token == null) {
            token = AUTH_TOKEN;
        }

        // Set up the UserStore client and check that we can speak to the server
        EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.YINXIANG, token);
        ClientFactory factory = new ClientFactory(evernoteAuth);
        // Set up the NoteStore client
        try {
            userStore = factory.createUserStoreClient();

            boolean versionOk = userStore.checkVersion("Evernote EDAMDemo (Java)",
                    com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
                    com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
            if (!versionOk) {
                System.err.println("Incompatible Evernote client protocol version");
                System.exit(1);
            }

            noteStore = factory.createNoteStoreClient();
            List<Notebook> notebooks = noteStore.listNotebooks();
            for(Notebook notebook : notebooks) {
                // presume that there must be a 'wordbook' in Yinxiang account
                if(notebook.getName().equals("wordbook")) {
                    this.wordbook = notebook;
                    break;
                }
            }
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }

        getListener().onStateUpdated("Uploader ready.");
    }

    @Override
    public int upload(UploadMessage msg) {
        getListener().onStateUpdated("Start uploading word " + msg.getWord());

        NoteFilter noteFilter = new NoteFilter();
        noteFilter.setNotebookGuid(wordbook.getGuid());

        try {
            getListener().onStateUpdated("Searching word record.");
            NoteList notes = noteStore.findNotes(noteFilter, 0, 1);
            if(notes.getNotesSize() != 0) {
                getListener().onStateUpdated("Try to fetch note content.");
                Note note = notes.getNotes().get(0);
                note.setContent(noteStore.getNoteContent(note.getGuid()));
                getListener().onStateUpdated("Try to update note.");
                updateNote(msg, note);
                getListener().onStateUpdated("Complete updating note.");
            }
            else {
                getListener().onStateUpdated("Try to create note.");
                List<String> egSentences = new ArrayList<String>();
                egSentences.add(msg.getEgSentence());
                EvernoteWordRecord record = new EvernoteWordRecord(msg.getWord(), egSentences);
                Note note = new Note();
                note.setNotebookGuid(wordbook.getGuid());
                note.setTitle(msg.getWord());
                note.setContent(record.getNoteContent());
                noteStore.createNote(note);
                getListener().onStateUpdated("Complete creating note.");
            }
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }

        return getAccumulatedTaskId();
    }

    private void updateNote(UploadMessage msg, Note originNote) throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        EvernoteWordRecord record = new EvernoteWordRecord(msg.getWord(), originNote.getContent());
        getListener().onStateUpdated("Complete parsing note.");
        record.getEgSentences().add(msg.getEgSentence());
        originNote.setContent(record.getNoteContent());
        getListener().onStateUpdated("Complete generating new content.");
        noteStore.updateNote(originNote);
    }
}