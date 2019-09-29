package yandex.cloud.kms.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerateDataKeyRequest {
    String keyId;

    String versionId;

    byte[] aadContext;

    SymmetricAlgorithm dataKeySpec;

    boolean skipPlaintext;
}
