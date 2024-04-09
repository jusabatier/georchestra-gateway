package org.georchestra.gateway.app;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.*;

@Controller
public class XhrProxy {

    private @Setter WebClient webClient;

    public XhrProxy() {
        webClient = WebClient.builder().build();
    }

    @ResponseBody
    public @RequestMapping("/proxy/") Mono<String> xhrProxy(@RequestParam String url)
            throws MalformedURLException, UnknownHostException {
        URL remoteUrl = URI.create(url).toURL();
        InetAddress address = InetAddress.getByName(remoteUrl.getHost());
        if (address.isSiteLocalAddress() || address.isLoopbackAddress()) {
            throw new AccessDeniedException("provided url is forbidden");
        }
        return webClient.get().uri(url).retrieve().bodyToMono(String.class);
    }
}
