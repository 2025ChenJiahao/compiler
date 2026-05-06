package cn.edu.nwpu.sonnx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * S-ONNXCompiler 后端服务启动类
 * * 作为西工大（NWPU）软件学院的编译器实践项目，
 * 本启动类负责初始化 Spring 上下文并开启 Web 容器。
 */
@SpringBootApplication
public class CompilerApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(CompilerApplication.class, args);

        System.out.println("=================================================");
        System.out.println("   S-ONNX 编译器后端服务已成功启动！");
        System.out.println("   API 地址: http://localhost:8080/api/compiler/compile");
        System.out.println("   等待前端 Web IDE 的编译请求...");
        System.out.println("=================================================");
    }
}