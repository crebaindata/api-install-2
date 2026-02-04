package com.crebain.client.example;

import com.crebain.client.CrebainClient;
import com.crebain.client.model.WebhookSubscription;
import com.crebain.client.webhook.WebhookVerifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.logging.*;

/**
 * Minimal webhook example for Crebain API (Java).
 * <p>
 * Usage:
 *   1. Build: mvn package
 *   2. Run server: java -cp target/crebain-client-1.0.0.jar com.crebain.client.example.TestWebhook
 *   3. Expose with ngrok: ngrok http 5001
 *   4. Register webhook: java -cp target/crebain-client-1.0.0.jar com.crebain.client.example.TestWebhook --register https://xxx.ngrok.io/webhook
 * <p>
 * Commands:
 *   (no args)           - Start webhook server on port 5001
 *   --register <url>    - Register a webhook with the given URL
 *   --list              - List all registered webhooks
 */
public class TestWebhook {

    private static final Logger logger = Logger.getLogger(TestWebhook.class.getName());
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Configuration - set via environment variables
    private static final String WEBHOOK_SECRET = getEnvOrDefault("WEBHOOK_SECRET", "");
    private static final String CREBAIN_API_KEY = getEnvOrDefault("CREBAIN_API_KEY", "");
    private static final String CREBAIN_BASE_URL = getEnvOrDefault("CREBAIN_BASE_URL", "");
    private static final String SUPABASE_ANON_KEY = getEnvOrDefault("SUPABASE_ANON_KEY", "");

    public static void main(String[] args) {
        setupLogging();

        if (args.length > 0) {
            String command = args[0];

            if ("--register".equals(command) && args.length > 1) {
                // Register webhook
                String webhookUrl = args[1];
                registerWebhook(webhookUrl);
            } else if ("--list".equals(command)) {
                // List webhooks
                listWebhooks();
            } else {
                printUsage();
            }
        } else {
            // Start the webhook server
            startServer();
        }
    }

    private static void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);

        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.INFO);
            handler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("%1$tF %1$tT | %2$s%n", record.getMillis(), record.getMessage());
                }
            });
        }
    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    private static void startServer() {
        int port = Integer.parseInt(getEnvOrDefault("PORT", "5001"));

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/webhook", TestWebhook::handleWebhook);
            server.createContext("/health", TestWebhook::handleHealth);
            server.setExecutor(null);

            logger.info("Starting webhook server on http://localhost:" + port);
            logger.info("Webhook endpoint: http://localhost:" + port + "/webhook");
            logger.info("");
            logger.info("To expose publicly, run: ngrok http " + port);
            logger.info("Then register: java -cp target/crebain-client-1.0.0.jar " +
                    "com.crebain.client.example.TestWebhook --register https://xxx.ngrok.io/webhook");

            server.start();
        } catch (IOException e) {
            logger.severe("Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleWebhook(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            return;
        }

        // Get signature headers
        String timestamp = exchange.getRequestHeaders().getFirst("X-Crebain-Timestamp");
        String signature = exchange.getRequestHeaders().getFirst("X-Crebain-Signature");

        // Read raw body
        byte[] rawBody = exchange.getRequestBody().readAllBytes();

        logger.info("==================================================");
        logger.info("Received webhook");
        logger.info("  Timestamp: " + timestamp);
        logger.info("  Signature: " + (signature != null ? signature.substring(0, Math.min(50, signature.length())) + "..." : null));

        // Verify signature
        if (!WebhookVerifier.verify(WEBHOOK_SECRET, timestamp, rawBody, signature)) {
            logger.severe("  Signature: INVALID");
            sendResponse(exchange, 401, "{\"error\": \"Invalid signature\"}");
            return;
        }

        logger.info("  Signature: VALID");

        // Parse and log the event
        String bodyString = new String(rawBody, StandardCharsets.UTF_8);
        JsonObject event = JsonParser.parseString(bodyString).getAsJsonObject();

        String eventType = event.has("event") ? event.get("event").getAsString() : null;
        String requestId = event.has("request_id") ? event.get("request_id").getAsString() : null;
        String orgId = event.has("org_id") ? event.get("org_id").getAsString() : null;
        String kind = event.has("kind") ? event.get("kind").getAsString() : null;

        logger.info("  Event: " + eventType);
        logger.info("  Request ID: " + requestId);
        logger.info("  Org ID: " + orgId);
        logger.info("  Kind: " + kind);

        if ("request.complete".equals(eventType)) {
            if (event.has("result") && !event.get("result").isJsonNull()) {
                String resultStr = gson.toJson(event.get("result"));
                logger.info("  Result: " + resultStr.substring(0, Math.min(500, resultStr.length())));
            }

            // Here you would typically:
            // 1. Update your database
            // 2. Notify your users
            // 3. Trigger downstream processing
        }

        logger.info("==================================================");
        sendResponse(exchange, 200, "{\"status\": \"ok\"}");
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 200, "{\"status\": \"healthy\"}");
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static void registerWebhook(String webhookUrl) {
        if (CREBAIN_API_KEY.isEmpty() || CREBAIN_BASE_URL.isEmpty()) {
            logger.severe("Missing CREBAIN_API_KEY or CREBAIN_BASE_URL environment variables");
            System.exit(1);
        }

        if (WEBHOOK_SECRET.isEmpty()) {
            logger.severe("Missing WEBHOOK_SECRET environment variable");
            System.exit(1);
        }

        try (CrebainClient client = CrebainClient.builder()
                .apiKey(CREBAIN_API_KEY)
                .baseUrl(CREBAIN_BASE_URL)
                .supabaseAnonKey(SUPABASE_ANON_KEY)
                .timeout(Duration.ofSeconds(30))
                .build()) {

            WebhookSubscription webhook = client.createWebhook(webhookUrl, WEBHOOK_SECRET);

            logger.info("Webhook registered successfully!");
            logger.info("  Webhook ID: " + webhook.getId());
            logger.info("  URL: " + webhook.getUrl());

        } catch (Exception e) {
            logger.severe("Failed to register webhook: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void listWebhooks() {
        if (CREBAIN_API_KEY.isEmpty() || CREBAIN_BASE_URL.isEmpty()) {
            logger.severe("Missing CREBAIN_API_KEY or CREBAIN_BASE_URL environment variables");
            System.exit(1);
        }

        try (CrebainClient client = CrebainClient.builder()
                .apiKey(CREBAIN_API_KEY)
                .baseUrl(CREBAIN_BASE_URL)
                .supabaseAnonKey(SUPABASE_ANON_KEY)
                .timeout(Duration.ofSeconds(30))
                .build()) {

            List<WebhookSubscription> webhooks = client.listWebhooks();

            logger.info("Registered webhooks:");
            for (WebhookSubscription wh : webhooks) {
                logger.info("  - " + wh.getId() + ": " + wh.getUrl() + " (enabled: " + wh.isEnabled() + ")");
            }

        } catch (Exception e) {
            logger.severe("Failed to list webhooks: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java -cp target/crebain-client-1.0.0.jar com.crebain.client.example.TestWebhook");
        System.out.println("      Start webhook server");
        System.out.println("");
        System.out.println("  java -cp target/crebain-client-1.0.0.jar com.crebain.client.example.TestWebhook --register <url>");
        System.out.println("      Register webhook");
        System.out.println("");
        System.out.println("  java -cp target/crebain-client-1.0.0.jar com.crebain.client.example.TestWebhook --list");
        System.out.println("      List webhooks");
    }
}
