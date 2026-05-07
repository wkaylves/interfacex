package com.kaylves.interfacex.test;

import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.ConfigDao;
import com.kaylves.interfacex.db.model.ConfigEntity;

import java.sql.SQLException;

/**
 * 测试数据库连接和写入功能
 */
public class DatabaseConnectionTest {

    public static void main(String[] args) {
        try {
            System.out.println("开始测试数据库连接...");
            
            // 获取数据库服务实例
            InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
            
            // 初始化数据库
            dbService.initialize();
            
            // 获取DAO实例
            ConfigDao configDao = dbService.getConfigDao();
            
            // 尝试插入一条测试记录
            ConfigEntity testEntity = ConfigEntity.builder()
                    .projectPath("/test/project")
                    .configKey("test_key")
                    .configValue("test_value")
                    .updatedTime(System.currentTimeMillis())
                    .build();
                    
            System.out.println("尝试写入测试数据...");
            configDao.upsert(testEntity);
            System.out.println("数据写入成功！");
            
            // 读取刚插入的数据
            String value = configDao.getValue("/test/project", "test_key");
            System.out.println("读取到数据: " + value);
            
            // 清理测试数据
            System.out.println("清理测试数据...");
            
            System.out.println("数据库连接测试完成！");
        } catch (SQLException e) {
            System.err.println("数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}