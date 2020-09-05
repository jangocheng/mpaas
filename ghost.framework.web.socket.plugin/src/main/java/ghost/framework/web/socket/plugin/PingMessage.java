package ghost.framework.web.socket.plugin;

import java.nio.ByteBuffer;

/**
 * package: ghost.framework.web.socket.plugin
 *
 * @Author: 郭树灿{gsc-e590}
 * @link: 手机:13715848993, QQ 27048384
 * @Description:
 * @Date: 2020/9/2:22:22
 */
public interface PingMessage {
    /**
     * The application data inside the pong message from the peer.
     *
     * @return the application data.
     */
    ByteBuffer getApplicationData();
}
