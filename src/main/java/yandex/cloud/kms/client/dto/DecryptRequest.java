package yandex.cloud.kms.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DecryptRequest {
    String keyId;

    byte[] aadContext;

    byte[] ciphertext;
}
