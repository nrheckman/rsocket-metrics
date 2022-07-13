package xyz.heckman.demo.rsocketmetrics.router;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HelloController implements InitializingBean {

	/**
	 * Non-ideal solution to get some kind of metrics out of rsocket interactions
	 * Ideally, these would be added by a method interceptor maybe? Or a customization of the method handler?
	 */
	private final MeterRegistry meterRegistry;
	private Timer successTimer;
	private Timer failureTimer;

	@Override
	public void afterPropertiesSet() {
		successTimer = meterRegistry.timer("rsocket.interactions", "type", "request-response", "path", "greetings.{lang}", "result", "success");
		failureTimer = meterRegistry.timer("rsocket.interactions", "type", "request-response", "path", "greetings.{lang}", "result", "failure");
	}

	@MessageMapping("greetings.{lang}")
	public Mono<HelloDto> encrypt(@DestinationVariable String lang, @Payload Mono<NameDto> nameMono) {
		return nameMono.transform(mono -> addTimerMetrics(successTimer, failureTimer, mono))
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

	private <T> Mono<T> addTimerMetrics(Timer successTimer, Timer failureTimer, Mono<T> mono) {
		final AtomicLong start = new AtomicLong();
		return mono
				.doFirst(() -> start.set(System.nanoTime()))
				.doFinally(signalType -> {
					Duration duration = Duration.ofNanos(System.nanoTime() - start.get());
					if (signalType == SignalType.ON_ERROR || signalType == SignalType.CANCEL) {
						failureTimer.record(duration);
					} else {
						successTimer.record(duration);
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
