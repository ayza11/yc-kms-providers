package yandex.cloud.kms.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EncryptRequest {
    String keyId;

    String versionId;

    byte[] aadContext;

    byte[] plaintext;
}
