package yandex.cloud.kms.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EncryptResponse {
    String keyId;

    String versionId;

    byte[] ciphertext;
}
