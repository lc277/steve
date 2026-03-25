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
package de.rwth.idsg.steve;

import de.rwth.idsg.steve.config.SteveProperties;
import de.rwth.idsg.steve.web.dto.EndpointInfo;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;

import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * Builds the same URLs as the About page ({@link de.rwth.idsg.steve.web.controller.AboutSettingsController}) for console output after startup.
 */
public final class SteveStartupUrls {

    private SteveStartupUrls() {
    }

    public static EndpointInfo endpointInfo(ServerProperties serverProperties) {
        String scheme = httpScheme(serverProperties);
        int port = serverProperties.getPort() != null ? serverProperties.getPort() : 8080;
        String hostHeader = clientFacingHost(serverProperties, port);
        String contextPath = serverProperties.getServlet().getContextPath();
        return EndpointInfo.fromRequest(scheme, hostHeader, contextPath);
    }

    public static String managerUrl(ServerProperties serverProperties) {
        String scheme = httpScheme(serverProperties);
        int port = serverProperties.getPort() != null ? serverProperties.getPort() : 8080;
        String hostHeader = clientFacingHost(serverProperties, port);
        String contextPath = serverProperties.getServlet().getContextPath();
        return scheme + "://" + hostHeader + contextPath + SteveProperties.SPRING_MANAGER_MAPPING;
    }

    private static String httpScheme(ServerProperties serverProperties) {
        return serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled() ? "https" : "http";
    }

    /**
     * Host[:port] as in the HTTP {@code Host} header.
     */
    private static String clientFacingHost(ServerProperties serverProperties, int port) {
        InetAddress addr = serverProperties.getAddress();
        String host;
        if (addr == null) {
            host = "localhost";
        } else if (addr.isAnyLocalAddress()) {
            host = "localhost";
        } else if (addr instanceof Inet6Address) {
            host = "[" + addr.getHostAddress() + "]";
        } else {
            host = addr.getHostAddress();
        }

        boolean https = serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled();
        boolean showPort = (https && port != 443) || (!https && port != 80);
        return showPort ? host + ":" + port : host;
    }
}
