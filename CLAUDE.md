# fn-captcha 滑块验证码服务

基于 Spring Boot 3 的滑块验证码生成与验证服务，支持图片处理、Redis 缓存和 Nacos 配置中心。

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.5.11
- **Spring Cloud**: 2025.0.1
- **Spring Cloud Alibaba**: 2025.0.0.0
- **Redis**: Redisson 3.52.0
- **工具库**: Hutool 5.8.38, Bouncy Castle 1.83
- **构建工具**: Maven 3.8+
- **服务发现与配置**: Nacos

## 常用命令

### 编译与构建
```bash
mvn clean compile          # 清理并编译项目
mvn package                # 打包应用（跳过测试）
mvn clean package -DskipTests  # 清理并打包（跳过测试）
```

### 运行应用
```bash
mvn spring-boot:run        # 启动开发服务器
java -jar target/fn-captcha-1.0.0.jar  # 运行打包后的 JAR
```

### 测试
```bash
mvn test                   # 运行所有单元测试
mvn test -Dtest=ClassName  # 运行指定测试类
```

### 代码检查
```bash
mvn dependency:tree        # 查看依赖树
mvn help:effective-pom     # 查看生效的 POM 配置
```

## 项目结构

```
src/main/java/com/github/pkyit/fncaptcha/
├── component/              # 初始化组件（启动时执行的任务）
│   └── InitCaptchaCacheRunner.java
├── config/                 # 配置类
│   ├── CaptchaConfigProperties.java    # 验证码配置属性
│   ├── CaptchaConfiguration.java       # 验证码配置
│   └── RedissonConfig.java             # Redisson 配置
├── controller/             # REST API 控制器
│   └── CaptchaController.java
├── domain/                 # 领域对象
│   ├── bo/                 # 业务对象
│   │   └── CaptchaImageBO.java
│   ├── consts/             # 常量定义
│   │   └── CaptchaImageConst.java
│   ├── dto/                # 数据传输对象
│   │   ├── CaptchaGenerateDTO.java
│   │   ├── CaptchaImageResultDTO.java
│   │   ├── CaptchaVerifyDTO.java
│   │   └── Result.java
│   └── entity/             # 实体类
│       └── CaptchaImageRepository.java
├── service/                # 业务逻辑层
│   ├── SliderCaptchaService.java       # 服务接口
│   └── impl/
│       └── SliderCaptchaServiceImpl.java  # 服务实现
└── util/                   # 工具类
    └── CaptchaImageUtils.java

src/main/resources/
├── bg_images/              # 背景图片资源（40张）
├── application.yaml        # 应用配置文件
└── bootstrap.yaml          # Bootstrap 配置（Nacos）
```

## Spring Boot 3 开发规范

### 编码规范

#### 1. 依赖注入
- ✅ 使用构造函数注入（Constructor Injection），避免字段注入
- ✅ 使用 `@RequiredArgsConstructor` + `final` 字段（Lombok）
- ❌ 禁止使用 `@Autowired` 字段注入

```java
// ✅ 推荐
@Service
@RequiredArgsConstructor
public class SliderCaptchaServiceImpl implements SliderCaptchaService {
    private final StringRedisTemplate redisTemplate;
    private final CaptchaConfigProperties properties;
}

// ❌ 禁止
@Service
public class SliderCaptchaServiceImpl implements SliderCaptchaService {
    @Autowired
    private StringRedisTemplate redisTemplate;
}
```

#### 2. REST API 设计
- 使用 `@RestController` 注解
- 使用标准的 HTTP 方法（GET、POST、PUT、DELETE）
- 统一返回格式：使用 `Result<T>` 封装响应
- 路径使用 kebab-case（如 `/captcha/generate`）

```java
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {
    
    @PostMapping("/generate")
    public Result<CaptchaImageResultDTO> generate(@Valid @RequestBody CaptchaGenerateDTO dto) {
        // ...
    }
    
    @PostMapping("/verify")
    public Result<Boolean> verify(@Valid @RequestBody CaptchaVerifyDTO dto) {
        // ...
    }
}
```

#### 3. 参数校验
- 使用 Jakarta Validation（`jakarta.validation`）
- DTO 中使用 `@NotNull`、`@NotBlank`、`@Size` 等注解
- Controller 方法参数添加 `@Valid` 或 `@Validated`

```java
@Data
public class CaptchaVerifyDTO {
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;
    
    @NotNull(message = "滑动距离不能为空")
    private Integer distance;
}
```

#### 4. 异常处理
- 使用全局异常处理器（`@RestControllerAdvice`）
- 自定义业务异常类
- 统一的错误响应格式

#### 5. 配置管理
- 使用 `@ConfigurationProperties` 绑定配置
- 配置类添加 `@Component` 或 `@Configuration`
- 敏感配置使用环境变量或 Nacos 配置中心

```java
@Component
@ConfigurationProperties(prefix = "pkyit.fncaptch.config")
@Data
public class CaptchaConfigProperties {
    private String imagePath;
    private Integer imageCount;
    private Boolean encryption;
    private Boolean difficult;
    private Integer expireTime;
}
```

#### 6. Redis 操作
- 使用 `StringRedisTemplate` 或 `RedisTemplate`
- 设置合理的过期时间（TTL）
- 使用 Redisson 分布式锁处理并发场景

```java
// 存储验证码信息，设置过期时间
redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);

// 使用 Redisson 分布式锁
RLock lock = redissonClient.getLock(lockKey);
try {
    lock.lock();
    // 业务逻辑
} finally {
    lock.unlock();
}
```

