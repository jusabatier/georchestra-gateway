# geOrchestra Gateway - Logging Module

This module provides enhanced logging capabilities for WebFlux applications with a focus on geOrchestra Gateway. It includes:

- **Access Logging** with configurable log levels based on URL patterns
- **JSON-formatted Structured Logging** using Logstash encoder
- **MDC (Mapped Diagnostic Context) Propagation** for WebFlux reactive applications
- **Spring Cloud Gateway Integration** with dedicated filters for the Gateway filter chain

## Key Features

### MDC Propagation in WebFlux

Standard SLF4J MDC doesn't work well in WebFlux reactive applications because a single request can span multiple threads. This module solves this by:

- Storing MDC data in the Reactor Context to propagate it across thread boundaries
- Providing utilities to easily access MDC from reactive code
- Supporting automatic propagation through the reactive chain

#### Understanding Thread Management in Reactive Applications

In traditional servlet-based applications, a single thread handles a request from start to finish, so the thread-local SLF4J MDC works well. However, in reactive applications:

1. **Thread Switching**: A request might be processed across multiple threads due to the asynchronous nature of reactive programming
2. **Lost Context**: Traditional thread-local MDC values would be lost when execution switches threads
3. **Broken Logging Context**: This would make it impossible to correlate log entries for a specific request

Our solution uses Reactor's Context as a thread-safe storage mechanism that flows through the reactive chain:

```
┌─────────────────┐                 ┌─────────────────┐                 ┌─────────────────┐
│  Thread 1       │                 │  Thread 2       │                 │  Thread 3       │
│                 │                 │                 │                 │                 │
│  ┌───────────┐  │                 │  ┌───────────┐  │                 │  ┌───────────┐  │
│  │ThreadLocal│  │ Thread Switch   │  │ThreadLocal│  │ Thread Switch   │  │ThreadLocal│  │
│  │   MDC     │──┼─-────X─────────>│  │   MDC     │──┼───-──X─────────>│  │   MDC     │  │
│  └───────────┘  │                 │  └───────────┘  │                 │  └───────────┘  │
└─────────────────┘                 └─────────────────┘                 └─────────────────┘
        │                                   │                                   │
        │                                   │                                   │
        │         ┌───────────────────────────────────────────────┐             │
        │         │                                               │             │
        └────────>│              Reactor Context                  │<────────────┘
                  │    (MDC data flows with the reactive chain)   │
                  └───────────────────────────────────────────────┘
```

**How it works:**

1. `MDCWebFilter` captures MDC data and stores it in Reactor Context via `contextWrite()`
2. The context is attached to the reactive chain and flows with operations across thread boundaries
3. `ReactorContextHolder` provides utilities to move data between thread-local MDC and Reactor Context
4. When logging occurs, we temporarily restore the MDC data from Reactor Context to the current thread
5. After logging, we clean up the thread-local MDC to prevent leaks

### Access Logging

The module provides a configurable access log filter that captures information about HTTP requests:

- HTTP method, URI, status code, and processing duration
- Configurable log levels (info, debug, trace) based on URI patterns
- Integration with MDC for enriched context in logs

### Structured JSON Logging

Includes a logback configuration for JSON-formatted logs using the Logstash encoder:

- Consistent structured logging format for easy parsing and analysis
- Automatic inclusion of all MDC properties in logs
- Configurable through standard logback properties

### Spring Cloud Gateway Integration

Special support for Spring Cloud Gateway:

- Global filter adapters to integrate with the Gateway filter chain
- Proper handling of access logs in the Gateway environment
- MDC propagation throughout Gateway's reactive processing pipeline

## Usage

### Maven Dependency

```xml
<dependency>
    <groupId>org.georchestra</groupId>
    <artifactId>georchestra-gateway-logging</artifactId>
    <version>${revision}</version>
</dependency>
```

### Auto-configuration

The module uses Spring Boot auto-configuration to automatically set up all necessary components:

- `AccessLogWebFluxAutoConfiguration` - Sets up access logging for WebFlux applications
- `LoggingMDCWebFluxAutoConfiguration` - Sets up MDC propagation for WebFlux
- `GatewayMdcAutoConfiguration` - Adds Gateway-specific adapters for MDC and access logging

### Configuration Properties

#### MDC Configuration

Control which information is included in the MDC:

