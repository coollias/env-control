package com.bank.config.client.gui;

import com.bank.config.client.ConfigClient;
import com.bank.config.client.poller.ConfigChangeListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 配置客户端GUI界面
 * 用于调试和监控配置客户端
 * 
 * @author bank
 */
public class ConfigClientGUI extends JFrame {
    
    private ConfigClient configClient;
    private ScheduledExecutorService scheduler;
    
    // GUI组件
    private JTextArea configArea;
    private JTextArea metricsArea;
    private JTextArea logArea;
    private JLabel statusLabel;
    private JButton refreshButton;
    private JButton startButton;
    private JButton stopButton;
    private JTextField configKeyField;
    private JTextField configValueField;
    private JButton getConfigButton;
    
    public ConfigClientGUI() {
        initComponents();
        initConfigClient();
        setupScheduler();
    }
    
    private void initComponents() {
        setTitle("配置客户端调试工具");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 顶部控制面板
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // 中间配置面板
        JPanel configPanel = createConfigPanel();
        mainPanel.add(configPanel, BorderLayout.CENTER);
        
        // 底部日志面板
        JPanel logPanel = createLogPanel();
        mainPanel.add(logPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("控制面板"));
        
        // 状态标签
        statusLabel = new JLabel("状态: 未初始化");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel);
        
        // 启动按钮
        startButton = new JButton("启动客户端");
        startButton.addActionListener(e -> startClient());
        panel.add(startButton);
        
        // 停止按钮
        stopButton = new JButton("停止客户端");
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopClient());
        panel.add(stopButton);
        
        // 刷新按钮
        refreshButton = new JButton("手动刷新");
        refreshButton.setEnabled(false);
        refreshButton.addActionListener(e -> refreshConfig());
        panel.add(refreshButton);
        
        return panel;
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        
        // 左侧：配置查看
        JPanel configViewPanel = new JPanel(new BorderLayout());
        configViewPanel.setBorder(BorderFactory.createTitledBorder("配置查看"));
        
        configArea = new JTextArea();
        configArea.setEditable(false);
        configArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane configScrollPane = new JScrollPane(configArea);
        configViewPanel.add(configScrollPane, BorderLayout.CENTER);
        
        // 配置查询面板
        JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        queryPanel.add(new JLabel("配置键:"));
        configKeyField = new JTextField(15);
        queryPanel.add(configKeyField);
        queryPanel.add(new JLabel("值:"));
        configValueField = new JTextField(20);
        configValueField.setEditable(false);
        queryPanel.add(configValueField);
        getConfigButton = new JButton("查询");
        getConfigButton.setEnabled(false);
        getConfigButton.addActionListener(e -> queryConfig());
        queryPanel.add(getConfigButton);
        configViewPanel.add(queryPanel, BorderLayout.SOUTH);
        
        // 右侧：监控指标
        JPanel metricsPanel = new JPanel(new BorderLayout());
        metricsPanel.setBorder(BorderFactory.createTitledBorder("监控指标"));
        
        metricsArea = new JTextArea();
        metricsArea.setEditable(false);
        metricsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane metricsScrollPane = new JScrollPane(metricsArea);
        metricsPanel.add(metricsScrollPane, BorderLayout.CENTER);
        
        panel.add(configViewPanel);
        panel.add(metricsPanel);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("操作日志"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        panel.add(logScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void initConfigClient() {
        try {
            configClient = new ConfigClient.ConfigClientBuilder()
                .serverUrl("http://localhost:8080")
                .appCode("1003")
                .envCode("dev")
                .token("Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwLCJzdWIiOiJjb29sbGlhcyIsImlhdCI6MTc1NDQzOTg1NCwiZXhwIjoxODQwODM5ODU0fQ.joAFmm_rW3Nm_w6848RHIuotBAofqAvjsooC0jzr17A")
                .pollInterval(30000)
                .cacheFile("/tmp/config-client-debug.yaml")
                .enablePolling(true)
                .enableCache(true)
                .build();
            
            // 添加配置变更监听器
            configClient.addConfigChangeListener(new ConfigChangeListener() {
                @Override
                public void onConfigChange(String key, String oldValue, String newValue) {
                    SwingUtilities.invokeLater(() -> {
                        log("配置变更: " + key + " = " + oldValue + " -> " + newValue);
                        updateConfigDisplay();
                    });
                }
                
                @Override
                public void onConfigRefresh(Map<String, String> newConfigs) {
                    SwingUtilities.invokeLater(() -> {
                        log("配置刷新，共" + newConfigs.size() + "个配置项");
                        updateConfigDisplay();
                    });
                }
            });
            
            log("配置客户端创建成功");
        } catch (Exception e) {
            log("配置客户端创建失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupScheduler() {
        scheduler = Executors.newScheduledThreadPool(1);
        // 每5秒更新一次监控指标
        scheduler.scheduleAtFixedRate(this::updateMetrics, 0, 5, TimeUnit.SECONDS);
    }
    
    private void startClient() {
        try {
            configClient.initialize();
            statusLabel.setText("状态: 运行中");
            statusLabel.setForeground(Color.GREEN);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            refreshButton.setEnabled(true);
            getConfigButton.setEnabled(true);
            log("配置客户端启动成功");
            updateConfigDisplay();
        } catch (Exception e) {
            log("配置客户端启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void stopClient() {
        try {
            configClient.stop();
            statusLabel.setText("状态: 已停止");
            statusLabel.setForeground(Color.RED);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            refreshButton.setEnabled(false);
            getConfigButton.setEnabled(false);
            log("配置客户端已停止");
        } catch (Exception e) {
            log("停止配置客户端失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void refreshConfig() {
        try {
            configClient.refreshConfig();
            log("手动刷新配置完成");
        } catch (Exception e) {
            log("手动刷新配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void queryConfig() {
        String key = configKeyField.getText().trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入配置键", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String value = configClient.getConfig(key);
            configValueField.setText(value != null ? value : "未找到");
            log("查询配置: " + key + " = " + value);
        } catch (Exception e) {
            log("查询配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateConfigDisplay() {
        try {
            Map<String, String> configs = configClient.getAllConfigs();
            StringBuilder sb = new StringBuilder();
            sb.append("配置项总数: ").append(configs.size()).append("\n");
            sb.append("最后更新时间: ").append(configClient.getCache().getLastUpdateTime()).append("\n");
            sb.append("缓存版本: ").append(configClient.getCache().getVersion()).append("\n\n");
            
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
            }
            
            configArea.setText(sb.toString());
        } catch (Exception e) {
            log("更新配置显示失败: " + e.getMessage());
        }
    }
    
    private void updateMetrics() {
        try {
            if (configClient != null && configClient.isHealthy()) {
                Map<String, Object> metrics = configClient.getMetricsData();
                StringBuilder sb = new StringBuilder();
                sb.append("健康状态: ").append(configClient.isHealthy() ? "健康" : "不健康").append("\n");
                sb.append("拉取器状态: ").append(configClient.getPoller().isRunning() ? "运行中" : "已停止").append("\n");
                sb.append("缓存大小: ").append(configClient.getCache().size()).append("\n\n");
                
                for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                    sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
                
                final String metricsText = sb.toString();
                SwingUtilities.invokeLater(() -> metricsArea.setText(metricsText));
            }
        } catch (Exception e) {
            log("更新监控指标失败: " + e.getMessage());
        }
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + java.time.LocalDateTime.now().toString() + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            ConfigClientGUI gui = new ConfigClientGUI();
            gui.setVisible(true);
        });
    }
} 