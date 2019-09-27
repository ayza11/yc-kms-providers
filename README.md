# Yandex.Cloud KMS Providers
Данный пример на языке Java иллюстрирует возможность шифрования на стороне клиента при использовании ключа, хранимого в
YC KMS. Для осуществления операций шифрования используется открытая библиотека AWS Encryption SDK.  

## Входные данные
* Имеется существующее облако с проставленным alpha-флагом `Yandex Key Management Service`
* Текущий пользователь обладает ролью `owner` или `editor` в облаке из предыдущего пункта
* Пользователь скачал yc cli и выполнил команду `yc init`
* Также понадобятся:
  * Java Runtime Environment версии 8 и выше
  * [AWS Encryption SDK for Java](https://docs.aws.amazon.com/encryption-sdk/latest/developer-guide/java.html)
  * [YC KMS provider for AWS Encryption SDK](https://github.com/ayza11/yc-kms-providers)  

### Создание ключа симметричного шифрования
* Зайдите в UI консоль Яндекс.Облака https://console.cloud.yandex.ru
* Зайдите в существующее облако с проставленным alpha-флагом `Yandex Key Management Service`
* Зайдите в существующий каталог или создайте новый каталог
* Перейдите на главную страницу сервиса Key Management Service
* Создайте новый ключ шифрования с параметрами
  * Имя: **my first key** (или любое другое)
  * Алгоритм: **AES-256**
  * Период ротации: **нет ротации**
* Убедитесь, что у вновь созданного ключа автоматически появилась первая версия ключа
* Скопируйте id созданного ключа нажав на иконку "Copy" (далее в описании возьмём id равным `fve71roc3v3v1o7fee00`)

### Подготовка окружения
Откройте терминал, запустите yc cli для получения IAM токена:
```bash
export YCKMS_AUTH=`yc iam create-token`
```
Установите в переменные окружения endpoint's YC KMS:
```bash
export YCKMS_HOST="kms.yandex"
export YCKMS_PORT=443
```

### Шифрование при помощи AWS Encryption SDK
Ниже приводится исходный код на языке Java, который осуществляет шифрование / дешифрование пользовательских данных при 
использовании открытой библиотеки AWS Encryption SDK и ключа из YC KMS. Следует отметить, что шифрование здесь осуществляется
на стороне клиента в соответствии со схемой envelope encryption. KMS генерирует ключ для локального шифрования данных, 
шифрует его на своём внутреннем KMS-ключе и передаёт шифрованную и открытую версии ключа шифрования данных на клиент. 
Клиентская библиотека шифрует 
данные локально на открытой версии ключа, после чего уничтожает открытую версию, а рядом с зашифрованными
данными кладёт зашифрованную (на ключе из KMS) версию. Подробнее про схему envelope encryption можно почитать 
[здесь](https://docs.aws.amazon.com/encryption-sdk/latest/developer-guide/how-it-works.html#envelope-encryption). 
Про формат итогового сообщения - см. [документацию AWS
Encryption SDK](https://docs.aws.amazon.com/encryption-sdk/latest/developer-guide/message-format.html)
```java
class AwsEncryptionTest {
    public static void main(final String[] args) {
        String keyId = args[0]; // id of the KMS key created in KMS UI
        String data = args[1]; // data to encrypt

        // initialize AWS Encryption framework, algorithm defaults to AWS GCM 256bit
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
Следует отметить, что при таком способе взаимодействия с KMS можно шифровать неограниченный объём данных, так как
непосредственно KMS здесь используется исключительно для шифрования ключа шифрования данных (256bit). 
Скрипты для сборки и запуска данного примера можно найти в публичном репозитории 
[YC KMS providers]((https://github.com/ayza11/yc-kms-providers)) в директории `src/examples`. 