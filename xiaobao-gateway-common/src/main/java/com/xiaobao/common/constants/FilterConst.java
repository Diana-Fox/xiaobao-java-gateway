package com.xiaobao.common.constants;

/**
 * 负载均衡的常量
 */
public interface FilterConst {
    String LOAD_BALANCE_FILTER_ID = "load_balance_filter";
    String LOAD_BALANCE_FILTER_NAME = "load_balance_filter";
    int LOAD_BALANCE_FILTER_ORDER = 100;

    //设置默认的策略
    String LOAD_BALANCE_STRATEGY_RANDOM = "Random";
    String LOAD_BALANCE_STRATEGY_RANDOM_ROBIN = "RoundRobin";
    String LOAD_BALANCE_KEY = "load_balancer";
}