```yaml
logging:
  mdc:
    enabled: true  # Enable/disable MDC support (default: true)
    include:
      # HTTP request MDC properties
      http:
        id: true             # Include request ID
        method: true         # Include HTTP method
        url: true            # Include request URL
        remote-addr: true    # Include client IP address
        query-string: false  # Include query string
        parameters: false    # Include request parameters
        headers: true        # Include selected HTTP headers
        headers-pattern: "(?i)x-.*|correlation-.*"  # Pattern for included headers
        cookies: false       # Include cookies
      
      # User authentication MDC properties
      user:
        id: true             # Include user ID
        roles: false         # Include user roles
        org: false           # Include user organization
        auth-method: false   # Include authentication method
      
      # Application environment MDC properties
      app:
        name: true           # Include application name
        version: true        # Include application version
        profile: true        # Include active profiles
        instance-id: false   # Include instance ID
```

#### Access Log Configuration

Configure which URLs are logged and at what level:

```yaml
logging:
  accesslog:
    info:    # URLs to log at INFO level
      - ".*\\/api\\/.*"
      - ".*\\/ws\\/.*"
    debug:   # URLs to log at DEBUG level
      - ".*\\/admin\\/.*"
    trace:   # URLs to log at TRACE level
      - ".*\\/debug\\/.*"
```

#### JSON Logging Configuration

Enable JSON logging format:

```yaml
spring:
  profiles:
    active: json-logs  # Activate the json-logs profile to use JSON format
```

## Extension Points

### Custom MDC Data

To add custom data to the MDC, extend the `MDCWebFilter` or use the `ReactorContextHolder` utility:

```java
// In a WebFilter or controller
ReactorContextHolder.setThreadLocalMdc(customMdcMap);

// Get MDC data in reactive code (non-blocking)
Mono<Map<String, String>> mdcMono = ReactorContextHolder.getMdcMapFromContext(mono);

// Extract MDC directly from the Reactor Context (non-blocking)
return Mono.deferContextual(ctx -> {
    Map<String, String> mdcMap = ReactorContextHolder.extractMdcMapFromContext(ctx);
    // Use the MDC map here
    return Mono.just(result);
});
```

### Custom Log Patterns

The access log patterns are fully customizable through regular expressions. Define your own patterns in your application properties.

## Implementation Details

### Key Classes

- `MDCWebFilter` - WebFlux filter that captures MDC data and propagates it through the Reactor Context
- `ReactorContextHolder` - Utility for accessing MDC data from the Reactor Context
  - `getMdcMap()` - Gets MDC from the thread-local context
  - `setThreadLocalMdc(Map<String, String>)` - Sets MDC values in the current thread
  - `setMdcFromContext(ContextView)` - Sets MDC from a Reactor Context
  - `getMdcMapFromContext(Mono<?>)` - Gets MDC map from a Mono's context
  - `extractMdcMapFromContext(ContextView)` - Extracts MDC map from a context view without blocking
- `AccessLogWebfluxFilter` - Filter for logging HTTP request access
- `AccessLogFilterConfig` - Configuration for the access log filter
- `HttpRequestMdcConfigProperties` - Configuration for HTTP request MDC properties
- `AuthenticationMdcConfigProperties` - Configuration for user authentication MDC properties
- `SpringEnvironmentMdcConfigProperties` - Configuration for application environment MDC properties

### Architecture

The module follows a layered architecture:

1. **Core** - Contains the fundamental functionality for MDC propagation and access logging
2. **Configuration** - Properties classes for controlling behavior
3. **Auto-configuration** - Spring Boot auto-configuration for easy integration
4. **Gateway Integration** - Adapters for Spring Cloud Gateway

### Thread Safety and MDC Propagation

All components are designed to be thread-safe, with careful handling of MDC context to avoid leaking context between requests:

- Initial MDC state is saved and restored after request processing
- MDC is cleaned up after logging operations
- Context is properly propagated through the reactive chain using Reactor's context system

#### Detailed Thread Management

The challenge of maintaining logging context across thread boundaries is handled through several key mechanisms:

1. **Capturing Initial Context**:
   ```java
   // In MDCWebFilter.java
   return setMdcAttributes(exchange).flatMap(requestMdc -> {
       // ...
       return chain.filter(exchange)
           .contextWrite(context -> context.put(MDC_CONTEXT_KEY, requestMdc))
           // ...
   });
   ```

2. **Thread Boundary Crossing**:
   - When an asynchronous operation switches from one thread to another, the Reactor Context (with our MDC data) is implicitly carried to the new thread
   - This happens automatically due to Reactor's design, where the Context is immutable and flows with the reactive chain

