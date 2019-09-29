package yandex.cloud.kms.providers.examples;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import yandex.cloud.kms.providers.awsEncryption.YcKmsMasterKey;
import yandex.cloud.kms.providers.awsEncryption.YcKmsMasterKeyProvider;
import yandex.cloud.kms.providers.awsEncryption.YcKmsMasterKeyProviderBuilder;

import java.util.Collections;
import java.util.Map;

public class AwsEncryptionStringExample {

    private static final String YCKMS_HOST = "YCKMS_HOST";
    private static final String YCKMS_PORT = "YCKMS_PORT";
    private static final String YCKMS_AUTH = "YCKMS_AUTH";

    public static void main(final String[] args) {

        Validate.isTrue(args.length >= 2, "Invalid number of arguments");
        String keyId = args[0];
        String data = args[1];

        AwsCrypto crypto = new AwsCrypto();

        YcKmsMasterKeyProvider prov = setupYcKmsKeyProvider()
                .setKeyId(keyId)
                .build();

        Map<String, String> context = Collections.singletonMap("Foo", "Bar");

        String ciphertext = crypto.encryptString(prov, data, context).getResult();
        System.out.println("Ciphertext: " + ciphertext);
        System.out.println("Ciphertext length: " + ciphertext.length());

        // Decrypt the data
        CryptoResult<String, YcKmsMasterKey> decryptResult = crypto.decryptString(prov, ciphertext);

        if (!data.equals(decryptResult.getResult())) {
            System.out.println("Decrypted text differs from original plaintext!!");
        }

        if (!decryptResult.getMasterKeyIds().get(0).equals(keyId)) {
            throw new IllegalStateException("Key id mismatch");
        }

        for (Map.Entry<String, String> e : context.entrySet()) {
            if (!e.getValue().equals(decryptResult.getEncryptionContext().get(e.getKey()))) {
                throw new IllegalStateException("Encryption Context mismatch");
            }
        }

        System.out.println("Decrypted: " + decryptResult.getResult());
    }

    private static YcKmsMasterKeyProviderBuilder setupYcKmsKeyProvider() {
        String hostname = System.getenv(YCKMS_HOST);
        Validate.isTrue(StringUtils.isNotBlank(hostname), "YC KMS hostname is empty");

        int port;
        try {
            port = Integer.parseInt(System.getenv(YCKMS_PORT));
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("YC KMS port is missing or invalid");
        }

        String auth = System.getenv(YCKMS_AUTH);
        Validate.isTrue(StringUtils.isNotBlank(auth), "YC KMS authentication header data is empty");

        return YcKmsMasterKeyProvider.builder()
                .setHost(hostname)
                .setPort(port)
                .setAuthHeader(auth);
    }

}
