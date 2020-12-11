package com.tvd12.ezyfoxserver.nio.testing;

import static org.mockito.Mockito.mock;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLContext;

import org.testng.annotations.Test;

import com.tvd12.ezyfoxserver.EzyBootstrap;
import com.tvd12.ezyfoxserver.EzySimpleServer;
import com.tvd12.ezyfoxserver.api.EzyResponseApi;
import com.tvd12.ezyfoxserver.api.EzyStreamingApi;
import com.tvd12.ezyfoxserver.config.EzySimpleConfig;
import com.tvd12.ezyfoxserver.context.EzyServerContext;
import com.tvd12.ezyfoxserver.context.EzySimpleServerContext;
import com.tvd12.ezyfoxserver.controller.EzyServerReadyController;
import com.tvd12.ezyfoxserver.event.EzyServerReadyEvent;
import com.tvd12.ezyfoxserver.nio.EzyNioServerBootstrap;
import com.tvd12.ezyfoxserver.nio.wrapper.EzyHandlerGroupManager;
import com.tvd12.ezyfoxserver.setting.EzyEventControllersSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleEventControllersSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleSettings;
import com.tvd12.ezyfoxserver.setting.EzySimpleStreamingSetting;
import com.tvd12.ezyfoxserver.socket.EzyBlockingSessionTicketsQueue;
import com.tvd12.ezyfoxserver.socket.EzyBlockingSocketStreamQueue;
import com.tvd12.ezyfoxserver.socket.EzyBlockingSocketUserRemovalQueue;
import com.tvd12.ezyfoxserver.socket.EzySessionTicketsQueue;
import com.tvd12.ezyfoxserver.socket.EzySimpleSocketRequestQueues;
import com.tvd12.ezyfoxserver.socket.EzySocketDisconnection;
import com.tvd12.ezyfoxserver.socket.EzySocketDisconnectionQueue;
import com.tvd12.ezyfoxserver.socket.EzySocketRequestQueues;
import com.tvd12.ezyfoxserver.socket.EzySocketStreamQueue;
import com.tvd12.ezyfoxserver.socket.EzySocketUserRemovalQueue;
import com.tvd12.ezyfoxserver.wrapper.EzyEventControllers;
import com.tvd12.ezyfoxserver.wrapper.EzyServerControllers;
import com.tvd12.ezyfoxserver.wrapper.impl.EzyEventControllersImpl;
import com.tvd12.ezyfoxserver.wrapper.impl.EzyServerControllersImpl;
import com.tvd12.test.base.BaseTest;

public class EzyNioServerBootstrapTest extends BaseTest {

	@Test
	public void test() throws Exception {
		SSLContext sslContext = SSLContext.getDefault();
		EzyResponseApi responseApi = mock(EzyResponseApi.class);
		EzyStreamingApi streamingApi = mock(EzyStreamingApi.class);
		EzySocketRequestQueues requestQueues = new EzySimpleSocketRequestQueues();
		EzySocketStreamQueue streamQueue = new EzyBlockingSocketStreamQueue();
		EzyHandlerGroupManager handlerGroupManager = mock(EzyHandlerGroupManager.class);
		EzySessionTicketsQueue socketSessionTicketsQueue = new EzyBlockingSessionTicketsQueue();
		EzySessionTicketsQueue websocketSessionTicketsQueue = new EzyBlockingSessionTicketsQueue();
		EzySocketDisconnectionQueue socketDisconnectionQueue = new EzySocketDisconnectionQueue() {
			
			BlockingQueue<EzySocketDisconnection> queue = new LinkedBlockingQueue<>();
			
			@Override
			public EzySocketDisconnection take() throws InterruptedException {
				return queue.take();
			}
			
			@Override
			public int size() {
				return 0;
			}
			
			@Override
			public void remove(EzySocketDisconnection disconnection) {
			}
			
			@Override
			public boolean isEmpty() {
				return false;
			}
			
			@Override
			public void clear() {
			}
			
			@Override
			public boolean add(EzySocketDisconnection disconnection) {
				return false;
			}
		};
		
		EzySimpleConfig config = new EzySimpleConfig();
		EzySimpleSettings settings = new EzySimpleSettings();
		EzySimpleStreamingSetting streaming = settings.getStreaming();
		streaming.setEnable(true);
		settings.getUdp().setActive(true);
		EzySimpleServer server = new EzySimpleServer();
		EzyServerControllers serverControllers = EzyServerControllersImpl.builder().build();
		server.setControllers(serverControllers);
		EzyEventControllersSetting eventControllersSetting = new EzySimpleEventControllersSetting();
		EzyEventControllers eventControllers = EzyEventControllersImpl.create(eventControllersSetting);
		server.setEventControllers(eventControllers);
		server.setConfig(config);
		server.setSettings(settings);
		EzySimpleServerContext serverContext = new EzySimpleServerContext();
		serverContext.setProperty(EzySocketUserRemovalQueue.class, new EzyBlockingSocketUserRemovalQueue());
		serverContext.setServer(server);
		serverContext.init();
		
		ExBootstrap localBootstrap = new ExBootstrap(new EzyBootstrap.Builder()
				.context(serverContext));
		
		EzyNioServerBootstrap bootstrap = new EzyNioServerBootstrap();
		bootstrap.setContext(serverContext);
		bootstrap.setLocalBootstrap(localBootstrap);
		bootstrap.setSslContext(sslContext);
		bootstrap.setResponseApi(responseApi);
		bootstrap.setStreamingApi(streamingApi);
		bootstrap.setRequestQueues(requestQueues);
		bootstrap.setStreamQueue(streamQueue);
		bootstrap.setHandlerGroupManager(handlerGroupManager);
		bootstrap.setSocketSessionTicketsQueue(socketSessionTicketsQueue);
		bootstrap.setWebsocketSessionTicketsQueue(websocketSessionTicketsQueue);
		bootstrap.setSocketDisconnectionQueue(socketDisconnectionQueue);
		bootstrap.start();
		bootstrap.destroy();
		bootstrap.destroy();
		
	}
	
	public static class ExBootstrap extends EzyBootstrap {

		protected ExBootstrap(Builder builder) {
			super(builder);
		}
		
		@Override
		public void start() throws Exception {
		}
		
	}
	
	public static class ExServerReadyController implements EzyServerReadyController {

		@Override
		public void handle(EzyServerContext ctx, EzyServerReadyEvent event) {
		}
		
	}
	
}
