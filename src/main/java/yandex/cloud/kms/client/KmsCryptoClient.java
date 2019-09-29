package yandex.cloud.kms.client;

import yandex.cloud.kms.client.dto.*;

public interface KmsCryptoClient {

    EncryptResponse encrypt(EncryptRequest request);

    DecryptResponse decrypt(DecryptRequest request);

    ReEncryptResponse reEncrypt(ReEncryptRequest request);

    GenerateDataKeyResponse generateDataKey(GenerateDataKeyRequest request);

}
