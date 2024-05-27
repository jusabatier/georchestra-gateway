/*
 * Copyright (C) 2024 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.filter.global;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * {@link GatewayFilterFactory} providing a {@link GatewayFilter} that throws a
 * {@link ResponseStatusException} with the proxied response status code if the
 * target responded with a {@code 400...} or {@code 500...} status code.
 * 
 */
public class ApplicationErrorGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

	public ApplicationErrorGatewayFilterFactory() {
		super(Object.class);
	}

	@Override
	public GatewayFilter apply(final Object config) {
		return new ServiceErrorGatewayFilter();
	}

	private static class ServiceErrorGatewayFilter implements GatewayFilter, Ordered {

		public @Override Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				HttpStatus statusCode = exchange.getResponse().getStatusCode();
				if (statusCode.is4xxClientError() || statusCode.is5xxServerError()) {
					throw new ResponseStatusException(statusCode);
				}
			}));
		}

		@Override
		public int getOrder() {
			return ResolveTargetGlobalFilter.ORDER + 1;
		}
	}
}
