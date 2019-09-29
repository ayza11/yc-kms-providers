package yandex.cloud.kms.providers.awsEncryption;

import com.amazonaws.encryptionsdk.*;
import com.amazonaws.encryptionsdk.exception.AwsCryptoException;
import com.amazonaws.encryptionsdk.exception.NoSuchMasterKeyException;
import com.amazonaws.encryptionsdk.exception.UnsupportedProviderException;
import yandex.cloud.kms.client.KmsCryptoClientImpl;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class YcKmsMasterKeyProvider extends MasterKeyProvider<YcKmsMasterKey> {
    static final String PROVIDER_NAME = "yc-kms";

    private final String host;
    private final int port;
    private final String authHeader;
    private final Set<String> keyIds;

    public static YcKmsMasterKeyProviderBuilder builder() {
        return new YcKmsMasterKeyProviderBuilder();
    }

    YcKmsMasterKeyProvider(String host, int port, String authHeader, Set<String> keyIds) {
        this.host = host;
        this.port = port;
        this.authHeader = authHeader;
        this.keyIds = Collections.unmodifiableSet(keyIds);
    }

    @Override
    public String getDefaultProviderId() {
        return PROVIDER_NAME;
    }

    @Override
    public YcKmsMasterKey getMasterKey(String provider, String keyId)
            throws UnsupportedProviderException, NoSuchMasterKeyException {

        validateProvider(provider);
        return new YcKmsMasterKey(keyId,
                new KmsCryptoClientImpl(host, port, authHeader));
    }

    @Override
    public List<YcKmsMasterKey> getMasterKeysForEncryption(MasterKeyRequest request) {
        return keyIds.stream()
                .map(keyStr -> getMasterKey(PROVIDER_NAME, keyStr))
                .collect(Collectors.toList());
    }

    @Override
    public DataKey<YcKmsMasterKey> decryptDataKey(CryptoAlgorithm algorithm,
                                                  Collection<? extends EncryptedDataKey> encryptedDataKeys,
                                                  Map<String, String> encryptionContext)
            throws AwsCryptoException {

        for (EncryptedDataKey encryptedKey : encryptedDataKeys) {
            String keyId = new String(encryptedKey.getProviderInformation(), StandardCharsets.UTF_8);
            MasterKey<YcKmsMasterKey> key = getMasterKey(PROVIDER_NAME, keyId);
            DataKey<YcKmsMasterKey> decryptedKey = key.decryptDataKey(algorithm, encryptedDataKeys, encryptionContext);
            if (decryptedKey != null) {
                return decryptedKey;
            }
        }
        return null;
    }

    private static void validateProvider(String provider) {
        if (!PROVIDER_NAME.equals(provider)) {
            throw new UnsupportedProviderException(String.format("Provider %s not supported", provider));
        }
    }
}