3. **Accessing MDC in a Thread-Safe Way**:
   ```java
   // Extract MDC from context when needed for logging
   Mono.deferContextual(ctx -> {
       Map<String, String> mdcMap = ReactorContextHolder.extractMdcMapFromContext(ctx);
       try {
           // Temporarily set MDC values for current thread
           ReactorContextHolder.setThreadLocalMdc(mdcMap);
           log.info("Log message with MDC context");
       } finally {
           // Clear MDC to prevent leaks
           MDC.clear();
       }
       return Mono.empty();
   });
   ```

4. **Subscription Hooks**:
   ```java
   // In MDCWebFilter.java
   return chain.filter(exchange)
       .doOnSubscribe(s -> {
           // Set the MDC values for this thread when the chain is subscribed
           MDC.setContextMap(requestMdc);
       })
       .doFinally(signalType -> {
           // Clean up
           MDC.clear();
       });
   ```

This combination of approaches ensures that regardless of thread switching, logging statements always have access to the correct MDC context.

## Development

### Building the Module

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Adding New Features

1. Add new functionality in the appropriate package
2. Write tests using WebFlux test utilities
3. Update configuration properties if needed
4. Add auto-configuration support if applicable
5. Update documentation

## Sample JSON Log Format

When JSON logging is enabled, logs will be structured with MDC properties included. Here are some example log entries for different scenarios:

### HTTP Request Access Log

```json
{
  "@timestamp": "2024-03-31T12:34:56.789Z",
  "message": "GET /api/maps/123 200",
  "logger_name": "org.georchestra.gateway.accesslog",
  "thread_name": "reactor-http-nio-3",
  "level": "INFO",
  "level_value": 20000,
  "http.request.id": "01HRG3ZNVN4CKF74DNKB1S2XRM",
  "http.request.method": "GET",
  "http.request.url": "/api/maps/123",
  "http.request.remote-addr": "192.168.1.25",
  "application.name": "gateway-service",
  "application.profile": "prod",
  "application.version": "1.2.0",
  "enduser.id": "admin",
  "enduser.roles": "ROLE_ADMINISTRATOR,ROLE_USER"
}
```

### Authentication Event Log

```json
{
  "@timestamp": "2024-03-31T12:33:45.678Z",
  "message": "User authenticated successfully",
  "logger_name": "org.georchestra.gateway.security",
  "thread_name": "reactor-http-nio-2",
  "level": "INFO",
  "level_value": 20000,
  "http.request.id": "01HRG3ZMPTS7FV9JD5YE5TH9MQ",
  "http.request.method": "POST",
  "http.request.url": "/login",
  "http.request.remote-addr": "192.168.1.25",
  "application.name": "gateway-service",
  "application.profile": "prod",
  "application.version": "1.2.0",
  "enduser.id": "admin",
  "enduser.auth-method": "FormLoginAuthenticationToken"
}
```

### Error Log

```json
{
  "@timestamp": "2024-03-31T12:35:12.345Z",
  "message": "Failed to process request: Resource not found",
  "logger_name": "org.georchestra.gateway.filters",
  "thread_name": "reactor-http-nio-4",
  "level": "ERROR",
  "level_value": 40000,
  "http.request.id": "01HRG3ZQ7YHSM6TVPKZ0SD8XWB",
  "http.request.method": "GET",
  "http.request.url": "/api/datasets/unknown",
  "http.request.remote-addr": "192.168.1.25",
  "application.name": "gateway-service",
  "application.profile": "prod",
  "application.version": "1.2.0",
  "enduser.id": "analyst",
  "enduser.roles": "ROLE_USER",
  "error.message": "Resource not found: Dataset with ID 'unknown' does not exist",
  "error.stack_trace": "org.springframework.web.server.ResponseStatusException: 404 NOT_FOUND \"Resource not found\"\n\tat org.georchestra.gateway.handlers.ApiHandler.getDataset(ApiHandler.java:87)\n\t..."
}
```

## Troubleshooting

### Common Issues

- **MDC Values Not Available**: Ensure the MDCWebFilter is registered and has highest precedence
- **Access Logs Not Generated**: Check log level configuration and URL patterns
- **JSON Format Not Applied**: Verify the `json-logs` profile is active

### Debugging Tips

- Enable DEBUG logging for the module:
  ```yaml
  logging:
    level:
      org.georchestra.gateway.logging: DEBUG
  ```

- Use ReactorContextHolder methods to verify MDC content:
  ```java
  // Non-blocking way to check MDC context in a reactive chain
  return Mono.deferContextual(ctx -> {
      Map<String, String> mdcMap = ReactorContextHolder.extractMdcMapFromContext(ctx);
      log.debug("MDC in reactive chain: {}", mdcMap);
      return Mono.empty();
  });
  ```

## License

This module is part of geOrchestra and is licensed under the GPL 3.0 license.
