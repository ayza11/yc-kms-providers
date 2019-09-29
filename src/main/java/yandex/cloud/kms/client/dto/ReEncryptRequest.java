package yandex.cloud.kms.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReEncryptRequest {
    String keyId;

    String versionId;

    byte[] aadContext;

    String sourceKeyId;

    byte[] sourceAadContext;

    byte[] ciphertext;
}
