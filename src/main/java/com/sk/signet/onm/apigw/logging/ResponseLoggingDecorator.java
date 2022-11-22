package com.sk.signet.onm.apigw.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class ResponseLoggingDecorator extends ServerHttpResponseDecorator {
	
	public ResponseLoggingDecorator(ServerHttpResponse delegate, long startTime) {
		super(delegate);
	}

	@Override
	public Mono<Void> writeWith(Publisher<? extends DataBuffer> bodyBuffer) {
		Flux<DataBuffer> buffer = Flux.from(bodyBuffer);
		return super.writeWith(buffer.doOnNext(dataBuffer -> {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				Channels.newChannel(baos).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
			} catch (IOException e) {
				log.error(e.getMessage());
			} finally {
				try {
					baos.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}));
	}

}
