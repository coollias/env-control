new Vue({
    el: '#app',
    data() {
        return {
            // 客户端状态
            clientStatus: {
                running: false,
                lastUpdateTime: '',
                configCount: 0,
                version: ''
            },
            // 配置数据
            allConfigs: {},
            businessConfig: {},
            // 监控指标
            metrics: {},
            // 缓存信息
            cacheInfo: {},
            // 日志
            logs: [],
            // 加载状态
            loading: {
                refresh: false,
                start: false,
                stop: false,
                health: false,
                allConfigs: false,
                businessConfig: false
            },
            // 定时器
            statusTimer: null,
            configTimer: null
        }
    },
    mounted() {
        this.init();
        this.startAutoRefresh();
    },
    beforeDestroy() {
        this.stopAutoRefresh();
    },
    methods: {
        // 初始化
        init() {
            this.addLog('info', '前端界面初始化完成');
            this.refreshStatus();
            this.getCacheInfo();
        },

        // 自动刷新
        startAutoRefresh() {
            // 每5秒刷新一次状态
            this.statusTimer = setInterval(() => {
                this.refreshStatus();
            }, 5000);

            // 每10秒刷新一次配置
            this.configTimer = setInterval(() => {
                this.getAllConfigs();
                this.getBusinessConfig();
            }, 10000);
        },

        stopAutoRefresh() {
            if (this.statusTimer) {
                clearInterval(this.statusTimer);
                this.statusTimer = null;
            }
            if (this.configTimer) {
                clearInterval(this.configTimer);
                this.configTimer = null;
            }
        },

        // 添加日志
        addLog(type, message) {
            const time = new Date().toLocaleTimeString();
            this.logs.unshift({
                time,
                type,
                message
            });
            
            // 限制日志数量
            if (this.logs.length > 100) {
                this.logs = this.logs.slice(0, 100);
            }
        },

        // 清空日志
        clearLogs() {
            this.logs = [];
            this.addLog('info', '日志已清空');
        },

        // API请求封装
        async apiRequest(url, method = 'GET', data = null) {
            try {
                const config = {
                    method,
                    url: `http://localhost:8081${url}`,
                    timeout: 5000
                };
                
                if (data) {
                    config.data = data;
                }

                const response = await axios(config);
                return response.data;
            } catch (error) {
                console.error('API请求失败:', error);
                this.addLog('error', `API请求失败: ${error.message}`);
                throw error;
            }
        },

        // 刷新客户端状态
        async refreshStatus() {
            try {
                const health = await this.apiRequest('/api/config/health');
                if (health.success) {
                    this.clientStatus = {
                        running: health.data.running || false,
                        lastUpdateTime: health.data.lastUpdateTime || '',
                        configCount: health.data.configCount || 0,
                        version: health.data.version || ''
                    };
                }
            } catch (error) {
                this.clientStatus = {
                    running: false,
                    lastUpdateTime: '',
                    configCount: 0,
                    version: ''
                };
            }
        },

        // 刷新配置
        async refreshConfig() {
            this.loading.refresh = true;
            try {
                const result = await this.apiRequest('/api/config/refresh', 'POST');
                if (result.success) {
                    this.addLog('success', '配置刷新成功');
                    this.$message.success('配置刷新成功');
                    // 刷新相关数据
                    this.getAllConfigs();
                    this.getBusinessConfig();
                    this.getCacheInfo();
                } else {
                    this.addLog('error', `配置刷新失败: ${result.message}`);
                    this.$message.error(`配置刷新失败: ${result.message}`);
                }
            } catch (error) {
                this.addLog('error', '配置刷新失败');
                this.$message.error('配置刷新失败');
            } finally {
                this.loading.refresh = false;
            }
        },

        // 启动客户端
        async startClient() {
            this.loading.start = true;
            try {
                const result = await this.apiRequest('/api/config/start', 'POST');
                if (result.success) {
                    this.addLog('success', '客户端启动成功');
                    this.$message.success('客户端启动成功');
                    this.refreshStatus();
                } else {
                    this.addLog('error', `客户端启动失败: ${result.message}`);
                    this.$message.error(`客户端启动失败: ${result.message}`);
                }
            } catch (error) {
                this.addLog('error', '客户端启动失败');
                this.$message.error('客户端启动失败');
            } finally {
                this.loading.start = false;
            }
        },

        // 停止客户端
        async stopClient() {
            this.loading.stop = true;
            try {
                const result = await this.apiRequest('/api/config/stop', 'POST');
                if (result.success) {
                    this.addLog('success', '客户端停止成功');
                    this.$message.success('客户端停止成功');
                    this.refreshStatus();
                } else {
                    this.addLog('error', `客户端停止失败: ${result.message}`);
                    this.$message.error(`客户端停止失败: ${result.message}`);
                }
            } catch (error) {
                this.addLog('error', '客户端停止失败');
                this.$message.error('客户端停止失败');
            } finally {
                this.loading.stop = false;
            }
        },

        // 健康检查
        async getHealth() {
            this.loading.health = true;
            try {
                const result = await this.apiRequest('/api/config/health');
                if (result.success) {
                    this.addLog('success', '健康检查通过');
                    this.$message.success('健康检查通过');
                } else {
                    this.addLog('warning', `健康检查失败: ${result.message}`);
                    this.$message.warning(`健康检查失败: ${result.message}`);
                }
            } catch (error) {
                this.addLog('error', '健康检查失败');
                this.$message.error('健康检查失败');
            } finally {
                this.loading.health = false;
            }
        },

        // 获取所有配置
        async getAllConfigs() {
            this.loading.allConfigs = true;
            try {
                const result = await this.apiRequest('/api/config/all');
                if (result.success) {
                    this.allConfigs = result.data || {};
                    this.addLog('info', `获取到 ${Object.keys(this.allConfigs).length} 个配置项`);
                } else {
                    this.addLog('error', `获取配置失败: ${result.message}`);
                }
            } catch (error) {
                this.addLog('error', '获取配置失败');
            } finally {
                this.loading.allConfigs = false;
            }
        },

        // 获取业务配置
        async getBusinessConfig() {
            this.loading.businessConfig = true;
            try {
                const result = await this.apiRequest('/api/business/config');
                if (result.success) {
                    this.businessConfig = result.data || {};
                    this.addLog('info', `获取到 ${Object.keys(this.businessConfig).length} 个业务配置项`);
                } else {
                    this.addLog('error', `获取业务配置失败: ${result.message}`);
                }
            } catch (error) {
                this.addLog('error', '获取业务配置失败');
            } finally {
                this.loading.businessConfig = false;
            }
        },

        // 获取监控指标
        async getMetrics() {
            try {
                const result = await this.apiRequest('/api/config/metrics');
                if (result.success) {
                    this.metrics = result.data || {};
                    this.addLog('info', '监控指标更新成功');
                } else {
                    this.addLog('error', `获取监控指标失败: ${result.message}`);
                }
            } catch (error) {
                this.addLog('error', '获取监控指标失败');
            }
        },

        // 获取缓存信息
        async getCacheInfo() {
            try {
                const result = await this.apiRequest('/api/config/cache');
                if (result.success) {
                    this.cacheInfo = result.data || {};
                    this.addLog('info', '缓存信息更新成功');
                } else {
                    this.addLog('error', `获取缓存信息失败: ${result.message}`);
                }
            } catch (error) {
                this.addLog('error', '获取缓存信息失败');
            }
        }
    }
}); 