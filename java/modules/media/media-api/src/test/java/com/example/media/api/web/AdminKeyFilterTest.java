package com.example.media.api.web;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * The admin boundary: the static key must match exactly, the comparison runs in constant time, and a
 * blank configured key is a startup failure instead of an open door.
 */
class AdminKeyFilterTest {

    private final AdminKeyFilter filter = new AdminKeyFilter("test-key");
    private final FilterChain chain = mock();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void passesARequestWithTheAdminKey() throws Exception {
        var request = requestWithKey("test-key");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void rejectsARequestWithoutTheHeader() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/admin/transformation");

        filter.doFilter(request, response, chain);

        verifyNoInteractions(chain);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void rejectsAWrongKey() throws Exception {
        var request = requestWithKey("wrong");

        filter.doFilter(request, response, chain);

        verifyNoInteractions(chain);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void rejectsAKeyThatOnlyDiffersInLength() throws Exception {
        var request = requestWithKey("test-key-longer");

        filter.doFilter(request, response, chain);

        verifyNoInteractions(chain);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void letsANonAdminRequestThroughWithoutTheKey() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/get/image/cover.jpg");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void aBlankConfiguredKeyFailsStartup() {
        assertThatThrownBy(() -> new AdminKeyFilter(" "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("media.admin-key");
    }

    private static MockHttpServletRequest requestWithKey(String key) {
        var request = new MockHttpServletRequest("POST", "/api/admin/transformation");
        request.addHeader(AdminKeyFilter.HEADER, key);
        return request;
    }
}
