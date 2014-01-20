package me.horzwxy.app.wordbook.network;

/**
 * Interface of general uploader.
 */
public abstract class Uploader {

    private int accumulatedTaskId = 0;
    private UploaderListener listener;

    protected Uploader(UploaderListener listener) {
        this.listener = listener;
    }

    protected UploaderListener getListener() {
        return listener;
    }

    /**
     * Upload the message to remote server.
     * It does all synchronization work. So it can be call at the same moment among multiple threads.
     * Uploading procedure is <em>asynchronized</em>, indicating the call can get task ID immediately after the invocation, and wait for the callback in the listener.
     * @return task ID
     */
    public abstract int upload(UploadMessage msg);

    /**
     * Get an available task ID and increase it by additional one.
     * @return available task ID
     */
    protected synchronized int getAccumulatedTaskId() {
        return accumulatedTaskId++;
    }
}
