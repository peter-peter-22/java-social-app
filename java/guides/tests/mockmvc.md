# MockMVC guide

## Install
Required dependencies:
- org.springframework.boot:spring-boot-starter-webmvc
- org.springframework.boot:spring-boot-starter-webmvc-test
These vary between spring boot versions.

## Usage

### Basic get

```java
package com.example.uploads_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EndToEndTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void testConnection() throws Exception {
        mockMvc
                // indicate what MockMvc should do
                .perform(
                        // the get method and path for the request
                        get("/"))
                // print the response
                .andDo(print())
                // the response should have status 200 OK
                .andExpect(status().isOk());
    }
}
```

# Text file upload

The target endpoint:
```java
@PostMapping(path = "/upload")
public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
    return file.isEmpty() ?
      new ResponseEntity<String>(HttpStatus.NOT_FOUND) : new ResponseEntity<String>(HttpStatus.OK);
}
```

The test:
```java
@Test
public void whenFileUploaded_thenVerifyStatus() 
  throws Exception {
    MockMultipartFile file 
      = new MockMultipartFile(
        "file", 
        "hello.txt", 
        MediaType.TEXT_PLAIN_VALUE, 
        "Hello, World!".getBytes()
      );

    MockMvc mockMvc 
      = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(multipart("/upload").file(file))
      .andExpect(status().isOk());
}
```