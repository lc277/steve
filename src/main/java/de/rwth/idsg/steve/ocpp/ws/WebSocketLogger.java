/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2026 SteVe Community Team
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.ocpp.ws;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.nio.channels.ClosedChannelException;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 10.05.2018
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketLogger {

    public static void connected(String chargeBoxId, WebSocketSession session) {
        log.info("[{} Connection is established", chargeBoxId, session.getId());
    }

    public static void closed(String chargeBoxId, WebSocketSession session, CloseStatus closeStatus) {
        log.warn("[{} Connection is closed, status: {}", chargeBoxId, closeStatus);
    }

    public static void sending(String chargeBoxId, WebSocketSession session, String msg) {
        log.info("[{} Send: {}", chargeBoxId, msg);
    }

    public static void willNotSend(String chargeBoxId, WebSocketSession session, String msg) {
        log.warn("[{} Attempted to send to closed session: {}", chargeBoxId, msg);
    }

    public static void sendingPing(String chargeBoxId, WebSocketSession session) {
        log.debug("[{} Send ping message", chargeBoxId, session.getId());
    }

    public static void receivedPong(String chargeBoxId, WebSocketSession session) {
        log.debug("[{} Recv pong message", chargeBoxId, session.getId());
    }

    public static void receivedText(String chargeBoxId, WebSocketSession session, String msg) {
        log.info("[{} Recv: {}", chargeBoxId, msg);
    }

    public static void receivedEmptyText(String chargeBoxId, WebSocketSession session) {
        log.warn("[{} Recv empty text message. Will pretend this never happened.", chargeBoxId, session.getId());
    }

    public static void pingError(String chargeBoxId, WebSocketSession session, Throwable t) {
        log.error("[{} Ping error", chargeBoxId, t);
    }

    public static void transportError(String chargeBoxId, WebSocketSession session, Throwable t) {
        // https://github.com/steve-community/steve/issues/1913
        //
        // Clients can disconnect abruptly at any moment without warning, especially in mobile environments or unstable
        // networks. ClosedChannelException is Jetty's way of notifying that the connection ended unexpectedly. This can
        // be seen as normal behavior in WebSocket applications. No need to print stacktrace (which is useless anyway).
        if (t instanceof ClosedChannelException) {
            log.warn("[{} Connection ended unexpectedly", chargeBoxId, session.getId());
        } else {
            log.error("[{} Transport error", chargeBoxId, t);
        }
    }

    public static void closingDangling(String chargeBoxId, WebSocketSession session) {
        log.warn("[{} Closing a dangling WebSocketSession", chargeBoxId, session.getId());
    }

    public static void closingDanglingError(String chargeBoxId, WebSocketSession session, Throwable t) {
        log.error("[{} Error while trying to close the WebSocketSession", chargeBoxId, t);
    }
}
