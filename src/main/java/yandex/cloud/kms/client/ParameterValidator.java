package yandex.cloud.kms.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

class ParameterValidator {

    private static final int MAX_ID_LENGTH = 50;
    private static final int MAX_AAD_LENGTH = 8192;
    private static final int MAX_PLAINTEXT_LENGTH = 1024 * 32;
    private static final int MAX_CIPHERTEXT_LENGTH = (int) (MAX_PLAINTEXT_LENGTH * 1.5);

    // 1000 is less than the sum of all field names, whitespace and punctuation.
    static final int MAX_CONTENT_LENGTH = 3 * MAX_ID_LENGTH + 2 * MAX_AAD_LENGTH + MAX_CIPHERTEXT_LENGTH + 1000;

    static void validateId(String id, boolean required) {
        if (required) {
            Validate.isTrue(StringUtils.isNotBlank(id), "id is empty");
        }
        if (id != null) {
            Validate.isTrue(id.length() <= MAX_ID_LENGTH, "id is longer than " + MAX_ID_LENGTH + " characters");
        }
    }

    static void validateAad(byte[] aad) {
        if (aad != null) {
            Validate.isTrue(aad.length <= MAX_AAD_LENGTH, "add context is longer than " +
                    MAX_AAD_LENGTH + " bytes");
        }
    }

    static void validatePlaintext(byte[] plaintext) {
        Validate.isTrue(plaintext != null && plaintext.length > 0, "plaintext is empty");
        Validate.isTrue(plaintext.length <= MAX_PLAINTEXT_LENGTH, "plaintext is longer than " +
                MAX_PLAINTEXT_LENGTH + " bytes");
    }

    static void validateCiphertext(byte[] ciphertext) {
        Validate.isTrue(ciphertext != null && ciphertext.length > 0, "ciphertext is empty");
        Validate.isTrue(ciphertext.length <= MAX_CIPHERTEXT_LENGTH, "ciphertext is longer than " +
                MAX_CIPHERTEXT_LENGTH + " bytes");
    }

    static void validateContentLength(int contentLength) {
        Validate.isTrue(contentLength > 0, "body is empty");
        Validate.isTrue(contentLength < MAX_CONTENT_LENGTH, "body is longer than " + MAX_CONTENT_LENGTH + " bytes");
    }
}
