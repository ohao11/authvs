1. 技术栈强制要求：
  - JDK 版本：Java 21（优先使用密封类、record 类、模式匹配等 Java 21 新特性）；
  - 框架版本：Spring Boot 4.x（遵循 Spring Boot 4 官方最佳实践，如简化的自动配置、原生 AOT 兼容）；
  - 依赖规范：使用 Spring Boot Starter 统一管理依赖，避免手动指定第三方依赖版本。
  - 命名规范：接口出参 VO 类命名统一使用 "Vo" 后缀放到对应的out包内，接口入参 DTO 类命名统一使用 "Param" 后缀放到对应的in包内。
  - 代码规范：入参的类字段需要校验使用hibernate-validator注解进行校验，接口使用OpenAPI注解进行描述。
  - 如果是编码任务，都是用mvn编译检查是否通过。
2. 普通要求：
  - 编写完后除了README.md外，不要修改或新增其他文件。
  