package com.xiaobao.gateway.config.center.api;

import com.xiaobao.common.config.Rule;

import java.util.List;

public interface RuleChangeListener {
    void onRulesChange(List<Rule> rule);
}
