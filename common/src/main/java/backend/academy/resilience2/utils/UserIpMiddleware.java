package backend.academy.resilience2.utils;

import backend.academy.configuration.GlobalConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Log4j2
public class UserIpMiddleware implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var address = exchange.getRequest().getRemoteAddress();
        log.info("UserIpMiddleware filter for ip {}",
            address == null ? "null" : address.getAddress().getHostAddress());
        if (address != null) {
            return chain.filter(exchange)
                .contextWrite(context ->
                    //Forwarded-from
                    context.put(GlobalConstants.USER_IP_CONTEXT, address.getAddress().getHostAddress()));
        }
        return chain.filter(exchange);
    }
}
