package org.georchestra.gateway.filter.headers;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;

public class ProxyGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    public ProxyGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(final Object config) {
        return (exchange, chain) -> {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            ServerHttpRequest request = exchange.getRequest();
            List<String> remoteUrls = request.getQueryParams().get("url");
            if ((remoteUrls != null) && (remoteUrls.size() == 1)) {
                try {
                    URI remoteUrl = URI.create(remoteUrls.get(0));
                    String remoteHost = remoteUrl.getHost();
                    InetAddress address = InetAddress.getByName(remoteHost);
                    if (address.isSiteLocalAddress() || address.isLoopbackAddress()) {
                        throw new AccessDeniedException("provided url is forbidden");
                    }

                    request = exchange.getRequest().mutate().uri(remoteUrl).header("Host", remoteHost).build();

                    Route newRoute = Route.async().id(route.getId()).uri(new URI(remoteUrls.get(0)))
                            .order(route.getOrder()).asyncPredicate(route.getPredicate()).build();

                    exchange.getAttributes().put(AddSecHeadersGatewayFilterFactory.DISABLE_SECURITY_HEADERS, "true");
                    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, newRoute);
                    return chain.filter(exchange.mutate().request(request).build());
                } catch (URISyntaxException e) {
                } catch (UnknownHostException e) {
                }
            }
            return chain.filter(exchange);
        };
    }
}
