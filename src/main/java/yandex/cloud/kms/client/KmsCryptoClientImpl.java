package yandex.cloud.kms.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import yandex.cloud.kms.client.dto.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class KmsCryptoClientImpl implements KmsCryptoClient {

    private static final String ENCRYPT = "encrypt";
    private static final String DECRYPT = "decrypt";
    private static final String RE_ENCRYPT = "reEncrypt";
    private static final String GENERATE_DATA_KEY = "generateDataKey";

    private static final ObjectMapper objMapper = new DefaultObjectMapper();

    private final String kmsHost;
    private final int kmsPort;
    private final String authToken;

    private final CloseableHttpClient httpClient;

    public KmsCryptoClientImpl(String kmsHost, int kmsPort, String authToken) {
        this.kmsHost = kmsHost;
        this.kmsPort = kmsPort;
        this.authToken = authToken;

        this.httpClient = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                .build();
    }

    @Override
    public EncryptResponse encrypt(EncryptRequest request) {
        return invokeOp(ENCRYPT, request.getKeyId(), request, EncryptResponse.class);
    }

    @Override
    public DecryptResponse decrypt(DecryptRequest request) {
        return invokeOp(DECRYPT, request.getKeyId(), request, DecryptResponse.class);
    }

    @Override
    public ReEncryptResponse reEncrypt(ReEncryptRequest request) {
        return invokeOp(RE_ENCRYPT, request.getKeyId(), request, ReEncryptResponse.class);
    }

    @Override
    public GenerateDataKeyResponse generateDataKey(GenerateDataKeyRequest request) {
        return invokeOp(GENERATE_DATA_KEY, request.getKeyId(), request, GenerateDataKeyResponse.class);
    }

    // ---

    private <REQ, RESP> RESP invokeOp(String operation, String keyId, REQ request, Class<RESP> respClass) {
        try {
            String response = doPost(getUri(keyId, operation), objMapper.writeValueAsString(request));
            return objMapper.readValue(response, respClass);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while processing JSON", e);
        }

    }

    private String doPost(String uri, String request) {
        HttpPost post = new HttpPost(uri);
        post.addHeader(HttpHeaders.AUTHORIZATION, getAuthToken());
        post.setEntity(new StringEntity(request, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            int statusCode = response.getStatusLine().getStatusCode();

            Optional<String> responseBody = Optional.empty();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream inStream = entity.getContent()) {
                    responseBody = Optional.of(new String(
                            IOUtils.readFully(inStream, (int) entity.getContentLength()),
                            StandardCharsets.UTF_8));
                }
            }

            if (statusCode == HttpStatus.SC_OK) {
                return responseBody.orElse("");
            } else {
                String message = "";
                if (responseBody.isPresent()) {
                    try {
                        message = objMapper.readValue(responseBody.get(), ErrorResponse.class).getError();
                    } catch (Exception e) {}
                }
                throw new KmsException(statusCode, message);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            post.reset();
        }
    }

    private String getUri(String keyId, String operation) {
        return String.format("https://%s:%d/kms/v1/keys/%s:%s",
                kmsHost,
                kmsPort,
                keyId,
                operation);
    }

    private String getAuthToken() {
        return "Bearer " + authToken;
    }

    // ---

    @Value
    private static class KmsException extends RuntimeException {
        int httpStatus;
        String message;
    }
}
