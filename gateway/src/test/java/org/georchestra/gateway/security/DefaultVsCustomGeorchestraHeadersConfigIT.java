package org.georchestra.gateway.security;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.georchestra.gateway.app.GeorchestraGatewayApplication;
import org.georchestra.testcontainers.ldap.GeorchestraLdapContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = GeorchestraGatewayApplication.class)
@WireMockTest
@AutoConfigureWebTestClient
@ActiveProfiles("customgeorheaders")
public class DefaultVsCustomGeorchestraHeadersConfigIT {
    public static GeorchestraLdapContainer ldap = new GeorchestraLdapContainer();

    public static WireMockRuntimeInfo wmri;

    private @Autowired WebTestClient testClient;

    @BeforeAll
    public static void setUp(WireMockRuntimeInfo wmri) {
        DefaultVsCustomGeorchestraHeadersConfigIT.wmri = wmri;
        ldap.start();
        System.setProperty("wmHost", "localhost");
        System.setProperty("wmPort", Integer.toString(wmri.getHttpPort()));
    }

    public static @AfterAll void shutDownContainers() {
        ldap.stop();
    }

    @Test
    /**
     * This test checks that the base64-json versions of the http headers are sent
     * to proxified services, if specified in the configuration of the service. e.g.
     * if they are deactivated by default:
     *
     * ```yaml georchestra: gateway: default-headers: proxy: true username: true
     * roles: true org: true orgname: true ``` but activated at the service level,
     * e.g.:
     *
     * ```yaml georchestra: gateway: services: myservice: [...] headers: json-user:
     * true json-organization: true ``` Then the sec-user and sec-organization
     * headers should be received by the proxified webapp.
     *
     */
    public void testCustomizedHeadersForTarget() {
        stubFor(get("/echo/").willReturn(ok()));

        testClient.get().uri("/echo/")//
                .header("Host", "localhost")//
                .header("Authorization", "Basic dGVzdGFkbWluOnRlc3RhZG1pbg==") // testadmin:testadmin
                .exchange();

        List<ServeEvent> events = getAllServeEvents();

        assertEquals(1, events.size());
        assertThat(events.getFirst().getRequest().getAllHeaderKeys(), contains("sec-user", "sec-organization"));
    }
}
