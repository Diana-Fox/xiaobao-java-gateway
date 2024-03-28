package com.xiaobao.common.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 核心规则类
 */
@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Rule implements Comparable<Rule>, Serializable {
    /**
     * 规则ID，全局唯一
     */
    private String id;
    /**
     * 规则名称
     */
    private String name;
    /**
     * 协议
     */
    private String protocol;
    /**
     * 后端服务id
     */
    private String servicedId;
    /**
     * 请求前缀
     */
    private String prefix;
    /**
     * 路径集合
     */
    private List<String> paths;
    /**
     * 规则顺序，对应的场景：一个路径对应多条规则，然后只执行一条规则的情况
     */
    private Integer order;
    private Set<FilterConfig> filterConfigSet = new HashSet<>();

    /**
     * 过滤器配置
     */
    public static class FilterConfig {
        /**
         * 过滤器唯一ID
         */
        private String id;
        /**
         * 过滤器规则描述：{“timeout":500}
         */
        private String config;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FilterConfig that = (FilterConfig) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    /**
     * 向规则中添加过滤器
     *
     * @param filterConfig
     * @return
     */
    public boolean addFilterConfig(FilterConfig filterConfig) {
        return filterConfigSet.add(filterConfig);
    }

    /**
     * 通过id获取FilterConfig
     *
     * @param id
     * @return
     */
    public FilterConfig getFilterConfig(String id) {
        for (FilterConfig filterConfig : filterConfigSet) {
            if (filterConfig.id.equalsIgnoreCase(id)) {
                return filterConfig;
            }
        }
        return null;
    }

    @Override
    public int compareTo(Rule o) {
        int orderCompare = Integer.compare(getOrder(), o.getOrder());
        if (orderCompare == 0) {
            return getId().compareTo(o.getId());
        }
        return orderCompare;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Rule that = (Rule) obj;
        return id.equals(that.id);
    }
}
