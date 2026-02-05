package io.github.oldmerman.web.service;

import io.github.oldmerman.model.po.Counter;

import java.util.List;

public interface CounterService {

    Integer getSystemTime();

    List<Counter> getIncr(Long type);

}
