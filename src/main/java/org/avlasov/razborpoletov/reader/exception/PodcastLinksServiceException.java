package org.avlasov.razborpoletov.reader.exception;

/**
 * Created by artemvlasov on 04/07/2017.
 */
public class PodcastLinksServiceException extends RuntimeException {

    public PodcastLinksServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PodcastLinksServiceException(Throwable cause) {
        super(cause);
    }
}
