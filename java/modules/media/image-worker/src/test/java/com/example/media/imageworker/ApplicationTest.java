package com.example.media.imageworker;

import com.example.media.contract.port.ObjectStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * The context smoke test. The driven adapters arrive with the wiring step; until then the port
 * stands in as a mock so the application context still loads.
 */
@SpringBootTest
class ApplicationTest {

    @MockitoBean
    ObjectStore objectStore;

    @Test
    void contextLoads() {
    }
}
