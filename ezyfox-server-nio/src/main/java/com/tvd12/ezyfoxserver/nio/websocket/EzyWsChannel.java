package com.tvd12.ezyfoxserver.nio.websocket;

import static com.tvd12.ezyfox.util.EzyProcessor.processWithException;
import static com.tvd12.ezyfox.util.EzyProcessor.processWithLogException;
import static com.tvd12.ezyfoxserver.nio.websocket.EzyWsCloseStatus.CLOSE_BY_SERVER;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.constant.EzyConnectionType;
import com.tvd12.ezyfoxserver.socket.EzyChannel;

import lombok.Getter;

@Getter
public class EzyWsChannel extends EzyLoggable implements EzyChannel {

	private final Session session;
	private volatile boolean opened;
	private final SocketAddress serverAddress;
	private final SocketAddress clientAddress;
	
	public EzyWsChannel(Session session) {
		this.opened = true;
		this.session = session;
		this.serverAddress = session.getLocalAddress();
		this.clientAddress = session.getRemoteAddress();
	}
	
	@Override
	public int write(Object data, boolean binary) throws Exception {
		try {
			if(binary)
				return writeBinary((byte[])data);
			return writeString((String)data);
		}
		catch(WebSocketException e) {
			logger.debug("write data: " + data + ", to: " + clientAddress + " error", e);
			return 0;
		}
	}
	
	private int writeBinary(byte[] bytes) throws Exception {
		int bytesSize = bytes.length;
		RemoteEndpoint remote = session.getRemote();
		remote.sendBytes(ByteBuffer.wrap(bytes));
		return bytesSize;
	}
	
	private int writeString(String bytes) throws Exception {
		int bytesSize = bytes.length();
		RemoteEndpoint remote = session.getRemote();
		remote.sendString(bytes);
		return bytesSize;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Session getConnection() {
		return session;
	}
	
	@Override
	public EzyConnectionType getConnectionType() {
		return EzyConnectionType.WEBSOCKET;
	}
	
	@Override
	public boolean isConnected() {
		return opened;
	}
	
	public void setClosed() {
		this.opened = false;
	}
	
	@Override
	public void disconnect() {
		processWithLogException(session::disconnect);
	}
	
	@Override
	public void close() {
		processWithException(() -> session.close(CLOSE_BY_SERVER));
	}
}
