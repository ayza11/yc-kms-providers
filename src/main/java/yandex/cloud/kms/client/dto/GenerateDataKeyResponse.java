package yandex.cloud.kms.client.dto;

import lombok.Data;

@Data
public class GenerateDataKeyResponse {
    String keyId;

    String versionId;

    byte[] dataKeyPlaintext;

    byte[] dataKeyCiphertext;
}
