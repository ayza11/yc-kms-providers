package yandex.cloud.kms.providers.awsEncryption;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.Set;

public class YcKmsMasterKeyProviderBuilder {
    private String host;
    private int port;
    private String authHeader;
    private Set<String> keyIds;

    public YcKmsMasterKeyProviderBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public YcKmsMasterKeyProviderBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public YcKmsMasterKeyProviderBuilder setAuthHeader(String header) {
        this.authHeader = header;
        return this;
    }

    public YcKmsMasterKeyProviderBuilder setKeyIds(Set<String> keyIds) {
        this.keyIds = keyIds;
        return this;
    }

    public YcKmsMasterKeyProviderBuilder setKeyId(String keyId) {
        this.keyIds = Collections.singleton(keyId);
        return this;
    }


    public YcKmsMasterKeyProvider build() {
        Validate.isTrue(!keyIds.isEmpty(), "no key ids given");
        Validate.isTrue(StringUtils.isNotBlank(host), "no host given");
        Validate.isTrue(port > 0, "no port given");
        return new YcKmsMasterKeyProvider(host, port, authHeader, keyIds);
    }
}
