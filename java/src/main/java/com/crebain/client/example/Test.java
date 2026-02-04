package com.crebain.client.example;

import com.crebain.client.CrebainClient;
import com.crebain.client.exception.ApiException;
import com.crebain.client.model.*;
import com.crebain.client.request.SubmitEntityRequest;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.logging.*;

/**
 * Crebain API — Client Integration Example (Java)
 * <p>
 * Steps:
 * 1) Load config from environment variables
 * 2) Check/onboard an entity (idempotent)
 * 3) Print any available files and their signed URLs (if returned)
 * 4) Download files from signed URLs (optional)
 * 5) List entities (sanity check)
 * <p>
 * Run:
 *   export CREBAIN_API_KEY="ck_live_..."
 *   export CREBAIN_BASE_URL="https://<project-ref>.supabase.co/functions/v1/api"
 *   mvn exec:java -Dexec.mainClass="com.crebain.client.example.Test"
 * <p>
 * Or run the JAR:
 *   java -cp target/crebain-client-1.0.0.jar com.crebain.client.example.Test
 */
public class Test {

    private static final Logger logger = Logger.getLogger(Test.class.getName());

    // Configuration - set via environment variables or replace here
    private static final String CREBAIN_API_KEY = "";
    private static final String CREBAIN_BASE_URL = "";
    private static final String SUPABASE_ANON_KEY = "";

    public static void main(String[] args) {
        setupLogging();

        try {
            runExample();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Example failed", e);
            System.exit(1);
        }
    }

