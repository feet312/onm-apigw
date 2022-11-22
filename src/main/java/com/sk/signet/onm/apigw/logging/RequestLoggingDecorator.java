package com.sk.signet.onm.apigw.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class RequestLoggingDecorator extends ServerHttpRequestDecorator {
	
	
	public RequestLoggingDecorator(ServerHttpRequest delegate) {
		super(delegate);
	}
	
	@Override
	public Flux<DataBuffer> getBody() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		return super.getBody()
				.doOnNext(dataBuffer -> {
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
				});
	}

}