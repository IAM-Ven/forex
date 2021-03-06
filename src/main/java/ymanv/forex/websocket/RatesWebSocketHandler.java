/**
 * Copyright (C) 2015 https://github.com/ymanv
 *
 * This software is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ymanv.forex.websocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import ymanv.forex.event.RatesUpdatedEvent;
import ymanv.forex.model.entity.rate.RateEntity;

/**
 * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/
 * websocket.html
 */
@Component
public class RatesWebSocketHandler extends TextWebSocketHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RatesWebSocketHandler.class);

	@Autowired
	private ObjectMapper mapper;

	private final Set<WebSocketSession> sessions = new HashSet<>();

	@Autowired
	private EventBus bus;

	@PostConstruct
	private void registerToBus() {
		bus.register(this);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		LOG.debug("Opened new {} in instance {}", session.getClass().getSimpleName(), session.getRemoteAddress());
		sessions.add(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		LOG.debug("Closed {} in instance {}", session.getClass().getSimpleName(), session.getRemoteAddress());
		sessions.remove(session);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws IOException {
		session.close(CloseStatus.SERVER_ERROR);
	}

	@Subscribe
	public void send(RatesUpdatedEvent event) throws IOException {
		if(sessions.isEmpty()) {
			return;
		}
		
		String txt = serializeRates(event.getRates());

		for (WebSocketSession wss : new CopyOnWriteArrayList<>(sessions)) {
			wss.sendMessage(new TextMessage(txt));
		}
	}

	@VisibleForTesting
	protected String serializeRates(List<? extends RateEntity> list) throws JsonProcessingException {
		return mapper.writeValueAsString(list);
	}
}