    private static void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("%1$tF %1$tT | %2$s | %3$s%n",
                        record.getMillis(), record.getLevel(), record.getMessage());
            }
        });

        rootLogger.getHandlers()[0].setLevel(Level.INFO);
    }

    private static Config loadConfig() {
        String apiKey = getEnvOrDefault("CREBAIN_API_KEY", CREBAIN_API_KEY).trim();
        String baseUrl = getEnvOrDefault("CREBAIN_BASE_URL", CREBAIN_BASE_URL).trim().replaceAll("/+$", "");
        String supabaseAnonKey = getEnvOrDefault("SUPABASE_ANON_KEY", SUPABASE_ANON_KEY).trim();
        String downloadDir = getEnvOrDefault("CREBAIN_DOWNLOAD_DIR", "downloads");
        int timeoutSeconds = Integer.parseInt(getEnvOrDefault("CREBAIN_TIMEOUT_SECONDS", "30"));

        // Validate API key
        if (apiKey.isEmpty()) {
            throw new IllegalArgumentException("Missing CREBAIN_API_KEY environment variable.");
        }
        if (!apiKey.startsWith("ck_")) {
            throw new IllegalArgumentException(String.format(
                    "Invalid CREBAIN_API_KEY: must start with 'ck_'.%n" +
                    "Got: %s...%n%n" +
                    "Fix:%n" +
                    "  export CREBAIN_API_KEY=\"ck_live_your_key_here\"",
                    apiKey.substring(0, Math.min(50, apiKey.length()))));
        }
        if (apiKey.contains(" ") || apiKey.contains("\t") || apiKey.contains("\n") || apiKey.contains("\r")) {
            throw new IllegalArgumentException("Invalid CREBAIN_API_KEY: contains whitespace/newlines.");
        }

        // Validate base URL
        if (baseUrl.isEmpty()) {
            throw new IllegalArgumentException("Missing CREBAIN_BASE_URL environment variable.");
        }
        if (!baseUrl.startsWith("https://")) {
            throw new IllegalArgumentException(String.format(
                    "Invalid CREBAIN_BASE_URL: must start with 'https://'.%n" +
                    "Got: %s...%n%n" +
                    "Fix:%n" +
                    "  export CREBAIN_BASE_URL=\"https://<project>.supabase.co/functions/v1/api\"",
                    baseUrl.substring(0, Math.min(50, baseUrl.length()))));
        }

        return new Config(apiKey, baseUrl, supabaseAnonKey, downloadDir, timeoutSeconds);
    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    private static void runExample() {
        Config cfg = loadConfig();

        logger.info("Loaded config");
        logger.info("   Base URL: " + cfg.baseUrl);
        logger.info("   Download dir: " + cfg.downloadDir);

        try (CrebainClient client = CrebainClient.builder()
                .apiKey(cfg.apiKey)
                .baseUrl(cfg.baseUrl)
                .supabaseAnonKey(cfg.supabaseAnonKey)
                .timeout(Duration.ofSeconds(cfg.timeoutSeconds))
                .build()) {

            String externalEntityId = "stenn";
            String entityName = "Stenn Technologies";
            Map<String, Object> metadata = Map.of("sector", "FinTech", "example_run", true);

            // STEP 1 — Entity submit / onboarding
            logger.info("");
            logger.info("STEP 1) Entity submit / onboarding");
            logger.info("────────────────────────────────────────────────────────");

            SubmitEntityRequest request = SubmitEntityRequest.builder()
                    .externalEntityId(externalEntityId)
                    .name(entityName)
                    .metadata(metadata)
                    .force(false)
                    .idempotencyKey("submit-" + externalEntityId + "-v1")
                    .build();

            EntitySubmitResult result = client.submitEntity(request);

            logger.info("Entity submit result");
            logger.info("   entity_id=" + result.getEntityId());
            logger.info("   new_company=" + result.isNewCompany());
            logger.info("   request_submitted=" + result.isRequestSubmitted());
            if (result.getAsyncRequestId() != null) {
                logger.info("   async_request_id=" + result.getAsyncRequestId());
            }

            // STEP 2 — Existing files (if any)
            logger.info("");
            logger.info("STEP 2) Files currently available (if returned)");
            logger.info("────────────────────────────────────────────────────────");

            var existingFiles = result.getExistingFiles();
            logger.info("   existing_files=" + existingFiles.size());

            for (FileItem f : existingFiles) {
                logger.info("   - " + f.getFilename() + " | mime=" + f.getMimeType() + " | file_id=" + f.getFileId());
                if (f.getSignedUrl() != null) {
                    String urlPreview = f.getSignedUrl().substring(0, Math.min(100, f.getSignedUrl().length()));
                    logger.info("     signed_url=" + urlPreview + "...");
                }
            }

            // STEP 3 — Download files from signed URLs (optional)
            logger.info("");
            logger.info("STEP 3) Download available files (if signed URLs exist)");
            logger.info("────────────────────────────────────────────────────────");
            downloadSignedFiles(existingFiles, cfg.downloadDir, cfg.timeoutSeconds);

            // STEP 4 — List entities (sanity check)
            logger.info("");
            logger.info("STEP 4) List entities (sanity check)");
            logger.info("────────────────────────────────────────────────────────");

            EntitiesPage entitiesPage = client.listEntities(20, null);
            logger.info("Returned " + entitiesPage.getEntities().size() + " entities (showing up to 20)");

            for (Entity e : entitiesPage.getEntities()) {
                logger.info("   - " + e.getName() + " | external=" + e.getExternalEntityId() + " | id=" + e.getId());
            }

            logger.info("");
            logger.info("Finished successfully.");

        } catch (ApiException e) {
            logger.severe("────────────────────────────────────────────────────────");
            logger.severe("API ERROR");
            logger.severe("   status=" + e.getStatusCode());
            logger.severe("   code=" + e.getCode());
            logger.severe("   request_id=" + e.getRequestId());
            logger.severe("   message=" + e.getErrorMessage());
            if (e.getResponseBody() != null) {
                logger.severe("   response_body=" + e.getResponseBody());
            }
            throw e;
        }
    }

    private static void downloadSignedFiles(java.util.List<FileItem> files, String outDir, int timeoutSeconds) {
        Path outPath = Path.of(outDir);

        try {
            Files.createDirectories(outPath);
        } catch (IOException e) {
            logger.warning("Failed to create download directory: " + e.getMessage());
            return;
        }

        if (files.isEmpty()) {
            logger.info("No downloadable files returned.");
            return;
        }

        logger.info("Downloading " + files.size() + " file(s) to " + outPath.toAbsolutePath());

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        for (int i = 0; i < files.size(); i++) {
            FileItem f = files.get(i);
            String signedUrl = f.getSignedUrl();
            String filename = f.getFilename() != null ? f.getFilename() : "file_" + (i + 1);

            if (signedUrl == null || signedUrl.isEmpty()) {
                logger.warning("Skipping " + filename + " (no signed_url).");
                continue;
            }

            Path destination = outPath.resolve(filename);
            logger.info("GET " + signedUrl.substring(0, Math.min(80, signedUrl.length())) + "...");

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(signedUrl))
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .GET()
                        .build();

                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

                if (response.statusCode() != 200) {
                    logger.warning("Download failed for " + filename + " (HTTP " + response.statusCode() + ").");
                    continue;
                }

                Files.write(destination, response.body());
                logger.info("Downloaded " + filename + " (" + response.body().length + " bytes).");

            } catch (Exception e) {
                logger.warning("Download exception for " + filename + ": " + e.getMessage());
            }
        }
    }

    private record Config(String apiKey, String baseUrl, String supabaseAnonKey, String downloadDir, int timeoutSeconds) {}
}
