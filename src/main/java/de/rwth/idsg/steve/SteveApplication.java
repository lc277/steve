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
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.logging.Slf4jLogger;
import org.joda.time.DateTimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 19.09.2025
 */
@SpringBootApplication
public class SteveApplication {

    private static final String NO_PROMPT_BIND_ARG = "--no-prompt-bind";

    /** Kept for backwards compatibility; prompt is already the default. Stripped from args if present. */
    private static final String PROMPT_BIND_ARG_LEGACY = "--prompt-bind";

    static {
        // Apache CXF
        LogUtils.setLoggerClass(Slf4jLogger.class);

        // For Hibernate validator
        System.setProperty("org.jboss.logging.provider", "slf4j");

        TimeZone.setDefault(TimeZone.getTimeZone(SteveProperties.TIME_ZONE_ID));
        DateTimeZone.setDefault(DateTimeZone.forID(SteveProperties.TIME_ZONE_ID));
    }

    public static void main(String[] args) throws Exception {
        start(args);
    }

    public static ConfigurableApplicationContext start(String... args) throws Exception {

        Enumeration<NetworkInterface> enu = null;
        try {
            enu = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        assert enu != null;
        ArrayList<NetworkInterface> arr = Collections.list(enu);

        System.out.println();
        for (NetworkInterface o : arr) {
            String intName = o.getName();
            String IfName = o.getDisplayName();
            ArrayList<InetAddress> inets = Collections.list(o.getInetAddresses());
            for (InetAddress inet : inets) {
                if (inet instanceof Inet4Address) {
                    String ipp = inet.getHostAddress();
//                    System.out.printf("%-10s %-5s %-6s %-15s\n", "InterfaceName:", intName, "| IPv4:", ipp);
                    System.out.printf("%-6s %-15s %s %-5s %s %-5s\n", "IPv4:", ipp, "|", intName, "|", IfName);
                }
            }
        }

        String[] runArgs = prepareArgsWithOptionalBindPrompt(args);
        SpringApplication application = new SpringApplication(SteveApplication.class);
        application.addListeners(new SteveApplicationStartupListener());
        SteveProdStarter starter = new SteveProdStarter();
        starter.starting();
        try {
            ConfigurableApplicationContext ctx = application.run(runArgs);
            ServerProperties serverProperties = ctx.getBean(ServerProperties.class);
            starter.started(SteveStartupUrls.endpointInfo(serverProperties), SteveStartupUrls.managerUrl(serverProperties));
            return ctx;
        } catch (Exception e) {
            starter.failed();
            throw e;
        }
    }

    /**
     * By default, reads bind IP and HTTP port from stdin before startup (overrides {@code server.host} /
     * {@code http.port}). To use only configuration files (e.g. Docker, CI, tests), use {@code --no-prompt-bind},
     * or {@code -Dsteve.no-prompt-bind=true}, or env {@code STEVE_NO_PROMPT_BIND=true}.
     * Legacy {@code --prompt-bind} is stripped and has no effect.
     */
    private static String[] prepareArgsWithOptionalBindPrompt(String[] args) throws IOException {
        boolean skipPrompt = Boolean.parseBoolean(System.getProperty("steve.no-prompt-bind", "false"))
            || Boolean.parseBoolean(System.getenv().getOrDefault("STEVE_NO_PROMPT_BIND", "false"));
        List<String> runArgs = new ArrayList<>(args.length);
        for (String a : args) {
            if (NO_PROMPT_BIND_ARG.equals(a)) {
                skipPrompt = true;
            } else if (!PROMPT_BIND_ARG_LEGACY.equals(a)) {
                runArgs.add(a);
            }
        }
        if (!skipPrompt) {
            applyBindFromPrompt();
        }
        return runArgs.toArray(String[]::new);
    }

    private static void applyBindFromPrompt() throws IOException {
        System.out.println("Interactive bind: press Enter to keep the suggested default.");
        System.out.println("(Use --no-prompt-bind or STEVE_NO_PROMPT_BIND=true to skip and use application properties.)");
        String defaultIp = "127.0.0.1";
        String defaultPort = "8080";

        String ip;
        String portStr;
        java.io.Console console = System.console();
        if (console != null) {
            ip = console.readLine("Bind IP (server.host) [%s]: ", defaultIp);
            portStr = console.readLine("HTTP port (http.port) [%s]: ", defaultPort);
        } else {
            Charset charset = Charset.defaultCharset();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in, charset));
            System.out.printf("Bind IP (server.host) [%s]: %n", defaultIp);
            System.out.flush();
            ip = in.readLine();
            System.out.printf("HTTP port (http.port) [%s]: %n", defaultPort);
            System.out.flush();
            portStr = in.readLine();
        }

        if (ip == null || ip.isBlank()) {
            ip = defaultIp;
        } else {
            ip = ip.trim();
        }
        if (portStr == null || portStr.isBlank()) {
            portStr = defaultPort;
        } else {
            portStr = portStr.trim();
        }

        try {
            int p = Integer.parseInt(portStr);
            if (p < 1 || p > 65535) {
                throw new IllegalArgumentException("port out of range: " + p);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid HTTP port: " + portStr, e);
        }

        System.setProperty("server.host", ip);
        System.setProperty("http.port", portStr);
        System.out.println("Using bind " + ip + " and HTTP port " + portStr + ".");
    }

}
