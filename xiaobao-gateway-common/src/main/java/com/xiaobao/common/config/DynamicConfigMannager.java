package com.xiaobao.common.config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 动态服务缓存配置管理类
 */
public class DynamicConfigMannager {
    //服务定义的集合：uniquedId 代表服务的唯一标识
    private ConcurrentHashMap<String/*uniqunedId*/, ServiceDefinition> serviceDefinitionMap = new ConcurrentHashMap<>();
    //服务实例集合
    private ConcurrentHashMap<String/*uniqunedId*/, Set<ServiceInstance>> serviceInstanceMap = new ConcurrentHashMap<>();
    //规则集合
    private ConcurrentHashMap<String/*ruleId*/, Rule> ruleMap = new ConcurrentHashMap<>();
    //路径以及规则的结合
    private ConcurrentHashMap<String/*path*/, Rule> pathRuleMap = new ConcurrentHashMap<>();
    //通过服务名称获取
    private ConcurrentHashMap<String/*服务名称*/, List<Rule>> serviceRuleMap = new ConcurrentHashMap<>();

    /*********************************单例的处理****************************************/
    private DynamicConfigMannager() {
    }

    public Rule getRuleByPath(String key) {
        return pathRuleMap.get(key);
    }

    public List<Rule> getRuleByServiceId(String serviceId) {
        return serviceRuleMap.get(serviceId);
    }


    private static class SingleHolder {
        private static final DynamicConfigMannager INSTANCE = new DynamicConfigMannager();
    }

    public static DynamicConfigMannager getInstance() {
        return SingleHolder.INSTANCE;
    }

    /********************对服务定义缓存进行操作的方法*********************/
    //添加服务定义
    public void putServiceDefinition(String uniqued, ServiceDefinition definition) {
        serviceDefinitionMap.put(uniqued, definition);
    }

    //删除服务定义
    public void removeServiceDefinition(String uniqued) {
        serviceDefinitionMap.remove(uniqued);
    }

    //获取服务定义信息
    public ServiceDefinition getServiceDefinition(String uniqued) {
        return serviceDefinitionMap.get(uniqued);
    }

    //获取所有服务定义
    public ConcurrentHashMap<String, ServiceDefinition> getServiceDefinitionMap() {
        return serviceDefinitionMap;
    }

    /****************对服务实例缓存进行操作*************************/
    //获取服务实例
    public Set<ServiceInstance> getServiceInstanceByUniquedId(String uniquedId) {
        return serviceInstanceMap.get(uniquedId);
    }

    //添加单个服务实例
    public void getServiceInstance(String uniquedId, ServiceInstance instance) {
        serviceInstanceMap.get(uniquedId).add(instance);
    }

    //添加服务实例列表
    public void addServiceInstance(String uniquedId, Set<ServiceInstance> instances) {
        serviceInstanceMap.put(uniquedId, instances);
    }

    //更新服务实例列表
    public void setServiceInstance(String uniquedId, Set<ServiceInstance> instances) {
        Set<ServiceInstance> set = serviceInstanceMap.get(uniquedId);
        Iterator<ServiceInstance> it = set.iterator();
        while (it.hasNext()) {
            ServiceInstance instance = it.next();
            if (instance.getServiceInstanceId().equals(instance.getServiceInstanceId())) {
                it.remove();
                break;
            }
            set.add(instance);
        }
    }

    //删除服务实例
    public void removeServiceInstance(String uniquedId, Set<ServiceInstance> instances) {
        Set<ServiceInstance> set = serviceInstanceMap.get(uniquedId);
        Iterator<ServiceInstance> it = set.iterator();
        while (it.hasNext()) {
            ServiceInstance instance = it.next();
            if (instance.getServiceInstanceId().equals(instance.getServiceInstanceId())) {
                it.remove();
                break;
            }
        }
    }

    //删除服务对应所有实例
    public void removeServiceInstance(String uniquedId) {
        serviceInstanceMap.remove(uniquedId);
    }

    /**********************对规则缓存操作的方法**************************/


    //添加多个规则
    public void putAllRule(List<Rule> ruleList) {
        ConcurrentHashMap<String, Rule> newRuleMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Rule> newPathMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, List<Rule>> newServiceMap = new ConcurrentHashMap<>();
        //处理规则
        for (Rule rule : ruleList) {
            newRuleMap.put(rule.getId(), rule);
            //拿到以前的
            List<Rule> rules = newServiceMap.get(rule.getServicedId());
            if (rules == null) {
                rules = new ArrayList<>();
            }
            //往以前的里面加上新的
            rules.add(rule);
            newServiceMap.put(rule.getServicedId(), rules);
            List<String> paths = rule.getPaths();
            for (String path : paths) {
                String key = rule.getServicedId() + "." + path;
                newPathMap.put(key, rule);
            }
        }
        this.ruleMap = newRuleMap;
        this.pathRuleMap = newPathMap;
        this.serviceRuleMap = newServiceMap;
    }

    //获取规则
    public Rule getRule(String ruleId) {
        return ruleMap.get(ruleId);
    }

    //删除规则
    public void removeRule(String ruleId) {
        ruleMap.remove(ruleId);
    }

    //添加单个规则
    public ConcurrentHashMap<String, Rule> getRuleMap() {
        return ruleMap;
    }

}
