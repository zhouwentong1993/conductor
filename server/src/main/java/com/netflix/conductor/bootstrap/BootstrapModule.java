package com.netflix.conductor.bootstrap;

import com.google.inject.AbstractModule;
import com.netflix.conductor.core.config.Configuration;
import com.netflix.conductor.core.config.SystemPropertiesConfiguration;

public class BootstrapModule extends AbstractModule {
    @Override
    protected void configure() {
        // 加载系统配置
        bind(Configuration.class).to(SystemPropertiesConfiguration.class);
        // 配置 Service 相关内容
        bind(ModulesProvider.class);
    }
}
