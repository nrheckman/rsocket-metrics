package xyz.heckman.demo.rsocketmetrics.router;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.io.Serializable;

@Slf4j
@Controller
public class HelloController {

	@MessageMapping("greetings.{lang}")
	public Mono<HelloDto> encrypt(@DestinationVariable String lang, @Payload Mono<NameDto> nameMono) {
		return nameMono
				.doOnNext(nameDto -> log.info("Got new message: {}", nameDto))
				.flatMap(nameDto -> {
					switch (lang.toLowerCase()) {
						case "en_us":
							return Mono.just(HelloDto.builder()
									.greeting(String.format("Hello, %s", nameDto.getName()))
									.build());
						default:
							return Mono.error(new IllegalArgumentException("Unsupported language requested"));
					}
				});
	}

	@Getter
	@ToString
	public static class NameDto implements Serializable {
		private static final long serialVersionUID = -4237908508127574939L;
		private String name;
	}

	@Getter
	@Builder
	public static class HelloDto implements Serializable {
		private static final long serialVersionUID = 3301732201713854103L;
		private String greeting;
	}
}
