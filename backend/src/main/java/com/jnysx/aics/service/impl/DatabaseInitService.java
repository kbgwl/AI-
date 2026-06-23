package com.jnysx.aics.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class DatabaseInitService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        createTenantTable();
        createPlanTable();
        createOrderTable();
        createPaymentTable();
        createSubscriptionTable();
        addTenantIdColumns();
        seedDefaultPlan();
    }

    private void createTenantTable() {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS `tb_tenant` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`tenant_code` VARCHAR(50) NOT NULL COMMENT '租户编码'," +
                "`tenant_name` VARCHAR(100) NOT NULL COMMENT '租户名称'," +
                "`contact_name` VARCHAR(50) COMMENT '联系人'," +
                "`contact_phone` VARCHAR(20) COMMENT '联系电话'," +
                "`contact_email` VARCHAR(100) COMMENT '联系邮箱'," +
                "`plan_id` BIGINT COMMENT '套餐ID'," +
                "`max_agents` INT DEFAULT 5 COMMENT '最大坐席数'," +
                "`max_sessions` INT DEFAULT 100 COMMENT '最大会话数'," +
                "`logo` VARCHAR(500) COMMENT '租户LOGO'," +
                "`domain` VARCHAR(200) COMMENT '自定义域名'," +
                "`status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常 2过期'," +
                "`expire_time` DATETIME COMMENT '到期时间'," +
                "`remark` VARCHAR(500) COMMENT '备注'," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "`deleted` TINYINT DEFAULT 0," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_tenant_code` (`tenant_code`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表'"
            );
            log.info("Tenant table initialized");
        } catch (Exception e) {
            log.error("Failed to init tenant table: {}", e.getMessage());
        }
    }

    private void createPlanTable() {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS `tb_plan` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`plan_code` VARCHAR(50) NOT NULL COMMENT '套餐编码'," +
                "`plan_name` VARCHAR(100) NOT NULL COMMENT '套餐名称'," +
                "`description` VARCHAR(500) COMMENT '套餐描述'," +
                "`price` DECIMAL(10,2) DEFAULT 0 COMMENT '价格'," +
                "`price_unit` VARCHAR(20) DEFAULT '月' COMMENT '价格单位：月/年'," +
                "`duration_days` INT DEFAULT 30 COMMENT '时长（天）'," +
                "`max_agents` INT DEFAULT 5 COMMENT '最大坐席数'," +
                "`max_sessions` INT DEFAULT 100 COMMENT '最大会话数'," +
                "`max_knowledge_items` INT DEFAULT 100 COMMENT '最大知识条目数'," +
                "`features` TEXT COMMENT '功能配置JSON'," +
                "`sort` INT DEFAULT 0 COMMENT '排序'," +
                "`status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用'," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "`deleted` TINYINT DEFAULT 0," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_plan_code` (`plan_code`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表'"
            );
            log.info("Plan table initialized");
        } catch (Exception e) {
            log.error("Failed to init plan table: {}", e.getMessage());
        }
    }

    private void createOrderTable() {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS `tb_order` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`order_no` VARCHAR(50) NOT NULL COMMENT '订单号'," +
                "`tenant_id` BIGINT COMMENT '租户ID'," +
                "`plan_id` BIGINT COMMENT '套餐ID'," +
                "`plan_name` VARCHAR(100) COMMENT '套餐名称'," +
                "`amount` DECIMAL(10,2) DEFAULT 0 COMMENT '原价'," +
                "`discount_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额'," +
                "`pay_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '实付金额'," +
                "`quantity` INT DEFAULT 1 COMMENT '数量'," +
                "`duration_days` INT DEFAULT 30 COMMENT '时长天数'," +
                "`status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending/paid/refunded/cancelled'," +
                "`pay_channel` VARCHAR(30) COMMENT '支付渠道：alipay/wechat'," +
                "`pay_trade_no` VARCHAR(100) COMMENT '第三方交易号'," +
                "`pay_time` DATETIME COMMENT '支付时间'," +
                "`expire_time` DATETIME COMMENT '到期时间'," +
                "`remark` VARCHAR(500) COMMENT '备注'," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "`deleted` TINYINT DEFAULT 0," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_order_no` (`order_no`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表'"
            );
            log.info("Order table initialized");
        } catch (Exception e) {
            log.error("Failed to init order table: {}", e.getMessage());
        }
    }

    private void createPaymentTable() {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS `tb_payment` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`payment_no` VARCHAR(50) NOT NULL COMMENT '支付流水号'," +
                "`order_id` BIGINT COMMENT '订单ID'," +
                "`order_no` VARCHAR(50) COMMENT '订单号'," +
                "`amount` DECIMAL(10,2) DEFAULT 0 COMMENT '支付金额'," +
                "`channel` VARCHAR(30) COMMENT '支付渠道'," +
                "`trade_no` VARCHAR(100) COMMENT '第三方交易号'," +
                "`status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending/paid/failed/refunded'," +
                "`buyer_info` VARCHAR(500) COMMENT '买家信息'," +
                "`pay_time` DATETIME COMMENT '支付时间'," +
                "`raw_notify` TEXT COMMENT '回调原始数据'," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_payment_no` (`payment_no`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表'"
            );
            log.info("Payment table initialized");
        } catch (Exception e) {
            log.error("Failed to init payment table: {}", e.getMessage());
        }
    }

    private void createSubscriptionTable() {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS `tb_subscription` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`tenant_id` BIGINT COMMENT '租户ID'," +
                "`plan_id` BIGINT COMMENT '套餐ID'," +
                "`plan_code` VARCHAR(50) COMMENT '套餐编码'," +
                "`plan_name` VARCHAR(100) COMMENT '套餐名称'," +
                "`order_id` BIGINT COMMENT '订单ID'," +
                "`max_agents` INT DEFAULT 5," +
                "`max_sessions` INT DEFAULT 100," +
                "`max_knowledge_items` INT DEFAULT 100," +
                "`status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/expired/replaced/cancelled'," +
                "`start_date` DATETIME COMMENT '开始时间'," +
                "`expire_date` DATETIME COMMENT '到期时间'," +
                "`auto_renew` TINYINT DEFAULT 0 COMMENT '自动续费'," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅表'"
            );
            log.info("Subscription table initialized");
        } catch (Exception e) {
            log.error("Failed to init subscription table: {}", e.getMessage());
        }
    }

    private void addTenantIdColumns() {
        String[] tables = {"tb_agent", "kb_item", "kb_category", "tb_ticket", "tb_session", "tb_message", "tb_intent", "tb_dialog_flow"};
        for (String table : tables) {
            try {
                Boolean exists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + table + "' AND COLUMN_NAME = 'tenant_id'",
                    Boolean.class
                );
                if (exists == null || !exists) {
                    jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD COLUMN `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID'");
                }
            } catch (Exception e) {
                log.debug("Skip tenant_id for {}: {}", table, e.getMessage());
            }
        }
        log.info("Tenant ID columns added");
    }

    private void seedDefaultPlan() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_plan", Integer.class);
            if (count != null && count == 0) {
                String features = "{\"dashboard\":true,\"knowledge_base\":true,\"intent_manage\":true,\"agent_manage\":true,\"channel_manage\":true,\"dialog_flow\":true,\"ticket_manage\":true,\"operation_log\":true,\"smart_routing\":false,\"quality_check\":false,\"marketing_rules\":false,\"advanced_reports\":false,\"multi_tenant\":false,\"api_access\":false,\"custom_deployment\":false}";
                jdbcTemplate.execute("INSERT INTO tb_plan (plan_code, plan_name, description, price, price_unit, duration_days, max_agents, max_sessions, max_knowledge_items, features, sort, status) VALUES ('trial', '试用版', '7天免费试用，基础功能体验', 0, '月', 7, 2, 50, 20, '" + features + "', 1, 1)");
                
                features = features.replace("\"smart_routing\":false", "\"smart_routing\":false");
                jdbcTemplate.execute("INSERT INTO tb_plan (plan_code, plan_name, description, price, price_unit, duration_days, max_agents, max_sessions, max_knowledge_items, features, sort, status) VALUES ('basic', '基础版', '适合小型团队，标准客服功能', 999, '月', 30, 5, 100, 200, '" + features + "', 2, 1)");
                
                String proFeatures = features.replace("\"smart_routing\":false", "\"smart_routing\":true").replace("\"quality_check\":false", "\"quality_check\":true").replace("\"marketing_rules\":false", "\"marketing_rules\":true").replace("\"advanced_reports\":false", "\"advanced_reports\":true");
                jdbcTemplate.execute("INSERT INTO tb_plan (plan_code, plan_name, description, price, price_unit, duration_days, max_agents, max_sessions, max_knowledge_items, features, sort, status) VALUES ('pro', '专业版', '适合中型企业，智能路由+质检', 2999, '月', 30, 20, 500, 1000, '" + proFeatures + "', 3, 1)");
                
                String entFeatures = proFeatures.replace("\"multi_tenant\":false", "\"multi_tenant\":true").replace("\"api_access\":false", "\"api_access\":true").replace("\"custom_deployment\":false", "\"custom_deployment\":true");
                jdbcTemplate.execute("INSERT INTO tb_plan (plan_code, plan_name, description, price, price_unit, duration_days, max_agents, max_sessions, max_knowledge_items, features, sort, status) VALUES ('enterprise', '企业版', '大型企业定制，全部功能', 9999, '月', 30, 100, 1000, 10000, '" + entFeatures + "', 4, 1)");
                
                log.info("Default plans seeded");
            }
        } catch (Exception e) {
            log.error("Failed to seed plans: {}", e.getMessage());
        }
    }
}
