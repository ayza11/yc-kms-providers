# Yandex.Cloud KMS Providers

## Входные данные
* Имеется существующее облако с проставленным alpha-флагом `Yandex Key Management Service`
* Текущий пользователь обладает ролью `owner` или `editor` в облаке из предыдущего пункта
* Пользователь скачал yc cli и выполнил команду `yc init`
* Также понадобятся:
  * Java Runtime Environment версии 8 и выше
  * [AWS Encryption SDK](https://docs.aws.amazon.com/encryption-sdk/latest/developer-guide/java.html)
  * [YC KMS provider for AWS Encryption SDK]()  

### Создание ключа симметричного шифрования
* Зайдите в UI консоль Яндекс.Облака https://console.cloud.yandex.ru
* Зайдите в существующее облако с проставленным alpha-флагом `Yandex Key Management Service`
* Зайдите в существующий каталог или создайте новый каталог
* Перейдите на главную страницу сервиса Key Management Service
* Создайте новый ключ шифрования с параметрами
  * Имя: myFirstKey (или любое другое)
  * Алгоритм: AES_256
  * Период ротации: нет
  * Остальные параметры оставьте по умолчанию, либо измените по своему усмотрению
* Убедитесь, что у вновь созданного ключа автоматически появилась первая версия ключа
* Скопируйте id созданного ключа нажав на иконку "Copy" (далее в описании возьмём id равным `fve71roc3v3v1o7fee00`)

### Подготовка переменных окружения
Откройте терминал, запустите yc cli для получения IAM токена:
```bash
export YCKMS_AUTH=`yc iam create-token`
```
Установите в переменные окружения endpoint's YC KMS:
```bash
export YCKMS_HOST="kms.yandex"
export YCKMS_PORT=443
```

### Шифрование строк при помощи AWS Encryption SDK
Ниже приводится исходный код на языке Java, в котором осуществляется шифрование / дешифрование пользовательских данных при 
использовании открытой библиотеки AWS Encryption SDK и ключа из YC KMS.
```java
class AwsEncryptionTest {
    public static void main(final String[] args) {
        String keyId = args[0]; // id of the key created in KMS UI
        String data = args[1]; // data to encrypt

        // initialize AWS Encryption framework
        AwsCrypto crypto = new AwsCrypto();

        // set up YC KMS provider
        YcKmsMasterKeyProvider prov = YcKmsMasterKeyProvider.builder()
                .setHost(System.getenv(YCKMS_HOST))
                .setPort(Integer.parseInt(System.getenv(YCKMS_PORT)))
                .setAuthHeader(System.getenv(YCKMS_AUTH))
                .setKeyId(keyId)
                .build();

        // set Additional Authenticated Data context
        Map<String, String> context = Collections.singletonMap("Foo", "Bar");

        // let's encrypt!
        String ciphertext = crypto.encryptString(prov, data, context).getResult();

        // and now decrypt
        CryptoResult<String, YcKmsMasterKey> decryptResult = crypto.decryptString(prov, ciphertext);

        if (!data.equals(decryptResult.getResult())) {
            System.out.println("Decrypted text differs from the original plaintext!!");
        }
    }
}
``` 