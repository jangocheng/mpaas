package ghost.framework.web.context.http;

/**
 * package: ghost.framework.web.module.http
 *
 * @Author: 郭树灿{guoshucan-pc}
 * @link: 手机:13715848993, QQ 27048384
 * @Description:
 * @Date: 2019-11-15:8:41
 */

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an HTTP input message, consisting of {@linkplain #getHeaders() headers}
 * and a readable {@linkplain #getBody() body}.
 *
 * <p>Typically implemented by an HTTP request handle on the server side,
 * or an HTTP response handle on the client side.
 *
 * @author Arjen Poutsma
 * @since 3.0
 */
public interface HttpInputMessage extends HttpMessage {

    /**
     * Return the body of the message as an input stream.
     * @return the input stream body (never {@code null})
     * @throws IOException in case of I/O errors
     */
    InputStream getBody() throws IOException;

}
