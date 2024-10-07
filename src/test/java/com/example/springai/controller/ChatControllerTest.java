package com.example.springai.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;

class ChatControllerTest {

    @Test
    void fluxDoNextTest() {
        Flux<String> flux = Flux.just("A", "B", "C");

        StringBuilder summaryText = new StringBuilder();

        flux.doOnNext(item -> summaryText.append("Received: ").append(item).append("\n"))
            .doFinally(text -> System.out.println(summaryText.toString()))
            .subscribe();
    }

}