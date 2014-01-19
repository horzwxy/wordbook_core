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
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.TException;
import com.evernote.thrift.transport.TTransportException;

import java.util.List;

/**
 * Uploader to Evernote(International) account.
 */
public class EvernoteUploader extends Uploader {

    private static final String AUTH_TOKEN = "S=s14:U=1c1a12:E=14b01a6b9d3:C=143a9f58dd5:P=1cd:A=en-devtoken:V=2:H=b4ef3878e66c13ff644c662a62e73e5c";

    private UserStoreClient userStore;
    private NoteStoreClient noteStore;

    public EvernoteUploader(UploaderListener listener) {
        super(listener);

        String token = System.getenv("AUTH_TOKEN");
        if (token == null) {
            token = AUTH_TOKEN;
        }

        // Set up the UserStore client and check that we can speak to the server
        EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.YINXIANG, token);
        ClientFactory factory = new ClientFactory(evernoteAuth);
        try {
            userStore = factory.createUserStoreClient();
        } catch (TTransportException e) {
            e.printStackTrace();
        }

        boolean versionOk = false;
        try {
            versionOk = userStore.checkVersion("Evernote EDAMDemo (Java)",
                    com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
                    com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
        } catch (TException e) {
            e.printStackTrace();
        }

        if (!versionOk) {
            System.err.println("Incompatible Evernote client protocol version");
            System.exit(1);
        }

        // Set up the NoteStore client
        try {
            noteStore = factory.createNoteStoreClient();
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int upload(UploadMessage msg) {
        // List the notes in the user's account
        System.out.println("Listing notes:");

        // First, get a list of all notebooks
        List<Notebook> notebooks = null;
        try {
            notebooks = noteStore.listNotebooks();
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }

        for (Notebook notebook : notebooks) {
            System.out.println("Notebook: " + notebook.getName());

            // Next, search for the first 100 notes in this notebook, ordering
            // by creation date
            NoteFilter filter = new NoteFilter();
            filter.setNotebookGuid(notebook.getGuid());
            filter.setOrder(NoteSortOrder.CREATED.getValue());
            filter.setAscending(true);

            NoteList noteList = null;
            try {
                noteList = noteStore.findNotes(filter, 0, 100);
            } catch (EDAMUserException e) {
                e.printStackTrace();
            } catch (EDAMSystemException e) {
                e.printStackTrace();
            } catch (EDAMNotFoundException e) {
                e.printStackTrace();
            } catch (TException e) {
                e.printStackTrace();
            }
            List<Note> notes = noteList.getNotes();
            for (Note note : notes) {
                System.out.println(" * " + note.getTitle());
            }
        }
        System.out.println();

        return getAccumulatedTaskId();
    }
}