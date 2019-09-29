package yandex.cloud.kms.client.dto;

import lombok.Data;

@Data
public class DecryptResponse {
    String keyId;

    String versionId;

    byte[] plaintext;
}
