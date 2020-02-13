# signer

## Overview

SpringBoot library for automatic and secure http request's signing using Spring RestTemplate.

This library provide you a way for signing your http requests between SpringBoot microservices ensuring the identity of the client and the integrity of the message.
The message is signed with a keyed-hash message authentication code (HMAC) generated with a pre-shared secret key, this allow you to authenticate your rest API in a smart way.


## Description

The library offers two annotations for client and server respectively.

***@Sign***

This annotation must be placed on the client method inside wich the http call is made with Spring RestTemplate client (not necessary a controller's method).
The library will attach the necessary header to the request that contain the signature, everything happens transparently for the user.

***@Signed***

This annotation must be placed on the called controller's method and notify the library that this method is signed and the http request must be authenticated.
If the signature verification process completes succesfully, the http request will be handled by the server otherwise the client will receive a 401 UNAUTHORIZED response message.
Every rest API annotated with *@Signed* annotation will be secured and will require a signed request. 

*In case of body's presence into the request, this content will be used for the signature's generation. Otherwise the signature will be calculated from a randomic generated seed that will be attached to the request as header.*

The library uses the Java Mac class provided by the JDK to make the symmetric signature.
See the official [Oracle](https://docs.oracle.com/javase/7/docs/api/javax/crypto/Mac.html) documentation for more details.


## Building and installation

Clone the project, build and install it with the following Maven command:

*mvn clean install -DskipTests*



## Configurations

### Library import

In order to use the library you have to import it on your pom.xml as shown below:

```
<dependency>
   <groupId>com.ffsec</groupId>
   <artifactId>signer</artifactId>
   <version>1.0</version>
</dependency>
```

### Library properties

Set the following properties into your *aplication.properties* or *application.yml*.

Both the client and server must have the same secret key configured inside them since the signature algorithm use a symmetric key.
On client side the secret key is used to generate the signature, on server side the same key is used for the signature's verification process.

The property to set is ***ffsec.signer.secret*** and must contains a randomic generated string with any length (recommended 128/256/512 bit) as in the following example.

```
ffsec.signer.secret=NV8UJUL81Y9F
```

***This property is mandatory, if you don't define it an Exception will be throwed at SpringBoot's startup.***

It's also possible to define the hashing algorithm used for the signature generation, the default is *HmacSHA256* but also these algorithms are supported:

- HmacMD5
- HmacSHA1
- HmacSHA256
- HmacSHA384
- HmacSHA512

The property to set is ***ffsec.signer.algorithm*** and the possible values are listed above, see the following example.

```
ffsec.signer.algorithm=HmacSHA384
```

***It's important to define the same algorithm on both client and server to avoid problems.***

If you define a string that is not equals to one of the possible values listed above, the default value will be used.


## Coding Example


### Client side implementation

```
@RestController
public class ClientController {

    @Autowired
    RestTemplate restTemplate;

    @Sign
    @GetMapping("client")
    public ResponseEntity<String> client() {

        return restTemplate.postForEntity("http://localhost:8080/server", "the brown fox jumps over the lazy dog" , String.class);

    }
}
```

The library provides you an already instantiated RestTemplate bean that you can inject into your *@RestController* class or wherever it is needed.

***All the http calls must be executed with this instance otherwise the library does not work.***

The name of the instance to inject is ***restTemplate*** as shown in the example above.


### Server side implementation

```
@RestController
public class ServerController {

    @Signed
    @PostMapping(value = "server", consumes = "text/plain")
    public ResponseEntity<String> server(@RequestBody String body) {
        return ResponseEntity.ok("OK");
    }
    
}
```

### Configuration class 

You have to import the configuration class ***SignerConfiguration.class*** on both client and server.

```
@SpringBootApplication
@Import(SignerConfiguration.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```


## Multithreading

The library is yet to bet tested for concurrency issues.

Some considerations:

Since the RestTemplate object does not change any of his state information to process HTTP it can be considered thread safe so the same instance can be shared among multiple processes.

If multiple generation's/verification's processes run at the same time on the same SpringBoot instance, different instances of Mac class are used, Mac objects are stateful so they can't be used by multiple threads.


## Logging

The library uses ***SLF4J*** as logging facade system.

See the official [SLF4J](http://www.slf4j.org/docs.html) documentation for more details.

If you want to enable the library's logs you have to configure the logging level TRACE for the package ***com.ffsec***

The exceptions are catched and logged at ERROR level.

This is an example with Log4j:

```
log4j.logger.com.ffsec=TRACE
```

## License

This software is distributed under the [MIT License](https://opensource.org/licenses/MIT).
