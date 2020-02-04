# signer
SpringBoot library for automatic and secure http request signing.

This library provide you a way for signing your RestTemplate HTTP request accross microservices.
The library offers two annotations, @Sign and @Signed for client and server respectively.

**@Sign**
This annotation must be placed on the client method inside wich the http call is made with Spring RestTemplate rest client.
The library will add the necessary headers to the request that contain the signature, everything happens transparently for the user.

**@Signed**
This annotation must be placed on the called rest endpoint and notify the library that this method is signed and the http request must be validated.
If the verification process completes succesfully, the http request will be handled by the server otherwise the client will receive a 401 UNAUTHORIZED response message.

Both the client and the server must have the same secret key configured inside them since the signature algorithm use a symmetric key.
On client side the secret key is used to generate the signature, on server side the same key is used for the singnature verification process.

The property to set is *ffsec.signer.secret* and must contains a randomic generated string with any length (recommended 128/256/512 bit).

Is also possible for the user to define the hashing algorithm used for the signature generation, the default is SHA-256 but also MD5 and SHA-1 are supported.

The property for the hashing algorithm is *ffsec.signer.algorithm* and the possible values are MD5, SHA-1 and SHA-256.

The library uses a randomic seed for the signature generation, this seed is combined with the secret key and finally hashed, 
this process guarantee high security.


**Building and installation**

Clone the project, build and install it with the following maven command:

*mvn clean install -DskipTests*


**Configuration**

Import the library into your maven project with the following dependecy on your pom:

```
<dependency>
   <groupId>com.ffsec</groupId>
   <artifactId>signer</artifactId>
   <version>1.0</version>
</dependency>
```

The library provides you an already instantiated RestTemplate bean that you can inject into your RestController or wherever it is needed (see the example below).

*All the http calls must be executed with this instance otherwise the library does not work*

This is an example for the client side usage.

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

This is an example for the server usage:

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

Is also required the import of the library configuration class on your SpringBoot application.
This is an example:

```
@SpringBootApplication
@Import(SignerConfiguration.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```