#### 7. 日志规范
- 使用 SLF4J + Logback
- 使用 `@Slf4j` 注解（Lombok）
- 日志级别：ERROR（错误）、WARN（警告）、INFO（重要流程）、DEBUG（调试）
- 禁止在生产环境打印敏感信息

```java
@Slf4j
@Service
public class SliderCaptchaServiceImpl {
    
    public void generate() {
        log.info("开始生成验证码, clientId: {}", clientId);
        try {
            // 业务逻辑
        } catch (Exception e) {
            log.error("生成验证码失败, clientId: {}", clientId, e);
            throw new BusinessException("验证码生成失败");
        }
    }
}
```

#### 8. 事务管理
- 在 Service 层使用 `@Transactional`
- 明确指定事务传播行为和隔离级别
- 避免大事务，及时提交

```java
@Transactional(rollbackFor = Exception.class)
public void businessMethod() {
    // 数据库操作
}
```

### 架构约束

#### 分层架构
严格遵循以下分层结构：
- **Controller 层**: 接收请求、参数校验、响应封装
- **Service 层**: 业务逻辑处理、事务控制
- **Repository/DAO 层**: 数据访问（本项目使用 Redis）
- **Domain 层**: 领域对象（BO、DTO、Entity）
- **Util 层**: 通用工具类

#### 依赖方向
- Controller → Service → Repository
- 禁止跨层调用
- 禁止循环依赖

#### 命名规范
- Controller: `XxxController`
- Service 接口: `XxxService`
- Service 实现: `XxxServiceImpl`
- DTO: `XxxDTO`
- BO: `XxxBO`
- Entity: `XxxEntity` 或 `XxxRepository`
- Config: `XxxConfig` 或 `XxxConfiguration`
- Utils: `XxxUtils`

### 注意事项

⚠️ **重要约束**：

1. **不要修改背景图片文件**: `src/main/resources/bg_images/` 目录下的图片是验证码生成的基础资源，禁止删除或修改文件名

2. **Redis 键命名规范**: 使用 `captcha:{clientId}` 格式，确保键的唯一性和可识别性

3. **验证码过期时间**: 默认 120 秒，可根据配置调整，必须在 Redis 中设置 TTL

4. **加密配置**: `encryption` 配置项控制是否对验证码数据进行加密，生产环境建议开启

5. **难度配置**: `difficult` 配置项控制验证码难度，影响滑块大小和位置随机性

6. **Nacos 配置**: 优先从 Nacos 配置中心读取配置，`application.yaml` 作为本地备份

7. **启动初始化**: `InitCaptchaCacheRunner` 在应用启动时预加载验证码图片到缓存，不要删除或禁用

8. **线程安全**: 验证码生成涉及随机数和图片处理，确保线程安全

9. **性能优化**: 
   - 背景图片缓存在内存中，避免重复读取
   - Redis 操作使用连接池
   - 图片处理使用高效的算法

10. **安全性**:
    - 验证码只能验证一次，验证后立即删除
    - 防止暴力破解，限制同一客户端的请求频率
    - 敏感操作记录日志

## 开发环境

### 前置要求
- JDK 17+
- Maven 3.6+
- Redis 6.0+（本地或远程）
- Nacos Server（可选，用于配置中心和服务发现）

### 本地开发配置

1. **启动 Redis**
```bash
# Docker 方式
docker run -d --name redis -p 6379:6379 redis:latest

# 或使用本地安装的 Redis
redis-server
```

2. **配置 application.yaml**
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

3. **启动应用**
```bash
mvn spring-boot:run
```

4. **访问地址**
- 应用地址: http://localhost:18080/fnCaptcha
- Actuator: http://localhost:18080/fnCaptcha/actuator

### 环境变量

可通过环境变量覆盖配置：
```bash
export SPRING_DATA_REDIS_HOST=redis-server
export SPRING_DATA_REDIS_PORT=6379
export PKYIT_FNCAPTCH_CONFIG_ENCRYPTION=true
```

## API 接口

### 生成验证码
```
POST /captcha/generate
Content-Type: application/json

{
  "clientId": "unique-client-id"
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaId": "xxx",
    "backgroundImage": "base64...",
    "sliderImage": "base64...",
    "sliderY": 100
  }
}
```

### 验证验证码
```
POST /captcha/verify
Content-Type: application/json

{
  "captchaId": "xxx",
  "distance": 150,
  "clientId": "unique-client-id"
}

Response:
{
  "code": 200,
  "message": "success",
  "data": true
}
```

## 测试规范

- 单元测试放在 `src/test/java` 目录
- 测试类命名: `XxxTest`
- 使用 `@SpringBootTest` 进行集成测试
- 使用 Mock 隔离外部依赖
- 测试覆盖率目标: 核心业务逻辑 > 80%

## Git 提交规范

使用 Conventional Commits 规范：
```
feat: 新增验证码刷新功能
fix: 修复验证码验证失败的问题
docs: 更新 API 文档
style: 代码格式化
refactor: 重构验证码生成逻辑
test: 添加单元测试
chore: 更新依赖版本
```

## 常见问题

### Q: 验证码图片加载失败？
A: 检查 `bg_images` 目录是否存在，确认 `image-path` 配置正确

### Q: Redis 连接失败？
A: 检查 Redis 服务是否启动，确认 `host` 和 `port` 配置正确

### Q: 验证码验证总是失败？
A: 检查滑块距离计算逻辑，确认前后端坐标系一致

### Q: 如何增加背景图片？
A: 将新图片放入 `bg_images` 目录，更新 `image-count` 配置
