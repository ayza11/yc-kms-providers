package yandex.cloud.kms.providers.awsEncryption;

import com.amazonaws.encryptionsdk.CryptoAlgorithm;
import com.amazonaws.encryptionsdk.DataKey;
import com.amazonaws.encryptionsdk.EncryptedDataKey;
import com.amazonaws.encryptionsdk.MasterKey;
import com.amazonaws.encryptionsdk.exception.AwsCryptoException;
import com.amazonaws.encryptionsdk.exception.UnsupportedProviderException;
import org.apache.commons.lang3.NotImplementedException;
import yandex.cloud.kms.client.KmsCryptoClient;
import yandex.cloud.kms.client.dto.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class YcKmsMasterKey extends MasterKey<YcKmsMasterKey> {

    private KmsCryptoClient kmsCryptoClient;
    private String keyId;


    YcKmsMasterKey(String keyId, KmsCryptoClient kmsCryptoClient) {
        this.keyId = keyId;
        this.kmsCryptoClient = kmsCryptoClient;
    }

    @Override
    public String getProviderId() {
        return YcKmsMasterKeyProvider.PROVIDER_NAME;
    }

    @Override
    public String getKeyId() {
        return keyId;
    }

    @Override
    public DataKey<YcKmsMasterKey> generateDataKey(CryptoAlgorithm awsAlgorithm, Map<String, String> encryptionContext) {
        SymmetricAlgorithm algorithm = convertAlgorithm(awsAlgorithm);
        GenerateDataKeyRequest req = GenerateDataKeyRequest.builder()
                .keyId(keyId)
                .dataKeySpec(algorithm)
                .aadContext(encryptionContext == null ? null : convertEncryptionContext(encryptionContext))
                .skipPlaintext(false)
                .build();
        GenerateDataKeyResponse resp = kmsCryptoClient.generateDataKey(req);
        SecretKey secretKey = new SecretKeySpec(resp.getDataKeyPlaintext(), awsAlgorithm.getDataKeyAlgo());
        return new DataKey<>(secretKey, resp.getDataKeyCiphertext(),
                keyId.getBytes(StandardCharsets.UTF_8), this);
    }

    @Override
    public DataKey<YcKmsMasterKey> encryptDataKey(CryptoAlgorithm algorithm, Map<String, String> encryptionContext,
                                                  DataKey<?> dataKey) {
        // let's just ignore the algorithm param
        EncryptRequest req = EncryptRequest.builder()
                .keyId(keyId)
                .aadContext(encryptionContext == null ? null : convertEncryptionContext(encryptionContext))
                .plaintext(dataKey.getKey().getEncoded())
                .build();
        EncryptResponse resp = kmsCryptoClient.encrypt(req);
        return new DataKey<>(dataKey.getKey(), resp.getCiphertext(),
                keyId.getBytes(StandardCharsets.UTF_8), this);
    }

    @Override
    public DataKey<YcKmsMasterKey> decryptDataKey(CryptoAlgorithm algorithm, Collection<? extends EncryptedDataKey> encryptedDataKeys, Map<String, String> encryptionContext) throws UnsupportedProviderException, AwsCryptoException {
        // let's just ignore algorithm param
        for (EncryptedDataKey encryptedKey : encryptedDataKeys) {
            DecryptRequest req = DecryptRequest.builder()
                    .keyId(keyId)
                    .aadContext(encryptionContext == null ? null : convertEncryptionContext(encryptionContext))
                    .ciphertext(encryptedKey.getEncryptedDataKey())
                    .build();
            DecryptResponse resp;
            try {
                resp = kmsCryptoClient.decrypt(req);
                SecretKey secretKey = new SecretKeySpec(resp.getPlaintext(), algorithm.getDataKeyAlgo());
                return new DataKey<>(secretKey, encryptedKey.getEncryptedDataKey(),
                        keyId.getBytes(StandardCharsets.UTF_8), this);
            } catch (RuntimeException ex) {
                // let's try another encrypted key

            }
        }
        return null; // unable to decrypt any of given keys
    }

    private static SymmetricAlgorithm convertAlgorithm(CryptoAlgorithm algorithm) {
        switch (algorithm) {
            case ALG_AES_128_GCM_IV12_TAG16_NO_KDF:
            case ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256: //FIXME
            case ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256: //FIXME
                return SymmetricAlgorithm.AES_128;
            case ALG_AES_192_GCM_IV12_TAG16_NO_KDF:
            case ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256: //FIXME
            case ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384: //FIXME
                return SymmetricAlgorithm.AES_192;
            case ALG_AES_256_GCM_IV12_TAG16_NO_KDF:
            case ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256: //FIXME
            case ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384: //FIXME
                return SymmetricAlgorithm.AES_256;
            default:
                throw new NotImplementedException(String.format("Algorithm %s not supported by YC KMS",
                        algorithm.name()));
        }
    }

    private static byte[] convertEncryptionContext(Map<String, String> context) {
        return context.entrySet().stream()
                .map(entry -> String.format("%s:%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(","))
                .getBytes(StandardCharsets.UTF_8);
    }
}
