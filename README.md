# signer

## Overview

SpringBoot library for automatic and secure http request signing.

This library provide you a way for signing your RestTemplate http request between microservices ensuring the identity of the client and the integrity of the message.
The message is signed with a keyed-hash message authentication code (HMAC) generated with a pre-shared secret key, this allow you to securize yor rest API in a smart way.

## Description

The library offers two annotations for client and server respectively.

***@Sign***

This annotation must be placed on the client method inside wich the http call is made with Spring RestTemplate client.
The library will attach the necessary header to the request that contain the signature, everything happens transparently for the user.

***@Signed***

This annotation must be placed on the called rest endpoint and notify the library that this method is signed and the http request must be validated.
If the verification process completes succesfully, the http request will be handled by the server otherwise the client will receive a 401 UNAUTHORIZED response message.
Every rest API annotated with @Signed annotation will be secured and will require a signed client. 

*Obviously the two annotations will trigger the verification process only for the request that contain a body to sign.
Put the annotations on top of a method that accept requests without body will have no effect.*

The library uses the Java Mac class provided by the JDK to make the symmetric signature.
See the official Oracle documentation linked below for more details.

https://docs.oracle.com/javase/7/docs/api/javax/crypto/Mac.html

## Building and installation

Clone the project, build and install it with the following maven command:

*mvn clean install -DskipTests*


## Configurations

### Library import

Import the library into your maven project with the following dependecy on your pom:

```
<dependency>
   <groupId>com.ffsec</groupId>
   <artifactId>signer</artifactId>
   <version>1.0</version>
</dependency>
```

### Library properties

Both the client and server must have the same secret key configured inside them since the signature algorithm use a symmetric key.
On client side the secret key is used to generate the signature, on server side the same key is used for the signature verification process.

The property to set is ***ffsec.signer.secret*** and must contains a randomic generated string with any length (recommended 128/256/512 bit).

```
ffsec.signer.secret=NV8UJUL81Y9F
```

It's also possible for the user to define the hashing algorithm used for the HMAC signature generation, the default is HmacSHA256 but also these algorithms are supported:

- HmacMD5
- HmacSHA1
- HmacSHA256
- HmacSHA384
- HmacSHA512

The property for the hashing algorithm is ***ffsec.signer.algorithm*** and the possible values are listed above.

*It's important to define the same algorithm on both client and server to avoid problems*

```
ffsec.signer.algorithm=HmacSHA384
```

## Coding Example


### Client implementation

```
@RestController
public class ClientController {

    @Autowired
    RestTemplate restTemplate;

    @Sign
    @GetMapping("client")
    public ResponseEntity<String> test() {

        return restTemplate.postForEntity("http://localhost:8080/server", "the brown fox jumps over the lazy dog" , String.class);

    }
}
```

The library provides you an already instantiated RestTemplate bean that you can inject into your RestController or wherever it is needed.

*All the http calls must be executed with this instance otherwise the library does not work.*


### Server implementation

```
@RestController
public class TestController {

    @Signed
    @PostMapping(value = "server", consumes = "text/plain")
    public ResponseEntity<String> demoSigned(@RequestBody String body) {
        return ResponseEntity.ok("OK");
    }
    
}
```

### Configuration class 

You have to import this configuration class on both client and server.

```
@SpringBootApplication
@Import(SignerConfiguration.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```

## Logging

The library uses ***Slf4j*** as logging facade system.

Visit official documentation for more details:
http://www.slf4j.org/docs.html

If you want to enable the internal library logs you have to enable the logging at DEBUG level for the package ***com.ffsec***.
This is an example with Log4j:

```
log4j.logger.com.ffsec=DEBUG
```
