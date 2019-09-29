package yandex.cloud.kms.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReEncryptResponse {
    String keyId;

    String versionId;

    String sourceKeyId;

    String sourceVersionId;

    byte[] ciphertext;
}
