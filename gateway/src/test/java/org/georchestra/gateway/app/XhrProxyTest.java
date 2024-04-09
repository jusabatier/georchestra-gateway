package org.georchestra.gateway.app;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XhrProxyTest {

    public @Test void testLinkLocalIpAddresses() {
        XhrProxy toTest = new XhrProxy();

        assertThrows(AccessDeniedException.class, () -> toTest.xhrProxy("http://192.168.0.22:8080/geoserver"),
                "Expected a AccessDeniedException");

        assertThrows(AccessDeniedException.class, () -> toTest.xhrProxy("http://localhost:8280/geoserver"),
                "Expected a AccessDeniedException");
    }

    public @Test void testExternalWebsite() throws MalformedURLException, UnknownHostException {
        XhrProxy toTest = new XhrProxy();
        WebClient wc = Mockito.mock(WebClient.class);
        toTest.setWebClient(wc);
        WebClient.RequestHeadersUriSpec req = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        Mockito.when(req.uri(Mockito.anyString())).thenReturn(req);
        WebClient.ResponseSpec resp = Mockito.mock(WebClient.ResponseSpec.class);
        Mockito.when(req.retrieve()).thenReturn(resp);
        Mockito.when(resp.bodyToMono(String.class)).thenReturn(Mono.just("It works"));
        Mockito.when(wc.get()).thenReturn(req);

        Mono<String> ret = toTest.xhrProxy("https://www.google.com/about");

        assertEquals("It works", ret.block());
    }

}
