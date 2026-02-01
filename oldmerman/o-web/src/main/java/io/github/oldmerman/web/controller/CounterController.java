package io.github.oldmerman.web.controller;

import io.github.oldmerman.web.service.CounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("counter")
@Slf4j
@RequiredArgsConstructor
public class CounterController {

    private final CounterService counterService;


}
