# 南峰黑私房手作系统

# 目录--设计接口+知识点

## 用户管理 JWT
------------------------
## 商品管理   POI | 判空 | MetaObjectHandler | 乐观锁 | 逻辑删除 | MP插件 | Lombok | @Data | ORM
支持商品的上架、下架、分类管理及库存控制，满足商品信息维护和展示需求。 

导出几十万数据还是很慢，怎么进一步优化？（系统设计视角）
1. 源头优化（数据库层）
   流式查询 (Stream Query)：
   MySQL 驱动黑科技：stmt.setFetchSize(Integer.MIN_VALUE)。
   防御性回答：开启流式查询后，必须快速消费。如果 Java 这边写 Excel 慢，会导致数据库连接（Connection）一直被占用。如果并发高，连接池几秒钟就被耗尽了。
   解决：如果写入逻辑耗时，建议还是用传统的“ID 范围切分”或“Limit 分页”，虽然慢点，但不会拖死数据库。
2. 过程优化（应用层）
   批量落盘：EasyExcel 读取时，不要读一行插一行数据库。要在 Listener 里设置一个 BATCH_COUNT = 1000，攒够 1000 条，做一次 batchInsert，然后清空 List。
   多线程并发写入（Sheet 维度）：
   如果是写入同一个 Sheet，无法多线程（文件流是顺序的）。
   但如果是多个 Sheet（比如按月份导出），可以开线程池，每个线程负责查一个月的数据并写入对应的 Sheet，最后合并。
3. 体验优化（异步化）
   转异步 + 进度条：
   用户点击导出 -> 返回 task_id。
   前端每 3 秒轮询 status (导出中/已完成/下载链接)。
   后端异步生成文件上传到 OSS，生成下载链接。
   降级方案 (CSV)：
   如果数据量真的到了千万级，直接告诉产品经理：“Excel 承载不了，我给你导 CSV 或者 TXT”。CSV 本质是纯文本，没有样式开销，写起来飞快。
### 2. 各种判空方式的总结对比：
null:无盒子。一个变量没有指向任何内存地址  
""/Empty:有盒子，但无东西。长度=0的字符串  
" "/Blank:有盒子有透明东西。含空格、Tab键、换行符的字符串   
判空：   
2. 判空方式的演进
   原始阶段：if (s == null || s.length() == 0) —— 啰嗦，易漏，非空安全。
   工具类阶段（推荐）：Apache StringUtils —— Null-Safe（空指针安全），这是工程实践的首选。
   JDK 进化：
   Java 8：Objects.isNull() 
   Java 11：String.isBlank() —— 终于原生支持了“空白字符”判断，但注意它不是 Null-Safe 的（调用前对象不能为 null）。

1. 集合判空（List/Set/Map）
   （依赖 Apache Commons Collections / Spring Framework）：我通常使用 CollectionUtils.isEmpty(list)。它既判断了 null，也判断了 size=0，一箭双雕且代码整洁
2. StringUtils 的 isEmpty vs isBlank （必考题）
   Apache Commons Lang 中这两个方法的区别是面试陷阱：
   StringUtils.isEmpty(" ") = false （它认为空格是有内容的，只是长度不为0）
   StringUtils.isBlank(" ") = true （它认为空格也是空的，包含空白字符）
   结论：绝大多数业务场景（如表单校验），我们应该用 isBlank，因为用户输入一堆空格对后端来说通常等同于没输。
3. Java 8 的 Optional（优雅判空）
   "对于链式调用中的判空（例如：user.getAddress().getCity()），为了避免多层 if (null) 嵌套，我会使用 Optional。"
// Optional 优雅版
return Optional.ofNullable(user)
.map(User::getAddress) 
.orElse("Unknown");
判空时，如果这个字符串后续要作为 Key 放入 ConcurrentHashMap，必须严格检查 != null，否则会直接抛出空指针异常，这在多线程缓存场景常被忽略。”

1. 为什么 ConcurrentHashMap 的 Key 和 Value 都不能为 null？
   二义性问题：如果 map.get(key) 返回 null，我无法分辨是“这个 key 不存在”还是“这个 key 对应的值是 null”。
   在 HashMap（非线程安全）中，可以通过 map.containsKey(key) 来进一步确认。
   但在多线程环境下，你刚检查完 containsKey，可能另一个线程就把数据删了或改了。为了避免这种复杂的二义性，作者 Doug Lea 直接禁止了 null。
2. 数据库查询结果的判空
   MyBatis 查询结果，如果此时 List<User> 没查到数据，返回的是 null 还是 空List？
   答案：绝大多数 ORM 框架（MyBatis/Hibernate）返回的是 空List (size=0)，而不是 null。所以遍历前不需要判 null，但在取第一个元素 list.get(0) 前必须判断 !list.isEmpty()。
### 3. MetaObjectHandler自动填充创建时间和更新时间等字段
“MP 的自动填充本质上是基于 AOP 切面和 Java 反射机制实现的。
实现流程：我们只需要实现 MetaObjectHandler 接口，重写 insertFill 和 updateFill 方法，并配合实体类上的 @TableField(fill = ...) 注解即可。
核心原理：MP 在 SQL 执行前拦截 MetaObject（元对象），检测到有标记填充的字段，就通过反射将值注入进去。
最佳实践：现在我都会使用 strictInsertFill 系列方法，因为它有两个显著优势：
    智能判空：如果业务代码已经手动赋了值，它不会覆盖，尊重业务逻辑。
    性能更好：利用 Supplier 函数式接口懒加载，减少不必要的反射开销。”
    类型安全：会检查字段类型是否匹配，不匹配则忽略，避免强转异常。

1. 自动填充 UserID 的深水区（高频追问）
   对于 create_by 这类字段，我会结合 ThreadLocal 上下文，从 SecurityContext 或 Token 拦截器中传递当前 UserID 到 Handler 中进行填充。
   Q: 如果我在一个 @Async 的异步线程中执行插入操作，自动填充的 UserID 会怎样？
      会丢失（或报错）。因为 ThreadLocal 是线程隔离的，子线程（异步线程）无法读取主线程 ThreadLocal 中的数据。
   解决方案：
       手动传递：在调用异步方法前，把 userId 传进去（笨办法）。
       InheritableThreadLocal：JDK 自带的，允许子线程继承父线程变量（仅限 new Thread 时）。
       TTL (TransmittableThreadLocal)：阿里开源的方案。推荐回答。在使用线程池时，它能保证父子线程、不同线程间的 Context 传递。
2. 关于 update_time 的一个隐蔽坑
   Q: 如果我执行 update(null, updateWrapper)，自动填充会生效吗？
      不会。因为没有实体对象，MP 只有 Wrapper 里的 SQL 片段，无法通过反射填充。
### 4. 添加乐观锁机制防止并发更新冲突
乐观锁本质上是一种逻辑锁，它假设冲突很少发生，只在提交时检查数据有没有变。在我的项目中，主要有两种落地方式：
1. MP 的 @Version 插件（标准做法）：
优点：框架集成，注解即用，无感知。
适用：并发不高的通用业务（如修改个人信息）。
2. 相对扣减（实战技巧）：
场景：库存扣减。
原理：不关心旧的 version 是多少，只关心结果不能为负。
SQL：UPDATE stock = stock - 1 WHERE id = 1 AND stock > 0。
优点：完全避免了 ABA 问题，减少了一次 SELECT，且并发成功率极高（不用重试）。

Q: 如果乐观锁失败了，怎么处理？（重试机制）
Spring Retry：引入 @Retryable 注解，设置最大重试次数（如 3 次）和避退策略（间隔 100ms），防止 CPU 空转。
业务降级：如果重试 3 次还失败，直接抛出“系统繁忙”给用户，或者记录日志人工处理。
Q: 什么时候放弃乐观锁？
当写冲突非常频繁（如秒杀热点商品）时，乐观锁会导致大量请求反复重试失败，CPU 飙升但吞吐量很低。这时候必须上 Redis 分布式锁 或 悲观锁（排队论）。

乐观锁的实现方式： 
1. 版本号机制 (Versioning)  
MP通过version字段实现乐观锁机制。
- 在实体类中的version 字段上添加@Version注解
- 在 MyBatis-Plus 的配置类中注册 OptimisticLockerInnerInterceptor乐观锁插件
- 触发：仅支持 updateById(id) 或 update(entity, wrapper)，且 entity 中必须包含原始 version 值。
如果用LambdaUpdateWrapper 直接构造 set 语句，或者手写 XML SQL，乐观锁插件不会生效！
2. 时间戳机制 (Timestamping)  
逻辑上与版本号相似，但使用时间戳字段。
  
优点：  
字段本身具有业务含义，可以直接看出数据的最后修改时间。   
无需在应用层手动管理 version 的自增。   
缺点（非常重要）：   
存在时钟偏移问题：在分布式环境下，不同服务器的系统时间可能存在微小差异，这会导致乐观锁失效。   
精度问题：如果并发极高，在同一毫秒（或数据库支持的最小时间单位）内发生多次更新，时间戳可能无法区分先后顺序，导致并发问题。

3. CAS (Compare-And-Swap) 思想    
   这是乐观锁底层的设计思想，通常用于 Java 内存中的并发控制（如 AtomicInteger），而不是直接用于数据库表。
CAS 是一种底层的原子操作，它包含三个操作数：内存位置（V）、预期原值（A）和新值（B）。onlyV 的值与A 相匹配时，处理器才会用B 更新 V，否则不执行任何操作。

数据库中基于 version 的 UPDATE ... WHERE version = ? 操作，本质上就是一种宏观的 CAS 实现。

在编程语言中的应用： Java 的 java.util.concurrent.atomic 包（如 AtomicInteger）就是基于 CPU 提供的 CAS 指令实现的，用于无锁化编程。

乐观锁优缺点：
避免了悲观锁独占对象的现象，提高了并发能力，读可以并发   

1. 乐观锁只能保证一个共享变量的原子操作，互斥锁可以控制多个。   
比如银行转账，A账户扣钱和B账户加钱必须是原子操作。对于这种需要保证多个操作一致性的场景，必须用 数据库事务，并可能需要配合悲观锁来确保整个转账过程的原子性和隔离性。
2. 长时间自旋导致开销大
3. ABA问题：要解决，需加一个版本号，因为是单向递增的

4. ID生成策略
一、 常见的 ID 生成策略
   数据库自增 (Auto Inc)：简单，但分库分表会 ID 冲突，且容易暴露业务量，单体小项目用。
   UUID(36 位的字符串 = 32个16进制数字 + 4个连字符)：本地生成,全球唯一，无序但太长。无序会导致 B+ 树频繁页分裂（Page Split），严重拖慢写入性能。坚决不用做主键。
   雪花算法 (Snowflake/ASSIGN_ID)：分布式标准答案。
        结构：1位符号 + 41位时间戳 + 10位机器ID + 12位序列号 = 64位 Long。
        优势：本地生成,有序递增（B+树友好）、全局唯一、不依赖数据库。但在时钟回拨和前端展示上有坑。
   MP集成：直接用 MyBatis-Plus 的 IdType.ASSIGN_ID。

1. 数据库自增 ID (Auto Increment)  
利用数据库本身的特性（如 MySQL 的 `auto_increment`）生成 ID。
*   **优点**：
    *   简单，无需引入额外组件。
    *   ID 是数字且单调递增，对数据库索引（B+树）非常友好，写入性能好。
    *   存储空间小（Long 类型 8 字节）。
*   **缺点**：
    *   **分库分表难**：不同表或不同库的主键容易重复，无法做到全局唯一。
    *   **ID 规律泄露**：竞争对手可以通过 ID 规律（如订单号）推测出你的业务量（爬虫容易爬取）。
    *   **性能瓶颈**：强依赖数据库主库，高并发下数据库成为瓶颈。

 4. Redis 生成 (Incr)
利用 Redis 的原子操作 `INCR` 和 `INCRBY`。
*   **优点**：
    *   全局唯一，有序递增。
    *   性能高于数据库。
*   **缺点**：
    *   引入了新的组件（Redis），增加了系统维护复杂度。
    *   需要考虑 Redis 持久化和高可用问题（Redis 挂了怎么办？）。

二、 MyBatis-Plus 中的 ID 生成策略
MP 对上述策略进行了封装。通过实体类注解 `@TableId(type = IdType.XXX)` 配置。

| IdType 枚举值 | 对应策略 | 说明 |
| :--- | :--- | :--- |
| **`ASSIGN_ID`** | **雪花算法** | **MP 默认策略**（3.3.0+）。如果不设置 type，默认就是这个。MP 内部实现了雪花算法，支持自动回填 ID。 |
| `AUTO` | 数据库自增 | 依赖数据库的 auto_increment，插入时不需要设置 ID。 |
| `ASSIGN_UUID` | UUID | 生成不带中划线的 UUID 字符串，实体类 ID 需为 String 类型。 |
| `INPUT` | 用户输入 | 开发者必须在 insert 前手动 `setId()`，否则为 null。 |

Q1: 为什么不推荐使用 UUID 作为主键？
> 1.  **存储消耗大**：字符串比 Long 类型占用更多空间，不仅浪费磁盘，也浪费内存中的索引空间。
> 2.  **索引性能差（核心）**：MySQL 默认使用 InnoDB 引擎，其主键索引是 **B+ 树**。B+ 树要求数据最好是顺序写入的。UUID 是**无序**的，插入时会造成大量的**随机 IO** 和 **页分裂 (Page Splitting)**，导致数据移动和碎片产生，严重降低写入性能。

Q2: 雪花算法的“时钟回拨”问题怎么解决？
> 雪花算法依赖系统时间，如果服务器时间回调（比如 NTP 同步），可能生成重复 ID。解决思路有：
> 1.  **抛出异常**：最简单的做法，发现当前时间 < 上次生成时间，直接拒绝请求（适合对可用性要求不极端的场景）。
> 2.  **等待**：如果回拨时间很短（如几毫秒），让线程休眠一小会儿，追上时间后再生成。
> 3.  **备用 Worker ID**：如果回拨时间较长，可以临时切换到备用的 Worker ID (机器 ID) 去生成，避免 ID 冲突。

Q3: 你的系统如果是分库分表，ID 怎么处理？
> 如果是分库分表，数据库自增 ID 就失效了，因为不同库会产生相同的 ID 1, 2, 3。
> 我们采用 **雪花算法 (MyBatis-Plus 的 ASSIGN_ID)**。因为它生成的 ID 是包含时间戳和机器标识的 64 位整数，天然保证了在分布式环境下的全局唯一性，而且粗略有序，不会影响分片后的排序和索引性能。

Q4：前端精度丢失问题 (JavaScript Long Precision Loss)
> 现象：后端生成的 Snowflake ID 是 19 位 long（64 bit），但 JavaScript 的 Number 类型最大安全整数是 53 bit (2^53 - 1)。
> 后果：前端拿到 ID 后最后几位会变成 000，导致 ID 不一致，查询 404。
> 解决方案：
> 后端处理：在 ID 字段上加 @JsonSerialize(using = ToStringSerializer.class)，将 Long 转为 String 传给前端。
> 全局配置：配置 Jackson 序列化器，统一将所有 Java Long 转换为 JSON String。

"项目中使用了 MyBatis-Plus 提供的默认策略 `ASSIGN_ID`，其底层是基于**雪花算法**实现的。它既保证了分布式环境下的全局唯一性，又因为整体呈递增趋势，保证了数据库 B+ 树索引的写入性能。"
### 5. 实现逻辑删除---MyBatis-Plus 的插件机制
第一步：全局配置 - application.yml配置全局默认值，这是首选。基于 MyBatis 的 插件机制 (Interceptor):MP 内部有一个 SqlInjector（SQL注入器）。
第二步：字段名非标准时实体类注解@TableLogic
第三步：MP对SQL 的自动改写  

唯一索引冲突问题：传统的 0/1 标记法会导致‘删了再建’时报 Duplicate Key 错误。我采用了充气式逻辑删除（时间戳方案），将删除字段存为 delete_time，配合联合唯一索引解决冲突。既解决了冲突，又保留了删除记录。
数据膨胀与性能问题：逻辑删除会让表越来越大。所以我设计了冷热分离策略，通过定时任务（Spring Batch）定期把三个月前的已删除数据迁移到历史表，并物理清除原表数据，保证主表的查询效率。”

Q1:如何查询被删除的数据？ 既然框架自动屏蔽了已删除数据，那如果我就是想看回收站里的内容怎么办？  
自己编写 SQL 语句。MyBatis-Plus 的逻辑删除功能只对它提供的 BaseMapper 方法和 QueryWrapper 生效。对于你在 XML 文件中或使用 @Select 注解自定义的 SQL，它不会进行任何修改，你可以自由地查询 deleted = 1 的数据

Q2:关联数据的逻辑删除（Cascade Deletion）
硬编码：在 Service 层删除部门时，手动先把该部门下的所有员工逻辑删除（在一个事务内）。
领域事件：发布“部门删除”事件，员工服务监听并处理。
### 6. MP插件总结 
“MyBatis-Plus 的插件体系基于 责任链模式 (Chain of Responsibility)，核心是一个 MybatisPlusInterceptor 容器，我们可以向里面添加各种 InnerInterceptor。
关于插件，我有两个核心心得：
添加顺序至关重要：
原则：先改变 SQL 语义的插件排前面，只改变 SQL 形式的排后面。
最佳实践：多租户 -> 动态表名 -> 乐观锁/防全表更新 -> 分页插件
原因：如果分页排在多租户前面，COUNT 查询可能会在没拼接 tenant_id 的情况下执行，导致统计了全库的数据，这不仅是数据错误，更是严重的数据泄露安全事故。
常用插件：我最常用的是分页插件和多租户插件，配合 ThreadLocal 实现了动态的数据隔离。”

以下是 MP 提供的主要插件及其功能：
1. 分页插件 (Pagination)
   工作原理：它会拦截你的查询请求，自动分析你传入的分页参数（Page 对象），然后根据你配置的数据库类型，动态地在原始 SQL 语句的末尾拼接上对应的分页查询语句（如 MySQL 的 LIMIT)。  
   开发者只需调用 mapper.selectPage(new Page<>(1, 10), queryWrapper) 即可，无需手写分页 SQL。
   不查 Count：
   "在手机端‘下滑加载更多’的场景，用户只关心有没有下一页，不关心总数。我会用 new Page<>(1, 10, false)，关闭 Count 查询，让接口响应速度提升一倍。"
   自定义 Count：
   "对于复杂的 LEFT JOIN 查询，MP 自动生成的 Count 语句效率很低。我会手动在 Mapper 里写一个 _COUNT 后缀的方法，MP 会自动检测并调用它来替代自动生成的 Count SQL。"
2. 多租户插件 (Tenant)
   白名单机制：
   "并不是所有表都需要隔离。像 sys_dict（字典）、sys_area（省市区）这种公共表，我会在拦截器配置中通过 ignoreTable 方法将它们加入白名单，防止 MP 报错。"
   超管特权：
   "当超级管理员需要跨租户统计数据时，我会利用 ThreadLocal 传递一个 flag，在拦截器的 ignoreTable 逻辑中判断：如果当前是超管且带有 flag，则跳过拼接待租户条件。"
3. 动态表名 (DynamicTableName) —— 内存泄漏陷阱
   "这个插件常用于分表场景（如按年分表 order_2023）。因为它依赖 ThreadLocal 传递表名后缀，所以必须在 finally 块中调用 ThreadLocal.remove()。
   否则在 Tomcat 线程池环境下，线程被复用时，上一个请求的表名可能会‘污染’下一个请求，导致把 2024 年的数据写到 2023 年的表里，这是非常可怕的 Bug。"
4. 防全表更新与删除插件 (IllegalSqlInnerInterceptor)  
工作原理：在 SQL 执行前，该插件会解析即将执行的 UPDATE 和 DELETE 语句。如果发现语句中缺少 WHERE 子句，它会直接抛出异常，从而阻止这条危险的 SQL 执行。

5. 特殊功能（非内部拦截器） ：逻辑删除 (Logical Delete)
其他的插件（如分页、乐观锁）都是运行时拦截器 (Runtime Interceptor)，它们在 SQL 执行的那一刻修改 SQL。
而逻辑删除不同，它更偏向于启动时注入 (Startup Injection)。MP 在应用启动扫描 Mapper 时，就直接把默认的 DELETE 方法改写成了 UPDATE 语句。所以它的性能损耗几乎为零，因为它不需要在每次请求时都去动态解析 SQL。”
### 7. Project Lombok
在 Javac 编译期 介入，直接修改了 AST (抽象语法树)。它把 Getter/Setter 等节点‘嫁接’到了语法树上，所以生成的 .class 文件里是有完整方法的，完全不影响运行时性能。
核心用法：
实体类：标准组合是 @Data + @NoArgsConstructor（给框架反射用） + @AllArgsConstructor（给 Builder 用）。
Service层：我们现在强制要求用 @RequiredArgsConstructor 代替 @Autowired，配合 final 字段实现构造器注入。
遇到的坑： @Builder 会吞掉无参构造，lombok 会默认生成一个全参构造函数,但自动取消生成无参构造函数。导致 MyBatis-Plus 查询报错。所以用了 Builder 就必须手动补上 @NoArgsConstructor 和 @AllArgsConstructor。”

1. 三种注解
A. @NoArgsConstructor 生成一个无参数的构造函数。   
   框架刚需。MP、Hibernate、Jackson 反序列化时，必须先通过反射 clazz.newInstance() 创建空对象，再调用 Setter。
B. @AllArgsConstructor  生成一个包含所有字段的构造函数。   
   开发便利。用于测试数据模拟、不涉及复杂逻辑的对象快速创建。
C. @RequiredArgsConstructor  生成一个对 所有final/@NonNull标记的字段 的构造函数。
   配合 final 字段，实现 Spring 的构造器注入，替代字段注入 (@Autowired)。

1. StackOverflowError：@Data 的死循环陷阱
   场景：双向关联（Bi-directional Relationship）。
   对象 A 有一个字段引用 B。
   对象 B 有一个字段引用 A。
   问题：@Data 会自动生成 toString()、hashCode() 和 equals() 方法。这些方法默认会打印/计算所有字段。
   A 的 toString 打印 B，B 的 toString 又打印 A…… 无限递归。
   
   解决方案：引用字段上加 @ToString.Exclude 和 @EqualsAndHashCode.Exclude，打断递归链。
2. @Accessors(chain = true)：MP 的好搭档
   场景：不想用 @Builder，但又想在一行代码里 set 完属性。
   用法：在类上加 @Accessors(chain = true)。
   效果：Setter 方法返回 this 而不是 void。

3. 构造器注入而不是 @Autowired?   
   第一层：防止空指针 (NPE)
   @Autowired 是‘先建对象再填数据’。在写单元测试时，如果我直接 new Service()，里面的 DAO 是 null，一跑就崩。
   而构造器注入是‘没有数据就不准建对象’。编译器会强迫我在 new 的时候传入依赖，保证了对象一出生就是完整可用的。
   第二层：保证不可变性 (Immutability)
   字段注入无法加 final 关键字，意味着这个 Service 在运行期间，理论上可能被人通过反射或错误代码把 DAO 改成 null，这很不安全。
   构造器注入配合 final 字段，确保了依赖关系一旦初始化，永久不变，天然线程安全。
   第三层：循环依赖的探测器
   如果是字段注入， Spring 会尝试用“三级缓存”技术默默解决这个循环依赖。但代码耦合度很高
   但如果是构造器注入，构造函数的参数列表会变得巨长（10几个参数），这会让我瞬间意识到：这个类违背了单一职责原则，需要重构了。
   另外，它能直接在这个层面暴露出循环依赖问题，让应用无法启动，而不是在运行时才发现。”
###  8. @Data
Lombok 的“全家桶”注解，旨在减少 POJO（普通 Java 对象）的样板代码。它等价于以下 5 个注解的组合：

@Getter / @Setter：生成读写方法（注意：final 字段只有 Getter）
@ToString：生成包含所有字段的 toString()。  
@EqualsAndHashCode：基于当前类字段生成 equals 和 hashcode。
@RequiredArgsConstructor：生成包含 final 和 @NonNull 字段的构造函数。

Q1: 为什么 @Data 不包含 @NoArgsConstructor？
   根本原因：Java 语法规定，final 字段必须在构造结束前被赋值。
   冲突逻辑：
   @Data 包含了 @RequiredArgsConstructor，它会生成一个带参数的构造函数来初始化 final 字段。
   一旦有了带参构造，Java 编译器就不再赠送默认的无参构造。
   Lombok 无法自动生成 @NoArgsConstructor，因为无参构造函数无法给 final 字段赋值，这会导致编译错误。
   结论：在使用 @Data 且包含 final 字段时，若框架（如 MP、Jackson）需要无参构造，必须配合 @NoArgsConstructor(force = true) 或手动处理默认值。

Q2: @Data 在继承场景下的 Equals 陷阱
场景：User 继承自 BaseEntity (包含 id, createTime)。
问题：默认情况下，@Data 生成的 equals() 和 hashCode() 方法只会比较子类特有的字段，忽略父类字段。这意味着两个 ID 不同但子类属性相同的对象，可能被判为 equals。
解决：在子类上显式添加 @EqualsAndHashCode(callSuper = true)。
### 9. ORM  (Object-Relational Mapping)
解决 面向对象语言 (Java) 与 关系型数据库 (MySQL) 之间天然的鸿沟

映射的四个层级 :
类与表：@TableName("user") —— 建立实体类与数据库表的对应关系。
主键策略：@TableId —— 决定主键是自增 (AUTO)、雪花算法 (ASSIGN_ID) 还是手动输入。
字段与列：
    显式映射：@TableField("user_name")。
    隐式映射：利用驼峰转下划线规则（userName <-> user_name）。
非数据库字段：@TableField(exist = false) —— 声明该属性仅在 Java 业务逻辑中使用（如关联查询的临时字段），不参与 CRUD SQL 生成。

类型处理器：TypeHandler (Java 与 DB 的翻译官)
“Java 里的 List<String> tags 或 Map<String, Object> extraInfo 怎么存进 MySQL？”
> 对于 List 或 Map 等复杂类型，我们可以直接在实体类字段上加上 @TableField(typeHandler = JacksonTypeHandler.class) 并开启 @TableName(autoResultMap = true)。
> 这样 MP 会在写入时自动序列化为 JSON，在读取时自动反序列化为对象，对业务代码完全透明。”
-----------------------------------------------------------------------------------
## 订单管理    AtomicReference | 事务传播行为+隔离级别 | @Transactional(redanoly = true) | 乐观锁+MQ | 读写分离一致性 | 分布式数据一致性 | 深度分页 | SQL注入 | 接口幂等性
处理用户通过微信下单的操作，支持自取和快递两种配送方式的选择，并记录完整订单流程。

### 1. AtomicReference
Q1：为什么在计算订单总金额时使用AtomicReference而不是double？

关于 AtomicReference：
在 Java Lambda 中，外部变量必须是 effectively final 的。如果不借助容器，简单的 BigDecimal sum 是无法在 forEach 里被修改的。
所以我用了 AtomicReference 作为一个‘容器’来绕过编译器检查（引用地址不变，修改内部值）。
关于 BigDecimal：
浮点数（double/float）在计算机中是二进制存储，会有精度丢失，这对金额计算是绝对不允许的。
避坑：坚决不用 new BigDecimal(0.1) 构造器，因为它会变成 0.100000005...，我会用 new BigDecimal("0.1") 字符串构造或 BigDecimal.valueOf(0.1)。”

1. 致命并发陷阱：get() + set() 不是原子的！
   虽然 AtomicReference 本身的 get 和 set 动作是原子的，但把它们组合起来——“读取旧值 -> 计算新值 -> 写入新值”——这就变成了三步操作。
   多线程环境下，线程 A 读了旧值还没写回，线程 B 也读了旧值，结果就是丢失更新。
   正确写法 (CAS)：
   “如果真的要在多线程环境下用 AtomicReference，必须使用 accumulateAndGet，它利用底层的 CAS (Compare-And-Swap) 自旋锁，保证了更新的原子性。”

2. 更优雅的替代方案：Stream Reduce (函数式编程)
    ```
    BigDecimal total = orderItems.stream()
   .map(this::calculateSubtotal) // 1. 先计算每个子项
   .reduce(BigDecimal.ZERO, BigDecimal::add); // 2. 归约求和
    ```
   无副作用 (Side-effect free)：不需要外部变量容器，天然线程安全。
   支持并行：直接换成 .parallelStream() 也能算出正确结果，不用担心锁的问题。

3. 除法与舍入 (RoundingMode) —— 经验之谈
   “金额计算最怕的不是加减乘，而是除法。
   只要涉及除法（如计算不含税金额、优惠分摊），我一定会指定精度 (Scale) 和 舍入模式 (RoundingMode)。
   否则一旦出现无限循环小数（如 10/3），BigDecimal 会直接抛出 ArithmeticException，导致生产事故。一般电商默认采用 HALF_UP（四舍五入）。”

4. 极致性能：Long 代替 BigDecimal (分币制)
   “虽然 BigDecimal 很准，但它是一个复杂的对象，内存占用大，计算慢。
   如果系统对高性能要求极高（如高频交易系统），我们会将金额单位定为‘分’，在数据库和代码中直接存 Long 类型。
   直接用 CPU 整数指令，速度快几个数量级。。只在展示给前端时转为小数。

### 2. 事务传播行为和隔离级别
**第一部分：事务传播行为 (Spring)**
REQUIRED（默认）：
  口诀：有局入局，无局组局。
  场景：绝大多数增删改查业务。
  REQUIRES_NEW：
  口诀：不管有没有局，都自己单开一桌（挂起原事务）。
  场景：日志、审计、流水。即使主业务回滚，日志也不能回滚，必须独立提交。
  SUPPORTS：
  口诀：有局就凑合吃，没局就不吃了（非事务运行）。
  场景：只读操作

NESTED 与 REQUIRES_NEW 的区别？
REQUIRES_NEW：完全独立的两个事务（两个连接）。内部事务回滚，不影响外部；外部回滚，也不影响内部（只要内部已提交）。
NESTED（嵌套事务）：同一个事务（同一个连接），利用数据库的 Savepoint 机制。外层回滚，内层一定回滚；内层回滚，外层可以捕获异常选择不回滚。

**第二部分：隔离级别与并发问题 (MySQL)**
**事务并发问题**  
脏读：读到了未提交的数据。
不可重复读：侧重于修改 (Update/Delete)。同一事务内两次读同一行，内容变了。
幻读：侧重于新增 (Insert) 或 范围统计。同一事务内两次读范围，记录条数变了。

隔离级别本质上是在并发性能和数据一致性之间做权
- READ_COMMITTED (RC)：解决脏读.每次 SQL 执行时生成新的 Read View（快照）。
- REPEATABLE_READ (RR)：解决脏读、不可重复读.MySQL中通过 Next-Key Lock 解决了大部分幻读。事务启动时（或第一次快照读时）生成 Read View，整个事务期间复用这个快照。
- SERIALIZABLE：最高隔离级别，完全避免并发问题但性能最差

隔离级别：为什么大厂（如阿里）倾向于用 RC 而不是默认的 RR？

MySQL 默认：RR (Repeatable Read)。
    优点：配合 Next-Key Lock 解决了幻读，数据一致性高。
    缺点：间隙锁 (Gap Lock) 会导致高并发下的死锁概率变高，且并发度不如 RC。
大厂偏好：RC (Read Committed)。
    理由 1：减少死锁。RC 级别下几乎没有间隙锁（Gap Lock），只有行锁。
    理由 2：性能更好。锁的粒度更细。
    理由 3：大部分业务并不怕“不可重复读”，应用层（CAS/乐观锁）可以解决。

Q: 幻读、MVCC 与 Next-Key Lock
RR主要解决的是“不可重复读”，但在标准SQL定义中，RR是无法解决“幻读”的。但是，MySQL的InnoDB引擎在RR级别下，通过MVCC和锁机制，在很大程度上解决了幻读：
      InnoDB的MVCC通过**一致性读视图**完美解决了**快照读**的幻读问题。在同一个事务里，无论你进行多少次快照读，结果都是一致的。
      但是，当你混入**当前读**时，情况就变了。当前读**不受**该事务快照的约束，它必须去读取最新的数据并加锁.

      对于普通查询（快照读），通过 MVCC（一致性视图）保证读到的是事务开始时的数据，读取历史版本，天然无幻读。
      对于加锁查询（当前读），通过 Next-Key Lock（临键锁），不仅锁住行，还锁住间隙，物理上阻止了其他事务的插入，也解决了幻读。

      补充特例：除非在同一个事务中，先做快照读，再做当前读（如update别人刚插入的数据），再做快照读，才可能因为版本号更新而看到“幻影行”。

      为了解决当前读的幻读问题，InnoDB 引入了 Next-Key Lock。它不仅锁住记录本身，还锁住记录之间的‘空隙’，防止其他事务在这个范围内插入新数据。
      Next-Key Lock(]=Gap Lock() + Record Lock[]。
### 3. @Transactional(readOnly = true)  
主要从以下三个层面进行优化：
Level 1 (Spring): 设置 JDBC Connection.setReadOnly(true) -> 触发读写分离。
Level 2 (ORM/Hibernate): FlushMode.MANUAL -> 0 脏检查，0 Session中的快照。
Level 3 (MySQL InnoDB): 0 TRX_ID -> 减少锁竞争和Undo Log的记录。

**脏检查（Dirty Checking）：**  
在标准的读写事务中，Hibernate会将从数据库加载的实体（Entity）放入一级缓存（Session Cache）中，并保留一个原始状态的快照。   
当事务提交时，Hibernate必须遍历缓存中的所有实体，将它们的当前状态与快照进行比较，以找出被修改过的“脏”数据，然后生成UPDATE语句同步到数据库。这个过程称为脏检查。

1. MySQL InnoDB 的底层优化
   不分配事务ID (TRX_ID)：
   普通读写事务启动时，InnoDB 会申请一个全局递增的 TRX_ID。这需要访问全局锁（trx_sys mutex），在高并发下有竞争。
   当事务被标记为只读时，InnoDB 不会分配 具体的 TRX_ID，而是用一个极小的开销来标记。这直接减少了内部锁的竞争，提升了并发吞吐量。
  
2. MyBatis vs Hibernate 的区别
   “我们项目用的 MyBatis，加这个注解有用吗？”
   回答：
   有用，但机制不同。
   对于 MyBatis，没有脏检查这个概念，所以无法享受 ORM层面的内存优化。
   但是，MyBatis 依然会调用 connection.setReadOnly(true)。
   这意味着 读写分离路由 依然生效（通过中间件如 MyCat 或 Sharding-JDBC）。数据库层面的优化（如 MySQL 不分配 TRX_ID）依然生效。
   结论：用 MyBatis 也要加，主要是为了主从路由和DB层减负。
   
   Q1: 如果我在 readOnly=true 的事务里写数据，会报错吗？
   看情况。
   报错：如果是 MySQL 5.6+ 且驱动正确传递了标志，数据库层面会因为是“只读事务”而拒绝写入，报 SQLException。
   报错：如果是 Hibernate，设置了 FlushMode 为 MANUAL，显式调用 save 可能报错或被忽略。
   不报错但无效：某些老版本配置下，可能仅仅是不执行 commit，导致写入丢失。
   不报错且写入成功：如果数据库驱动忽略了 setReadOnly(true)（比如某些Oracle旧驱动），且没有ORM拦截，数据可能会被写入主库！
   核心逻辑：readOnly 本质上是一个 Hint（提示），最终行为取决于数据库驱动和数据库的配合。
   
   Q2: 外层是读写事务，内层调用了一个 readOnly=true 的方法，内层还会只读吗？
   不会。
   根据默认传播行为 REQUIRED，内层方法会加入外层的事务。
   原则：事务的属性（读写性、隔离级别等）通常由开启事务的那个方法（外层）决定。内层的 readOnly=true 配置会被忽略。
### 4. Redis - RocketMQ - 相对扣减实现一致性：
#### **第一阶段：同步下单（抗并发）**
1.  **用户点击下单**：前端发起请求。
2.  **Redis 预扣减（原子性）**：
    *   执行 Lua 脚本或 `DECR` 操作。
    *   **检查点**：如果返回值 `< 0`，直接返回“已售罄”（挡住 99% 流量，保护数据库）。
3.  **RocketMQ 发送半消息（Half Message）**：
    *   订单服务向 MQ 发送“准备扣减库存”的消息。
    *   此时消息对消费者（库存服务）**不可见**。
4.  **执行本地事务（创建订单）**：
    *   订单服务在本地数据库执行 `INSERT INTO orders ...`。
    *   同时记录一条“下单时间”用于后续超时检查。
5.  **提交 MQ 消息（Commit）**：
    *   如果本地事务成功：向 MQ 发送 `Commit`，消息变为**可见**，投递给库存服务。
    *   如果本地事务失败：向 MQ 发送 `Rollback`，并执行 **Redis 回滚**（把刚才预扣的加回去）。
6.  **发送延时消息（兜底锁单）**：
    *   发送一条 30 分钟延迟的消息到 MQ，用于处理“拍而不买”。
7.  **返回结果**：立即返回订单号给用户（此时用户看到“下单成功，待支付”）。

#### **第二阶段：异步扣减（保一致）**
8.  **库存服务消费消息**：
    *   收到“扣减库存”消息。
9.  **幂等性检查**：
    *   查询去重表（或通过订单号唯一索引），判断该订单是否已经扣过库存。
    *   如果处理过，直接 ACK（确认），不再执行。
10. **数据库扣减**：
    *   执行 SQL：UPDATE stock = stock - num WHERE id = xxx AND stock >= num
    *   如果成功：流程结束。
    *   如果失败（并发冲突）：重试几次；若仍失败（真没货了），发送“扣减失败”消息触发订单取消流程。

#### **第三阶段：支付与超时（闭环）**
11. **用户支付**：更新订单状态为“已支付”。
12. **超时未支付（延时队列触发）**：
    *   30分钟后，消费者收到延时消息。
    *   检查订单状态：
        *   若已支付：忽略。
        *   若未支付：**关单 + 回滚数据库库存 + 回滚 Redis 库存**。
---

|阶段| 核心组件          | 解决的核心问题 (编号) |	
|--|---------------|--------------|
|入口| Nginx/Gateway |#4 (防刷)|	
|缓存| 	Redis        |	#9 (热点), #7 (宕机), #1 (预扣)|	
|下单| Order Service + MQ |#2 (异常回滚), #5 (回查), #8 (速回)	|
|扣减| Stock Service + DB | #10 (限流), #3 (幂等), #1 (兜底), #6 (空欢喜)	 |
|结算| Payment + DelayMQ | #8 (解耦), #4 (释放)	 |

#### 4. 恶意锁单 / 拍而不买 (Malicious Locking)
*   **现象**：竞争对手写脚本，瞬间把 Redis 库存抢光（预扣减成功），但就是不付款。导致真实用户买不到，半小时后库存回滚也晚了。
*   **解决方案**：
    *   **风控层**：在请求进来前（Nginx/网关），限制单用户 ID/IP 的访问频率。
    *   **应用层**：限制单用户未支付订单数量（如每人最多只能有1个待支付订单）。
    *   **数据层**：下单成功的同时，发送一条延时消息给 RocketMQ（比如 delayLevel 设置为 30分钟）。超时自动释放库存。

#### 9. 热点 Key 问题 (Hot Key)
场景：Redis 预扣减时，全网都在抢同一个 goods_id_1001，Redis 单节点被打爆（CPU 100%）。
解法：库存分片 (Sharding)+Redis集群

#### 7. 极端灾难：Redis 宕机
*   **现象**：Redis 挂了，所有请求直接打到数据库，或者所有请求都失败。
*   **解决方案**：
    *   **降级**：检测到 Redis 异常，自动切换为“数据库直连模式”（但要限流，比如只允许 100 QPS 透过），或者直接返回“系统繁忙”。
    *   **预热与恢复**：重启后，必须通过脚本将数据库库存加载回 Redis 才能重新开放服务。
    
#### 1. 超卖问题 (Overselling)
*   **现象**：库存10个，卖出了12个订单。
*   **第一道防线（Redis）**：使用 `DECR` 或 Lua 脚本，保证内存扣减是原子的，减到负数直接拦截。
*   **第二道防线（数据库）**：‘相对扣减’策略代替传统的版本号机制，避免了大量 CAS 失败导致的 CPU 空转和 DB 压力。”
    *   即使 Redis 漏了（比如宕机数据恢复不一致），数据库绝对不会把库存扣成负数。

#### 2. 缓存与数据库不一致 (Data Consistency)
*   **现象**：Redis 扣了（10->9），但后面代码报错了，数据库没扣，订单没建。导致“少卖”。
*   **解决方案**：
    *   在代码的 `catch` 异常块中，或者 MQ 事务消息的 `Rollback` 逻辑中，必须显式调用 Redis 的 `INCR` 把库存加回去。
    *   *兜底*：可以通过定时任务比对 Redis 和 DB 的库存（一般不需要，太重了，靠回滚机制足够）。

#### 5.  RocketMQ 事务消息的“回查”机制 (CheckBack)
* 如果订单服务在执行完本地事务（写完库）后，还没来得及发 Commit 就宕机了，怎么办？
* RocketMQ 的回查机制。
* MQ Server 发现半消息长时间没有确认，会主动回调订单服务的一个接口（checkLocalTransaction）。
* 订单服务去查数据库：“哎？这个订单号到底建成功没？”
* 这是保证最终一致性的最后一道保险

#### 8. 前端返回订单号给客户后,客户支付成功了,在等待第二阶段数据库真正扣减库存时会不会需要较长时间导致体验不好
*   1. 时序分析：人比机器慢得多
*   2. 架构解耦：支付成功并不依赖库存扣减
       即使遇到双11这种极端流量，MQ 发生了消息积压（比如延迟了 5 分钟才扣减数据库），用户依然会秒级看到“支付成功”。
       为什么？因为支付服务和库存服务是完全解耦的：
       支付服务逻辑： 接收微信/支付宝的回调 -> 校验金额 -> 修改订单状态 -> 直接告诉前端
       注意：这一步根本不需要去问库存服务“数据库扣完了吗？”
       库存服务逻辑（后台慢慢跑）：这纯粹是数据最终一致性的问题，不影响前端展示。
       只要 Redis 预扣减（门票机）放行了，我们就认为这个用户是有货的。 至于数据库什么时候真正扣掉，那是后台记账的事，不需要让用户在收银台罚站等待。

#### 10. 消费端限流 (Flow Control)
场景：MQ 堆积了 100 万条消息，消费者启动后拼命消费，瞬间把数据库打挂。
解法：令牌桶限流。

#### 3. 重复消费 (Duplicate Consumption)
*   **现象**：MQ 因为网络抖动重发了消息，导致库存服务把同一个订单扣了两次库存。
*   **解决方案（幂等性）**：
    *   **唯一索引法**：建立一张表 `stock_deduct_log`，字段 `order_id` 设为唯一索引。
    *   先 `INSERT` 只有成功了才执行 `UPDATE stock`。
    *   或者利用业务逻辑判断：`UPDATE ... WHERE order_id_processed IS NULL`（如果把已处理ID存入JSON字段）。

#### 6. 用户体验问题 (空欢喜)
*   **现象**：Redis 扣成功了，提示用户成功。结果 MQ 异步消费时，数据库因为某种极端原因（如磁盘坏了）扣减失败，被迫回滚取消订单。
*   **解决方案**：
    *   这是异步架构无法完全避免的牺牲（CAP理论）。
    *   但在工程上，通过**Redis预扣减**已经过滤了 99.9% 的无效流量，真正进入数据库扣减的请求量很小且都是“有票”的。
    *   除非数据库宕机，否则乐观锁重试机制基本能保证成功。
    *   “这是一个权衡。相比于阻塞用户等待数据库结果，我们选择先响应成功。极端失败情况通过短信/退款补偿，这是业务可接受的。”

#### 11. “为什么不用 Kafka？为什么不用 RabbitMQ？”
Kafka：
设计初衷是日志处理，追求极致吞吐，但不支持事务消息（虽然新版支持但较复杂且性能折损），且在消息精准投递和延迟消息支持上不如 RocketMQ。
RabbitMQ：
延时消息需要插件支持，且没有原生的“事务消息回查”机制，做分布式事务需要自己造轮子（比如本地消息表）。
RocketMQ：
原生支持 事务消息 (Transactional Message) 和 18个级别的延时消息，完美契合电商的“下单”和“超时取消”场景。

#### 12. 为什么搞这么复杂（MQ+Redis+DB），直接用 Redis 分布式锁 `lock.lock()` 锁住商品ID不就行了？
1.  **性能极差**：分布式锁把并发变成了串行。预扣减方案是并行的。
2.  **死锁风险**：锁服务如果不稳定，容易导致整个商品无法售卖。

### 5. 读写分离一致性保障机制：提升系统的并发读能力
1. 实现：ThreadLocal (Context) + AOP (注解) + AbstractRoutingDataSource (路由)。
核心逻辑：利用 Spring 的 AbstractRoutingDataSource 作为动态数据源路由。
AOP拦截：自定义了一个 @DataSource 注解，通过 AOP 拦截 Service 方法。如果注解标记为 Slave，就把 Slave 的 Key 放入当前线程的 ThreadLocal 中。
动态切换：当获取数据库连接时，路由数据源会从 ThreadLocal 取出 Key，自动返回从库的连接；默认或者标记为 Master 则返回主库连接。
资源回收：一定要在 AOP 的 finally 块中调用 ThreadLocal.remove()，防止线程复用导致的数据源错乱和内存泄漏。

2. 解决主从延迟问题：
   强制读主：对于注册、支付成功后的回调查询等核心实时业务，我们强制走主库（加上 @DataSource("master")）。
   智能路由（进阶）：对于一些写后即读的场景，我们会利用 Redis 记录一个短期 Key（如 user_updated），读请求进来时如果发现有这个 Key，就临时切到主库，确保数据一致性。”
   延迟双删/缓存更新: 在写入主库后，先淘汰缓存，等待一个主从延迟的时间（如1秒），再次淘汰缓存，确保缓存中的脏数据被清除。  
   半同步复制: 配置MySQL等数据库的主从复制为半同步模式，确保事务日志至少被一个从库接收后，主库才向客户端返回成功。

3. 事务与读写分离的冲突:“如果我在一个 @Transactional 的方法里，先做了写操作，又做了读操作，读操作会走从库吗？”
> “不会，都会走主库。
> 因为 Spring 的事务管理器为了保证 ACID（特别是可重复读隔离级别），要求整个事务内的所有操作必须复用同一个数据库连接。
> 一旦事务开启，连接就已经绑定为主库了，所以即使中间代码尝试切换 ThreadLocal，也不会生效。”

4. 注册同步:主从复制机制（如 MySQL 的 Binlog）自动同步
Binlog：MySQL Server 层生成的逻辑日志，采用追加写模式，记录了所有的 DDL 和 DML 语句。它有两个核心作用：主从复制 和 数据恢复。
**主从复制 (Replication)**
    *   Master 开启 Binlog，Slave 启动一个 I/O 线程读取 Master 的 Binlog，写入自己的 Relay Log（中继日志），然后重放执行，实现数据同步。
**数据恢复 (Data Recovery)**：
    *   用于**时间点恢复（Point-in-Time Recovery）**。
    *   例子：下午 2:00 误删库了。管理员可以利用昨天凌晨的全量备份 + 昨天凌晨到今天 2:00 的 Binlog，把数据重放到误删前的那一刻。

5. 日志格式 (Format)：
   虽然 Statement 格式记录 SQL 原文比较省空间，但它有隐患（比如 UUID() 或 NOW() 函数在主从库执行结果不一样）。
   而 ROW 格式记录的是每一行数据修改后的具体值，虽然日志大一点，但能保证主从数据绝对一致。 
   Mixed 是MySQL 自己判断。普通操作用 Statement，有函数风险时用 Row。理论上折中，但生产环境为了稳，**一般直接设为 Row**。 |
6. Binlog vs Redo Log ：
   Binlog 是 Server 层的逻辑日志，主要管归档和同步，写满了就切新文件，一直保留。
   Redo Log 是 InnoDB 引擎特有的物理日志，主要管 Crash-safe (崩溃恢复)，它是循环写的，固定空间。
   一句话总结：Redo Log 是用来‘保命’防止断电丢数据的，Binlog 是用来‘修旧账’做同步和恢复的。
只有 Binlog 能做数据恢复吗？Redo Log 不行吗？
    A: “Redo Log 不行。因为它大小固定，写满了会覆盖旧数据，只有最近的数据。要恢复一个月前的数据，必须靠 Binlog（因为它是一直追加保存的历史档案）。”
7. 数据一致性保障 (两阶段提交)：
   为了防止写完 Redo Log 还没写 Binlog 机器就挂了（导致主从不一致），MySQL 内部采用了两阶段提交：
   先写 Redo Log (Prepare) -> 再写 Binlog -> 最后提交 Redo Log (Commit)。
   只要 Binlog 写成功了，Redo Log 就算没 Commit，恢复时也会承认这条事务；如果 Binlog 没写，Redo Log 即使 Prepare 了也会回滚。

8. 写入机制：`sync_binlog`
控制 Binlog 什么时候刷入磁盘，涉及性能与安全的权衡。
*   `sync_binlog = 0`：MySQL 把日志交给 OS 的缓存就不管了。性能最好，但如果机器断电，会**丢失**最近的 Binlog。
*   `sync_binlog = 1`（推荐）：每次提交事务都**强制 fsync 写入磁盘**。最安全，但对 IO 有影响。
    *   *双1配置*：通常生产环境会将 `sync_binlog=1` 和 `innodb_flush_log_at_trx_commit=1`（Redo Log策略）同时开启，称为“双1设置”，保证数据零丢失。

### 6. 总结在分布式系统中常见的几种数据一致性保障策略
1. 强一致性策略   
   2PC / 3PC：2PC 是阻塞的（同步等待），3PC 也没彻底解决网络分区问题。
   现状：工业界很少直接用原生 2PC，基本都用改进版（如 Seata）。

   Seata AT 模式：2PC 的改进版
   机制：自动生成 Undo Log（反向 SQL）。提交前记录“修改前”和“修改后”的数据快照。
   优点：零侵入，开发者像写本地事务一样写代码。
   代价：存在全局锁（Global Lock），高并发下有性能瓶颈（因为要等待锁释放才能防止脏写）。

2. 最终一致性策略   
   MQ 事务消息（RocketMQ）：
   核心：半消息 (Half Message) + 回查机制。确保“本地事务执行”和“消息发送”是原子的。
   适用：解耦业务，如下单成功发积分、发券。

   本地消息表（通用方案）：
   痛点：公司用的是 Kafka/RabbitMQ，不支持原生的事务消息怎么办？
   解法：在业务库里建一张 message 表。在同一个本地事务里，执行业务 + 插入消息记录。然后由一个定时任务轮询这张表去发 MQ。
   评价：最通用的方案，但轮询数据库有 IO 压力。

   TCC (Try-Confirm-Cancel)：
   核心：资源预留。Try 阶段先冻结钱/库存；Confirm 阶段真正扣减。
   适用：核心资金/交易链路。因为锁粒度完全由业务控制，性能比 Seata AT 好。不依赖底层数据库的事务支持。
   代价：代码量翻三倍，开发成本极高。

3. 兜底与辅助策略
   a. 定时任务补偿机制    
   优点: 实现简单，健壮可靠，是最终一致性方案的完美兜底。  
   缺点: 非实时，数据不一致会存在一个时间窗口，可能对数据库造成周期性压力。  

   b. 数据校验机制  
   事前校验: 如使用乐观锁。  
   事后校验: 定期或在业务流程结束后，比对源系统和目标系统的数据，生成对账单，发现差异后进行人工或自动修复。

   c.重试机制 (Retry Mechanism)  
   核心思想:当一个操作失败时，不立即判定为最终失败，而是等待一小段时间后再次尝试。通常会结合退避策略（如指数退避，即每次重试的等待时间逐渐变长）和重试次数限制。  
   关键前提：幂等性 (Idempotency)
   保证幂等性的方法：为每次请求生成唯一的请求ID，在服务端记录已处理的请求ID，后续重复请求直接返回成功结果。   
   实现方式:  
   代码层面: 使用 AOP 框架（如 Spring Retry）或第三方库（如 Guava Retrying）为方法添加重试逻辑。  
   消息队列: MQ 的消费者消费失败后，MQ 会自动进行重试投递。
   任务调度: 定时任务补偿本身就是一种宏观的重试机制。

4. 架构层面的保障策略
a. 读写分离一致性保障

b. 缓存一致性保障
   Cache Aside Pattern (旁路缓存):
   最常用模式。读操作先读缓存，缓存未命中则读数据库，再将数据写入缓存。写操作先更新数据库，然后删除缓存。  
   Read/Write Through (读/写穿透):   
   由缓存服务自身负责与数据库的同步，应用层只与缓存交互。  
   Write Back (回写):  
   写操作只更新缓存，缓存定期批量将数据刷回数据库。  
   订阅Binlog:
   解决缓存一致性的终极方案。代码里只管写库，由 Canal 监听 Binlog 异步更新 Redis，彻底解耦。

Q: 为什么 Seata AT 适合内部系统，TCC 适合核心交易？（AT vs TCC）
Seata AT (自动挡)：
底层依赖数据库的行锁 + Seata 的全局锁。
在二阶段提交前，一直持有数据库锁。如果并发高，大量事务等待锁，性能会急剧下降。
TCC (手动挡)：
不依赖数据库锁。Try 阶段只是把状态改为“冻结”，Confirm 阶段把状态改为“扣除”。
数据库事务在 Try 完就提交了，锁持有的时间极短。
所以 TCC 并发性能远高于 AT，但写起来太累。

### 7. 深度分页问题
“针对深度分页，我通常根据业务场景在‘覆盖索引优化’和‘游标分页’中二选一：”
1. 场景选择
   后台管理系统（需页码跳转）：使用覆盖索引+子查询（Deferred Join）。
   逻辑：先用覆盖索引查出目标页的 10 个 ID，再回表查完整数据。
   移动端/Feeds流（无限滚动）：使用游标分页（Cursor Pagination）。
   逻辑：客户端记录上一页最后一条数据的“游标”（如 time + id），下次查询直接 WHERE (time, id) > (last_time, last_id)。
2. 游标分页的优缺点
   两大优势：
   性能极高：利用索引定位，无论翻到多少页，复杂度永远是 O(1)，没有全表扫描。
   数据一致性强：天然避免了传统分页在并发写入时产生的“数据漂移”（数据重复出现或漏掉）问题。
   三大限制：
   不支持跳页：只能“下一页”，不能直接跳到第 50 页（因为没有页码概念）。
   排序键必须唯一：为了防止漏数据，排序字段不能重复。如果有重复（如 create_time），必须加上“Tie-breaker”（如主键 ID）组合成唯一游标。
   实现复杂：
   前端需维护游标。
   “上一页”难做：后端需要反转 SQL 的排序规则（把 ASC 改 DESC，> 改 <），查出数据后再由代码反转回正序。

“对于必须保留‘页码跳转’功能的深度分页，我一般使用**‘延迟关联’**（Deferred Join）的方式来优化。
传统的 LIMIT 分页之所以慢，是因为数据库在执行比如 LIMIT 100万, 10 时，它会先把前 100 万零 10 条数据的完整行内容都读取出来（这涉及大量的回表和磁盘 I/O），然后把前 100 万条丢掉，只留最后 10 条，这非常浪费。
“针对深度分页，如果业务必须支持页码跳转，无法使用游标法，那我就采用延迟关联。
通过子查询先在索引树上快速定位到目标页的 主键 ID（利用覆盖索引避免回表），然后再拿这几个 ID 去关联主表获取完整数据。这样能最大限度地减少无效数据的回表 I/O 开销。”

Q: 为什么只查 ID 就快？
A: 因为 ID 通常在辅助索引的叶子节点里就有（或者就是聚簇索引的键），数据库引擎不需要去查找具体的数据行文件（Data Page），这叫覆盖索引，完全在索引结构中就能完成操作。
Q: 这种方案有什么缺点？
A: SQL 语句会变复杂，写起来不直观；而且虽然减少了回表，但扫描索引的开销（Scan 100W index nodes）依然存在，所以如果数据量达到几千万级，还是建议限制最大页码（比如只能翻到 100 页）或者换用搜索引擎（ES）。

### 8. SQL注入防护优化方案
a. 使用MyBatis的@Param注解进行参数绑定：  
用 @Param 给参数命名后，MyBatis 就能更清晰地知道哪个值对应SQL中的哪个占位符（例如 # {username}），方便后续的预编译机制。   

b. 使用#{}占位符而不是\${}字符串拼接   
#{} 会告诉 MyBatis，这里是一个参数，而不是SQL代码的一部分。MyBatis 在执行SQL之前，会用预编译语句来处理它。PreparedStatement 会将SQL语句和参数分开，先编译SQL模板，然后再安全地将参数值填充进去。这样，无论参数值是什么（即使包含了恶意SQL代码），它都只会被当作数据来处理，而不会改变SQL语句本身的结构。
${} 则是直接将变量的值 原封不动地拼接到SQL字符串中。如果变量的值包含恶意SQL代码，这些代码就会直接成为SQL语句的一部分，从而引发SQL注入。

c. 使用MyBatis Plus提供的LambdaQueryWrapper，它会自动处理参数绑定   

d. 创建了SqlInjectionUtil工具类，用于检测和过滤潜在的SQL注入字符   
检查用户输入字符串中是否含有常见的SQL注入攻击特征字符（如单引号 '、双引号 "、分号 ;、--、OR、AND 等），或者对这些字符进行转义、过滤。

e. 在Service层和Controller层都增加了参数安全性检查

### 9. 接口幂等性设计
“为了防止用户重复点击或网络重试导致的数据重复，我们设计了一套基于注解 + Redis 的通用幂等框架。
架构设计：定义了 @Idempotent 注解，配合 AOP 切面进行统一拦截。
当请求进来时，AOP 根据策略生成一个唯一的 幂等 Key（通常是 User ID + 方法名 + 请求参数的 MD5，或者前端传来的唯一 Request ID）。
利用 Redis 的 SETNX 命令尝试占锁，并设置较短的过期时间（如 3-5 秒）。
如果 SETNX 返回成功，说明是首次请求，放行执行业务。
如果返回失败，说明是重复请求，直接抛出异常或返回‘请勿重复提交’。
异常处理：如果业务执行报错，会在 finally 块或异常捕获中删除这个 Key，允许用户重试。”

1. 幂等 Key 的两种生成策
策略 A：参数指纹（Params Digest） —— 防手抖
做法：Key = MD5(User_ID + URL + JSON_Params)。
场景：防止用户手快，1秒内点两次提交。
缺点：如果用户改了一个标点符号再提交，MD5 变了，拦截不住。

策略 B：Token 机制（Request ID） —— 防重放/网络重试
做法：
客户端先调用 getToken 接口，后端生成一个 UUID 存入 Redis，并返回给前端。
前端提交表单时，把这个 Token 带在 Header 里。
后端 AOP 拦截，执行 DEL Token（或 Lua 脚本检查并删除）。
优势：这是最标准的幂等方案。无论参数怎么变，只要 Token 是用过的，就不让过。

2. Redis SETNX时先 GET 判断是否存在，再 SET？
   错。高并发下有并发安全问题。
   正确：
   直接使用 SET key value NX EX 5（Redis 2.6.12+ 支持原子命令）。
   或者使用 Lua 脚本 保证“检查+删除”的原子性（针对 Token 模式）。

3. 业务执行完了，Key 要不要删？
   这是一个两难的选择，分场景回答：
   场景一：防重复提交（防手抖）
   策略：不删 Key，让它自然过期（比如过期时间设为 2 秒）。
   理由：如果你业务执行完（耗时 200ms）立刻删 Key，用户在第 300ms 又点了一次，Redis 里没 Key 了，又放进来了。所以必须让 Key 存活一段时间。
   场景二：严格幂等（涉及资金）
   策略：Key 的有效期设得很长（甚至永久，比如存数据库去重表）。
   理由：只要这笔交易处理过了，以后永远不能再处理第二次。
------------------------------------------------------------
## 优惠券管理   LamdaQueryWrapper | MP架构 | 接口限流 | Sentinel | Resilience4j
提供优惠券的发放、使用规则配置、有效期管理等功能，增强营销活动灵活性。

### 1. QueryWrapper vs LambdaQueryWrapper
使用 LambdaQueryWrapper，核心原因有三点：
拒绝‘魔鬼字符串’（Magic Strings）：
    QueryWrapper 需要手写数据库字段名（如 "user_name"），一旦手抖拼错，编译期不报错，运行时才会炸。
    而 LambdaQueryWrapper 使用方法引用（User::getUserName），拼写错误直接编译通不过，将风险拦截在开发阶段。
重构零风险：
    如果我修改了实体类的属性名（比如 userName 改为 name），IDE 会自动更新所有 User::getUserName 的引用。但如果是字符串 "user_name"，IDE 无法感知，重构后系统到处报错。
屏蔽数据库差异：
    不需要关心数据库列名是 user_name 还是 username，也不用管驼峰转下划线的规则，MP 会根据实体类注解自动解析映射

Q: 你写 User::getUserName，MP 是怎么知道它对应数据库的 user_name 字段的？
    “这利用了 Java 8 的 SerializedLambda 机制和 Java 反射。
    函数式接口：LambdaQueryWrapper 接收的参数类型是 SFunction<T, R>，这是一个继承了 Serializable 的函数式接口。
    序列化解析：当我们将方法引用（User::getUserName）传进去时，MP 底层会通过序列化手段，解析出这个 Lambda 表达式包含的元数据（SerializedLambda 对象）。
    提取方法名：从元数据中拿到方法名 "getUserName"。
    推断属性名：通过去除 get/is 前缀并首字母小写，推断出属性名 "userName"。
    映射列名：最后结合实体类上的 @TableField 注解或全局配置（驼峰转下划线），找到对应的数据库列名 "user_name"。”

虽然 Lambda 好，但有一种情况它做不到：
    场景：前端传了一个排序字段过来，是字符串形式（例如 orderBy = "create_time"）。
    此时你无法使用 User::getCreateTime（因为字段是动态的字符串）。
    解决：只能降级使用 QueryWrapper.orderBy(true, isAsc, "create_time")。
### 2. MP架构
一、MyBatis-Plus 核心架构
底层基石：完全基于 MyBatis Core，无缝兼容所有 MyBatis 原生特性。
三大核心支柱：
BaseMapper：利用泛型提供通用的 CRUD 接口，解决了 90% 的基础 SQL 编写工作。
Wrapper：利用 Lambda 语法提供类型安全的条件构造器，解决了 XML 中动态 SQL 拼接繁琐的问题。
ServiceImpl：封装了常用的业务层链式操作（如批量插入），作为业务逻辑的标准实现。
两套支撑体系：
插件体系：基于责任链模式的拦截器，实现了分页、多租户、乐观锁等功能。
注解体系：实现了实体类与数据库表的元数据映射。”

二、更多为了方便开发而做的优秀设计
1. 主键生成策略 (@TableId(type = IdType.XXX))  
2. 自动填充 (MetaObjectHandler)  
3. 逻辑删除 (@TableLogic)  
4. 代码生成器 (AutoGenerator):开发者只需进行简单的配置（如数据库连接、要生成的表名等），就可以一键生成整个模块的Entity, Mapper, Service, Controller 等模板代码

Q: “BaseMapper 里的 insert 方法，连 XML 都没有，MP 到底怎么把 SQL 给数据库的？”
核心原理：SQL 注入器 (ISqlInjector)
MP 并没有在运行时去动态拼 SQL（那样太慢），而是在 Spring 启动阶段就完成了工作：
扫描：MP 扫描所有继承了 BaseMapper 的接口。
注入：通过 ISqlInjector，根据实体类的注解（@TableName, @TableId），自动生成标准的 CRUD SQL 语句（如 INSERT INTO table ...）。
注册：将这些生成的 SQL 语句封装成 MyBatis 的 MappedStatement 对象，直接注入到 MyBatis 的 Configuration 中。
结论：这就好比 MP 在启动时帮我们在内存里偷偷写好了 XML 文件。所以在运行时，调用 baseMapper.insert 和调用原生 MyBatis 的性能是完全一样的，没有额外反射开销。”
### 3. 接口限流
“限流的核心目的是保护系统不被压垮，确保在高并发下系统的可用性（避免雪崩）。
关于算法，我主要掌握四种，它们是逐步进化的：
固定窗口（计数器）：最简单，但在窗口切换边缘有**‘临界突刺’**问题（比如第59秒和61秒各来了100请求，2秒内其实是200，超过了阈值）。
滑动窗口：把大窗口切成多个小格子，随着时间平滑移动，解决了临界问题，但实现稍微复杂一点（Sentinel 底层就用了这个）。
漏桶算法：像漏斗一样，流出速率恒定。适合整流（Traffic Shaping），比如保护下游脆弱的数据库。缺点是无法应对突发流量。
令牌桶算法：以恒定速率往桶里放令牌，请求拿令牌。如果有突发请求，只要桶里有存货，就能瞬间处理。它兼具平滑和应对突发的能力，是目前最主流的算法（如 Guava、Gateway）。”

3. 在Spring Boot项目中，我们通常可以选择以下几种方式实现限流：   
a. Guava的RateLimiter：单机限流，令牌桶。
b. Redis + Lua脚本：分布式限流，原子性。
c. 自定义注解 + AOP:使用 @Around("@annotation(rateLimit)")
d. 网关层限流：如Spring Cloud Gateway内置限流
e. 第三方组件：如Sentinel  

Q1: “为什么一定要用 Lua 脚本？”

“因为限流通常包含‘读取当前次数’、‘判断是否超限’、‘计数+1’三个步骤。
如果在 Java 里分三步调用 Redis，会有并发竞争问题（两个线程同时读到 9，都加 1，结果变成 10，实际过了 11 个请求）。
Lua 脚本能保证这三步操作在 Redis 端是原子执行的，中间不会被插入其他命令。”

Q2: “Spring Cloud Gateway 已经有限流了，为什么还要用 Sentinel？”

| 维度     |Gateway (网关限流)|Sentinel (应用层限流)|Hystrix (已过时)|
|--------|--|--|--|			
| 位置	    |入口层，保护整个集群|	业务层，精细化保护具体接口	|业务层|
| 粒度     |	粗粒度 (IP, Route)	|细粒度 (参数, 调用链路, 热点Key)	|细粒度|
| 功能     |	主要是限流|	限流 + 熔断 + 降级 + 系统自适应|	主要是熔断|
| 流控效果	  |快速失败	|快速失败 + 预热 (WarmUp) + 排队等待	|快速失败|
| 运维	    |改配置需重启 (通常)|	控制台动态下发规则 (无需重启)	|无控制台或较弱|

Q3: 如果 Redis 挂了，基于 Redis 的限流会把业务搞挂吗？
A: 这是一个系统设计问题。通常有两种策略：
强依赖（Fail-fast）：Redis 挂了，所有请求全部拒绝。适合对安全性要求极高的系统。
弱依赖（Fail-open，推荐）：捕获 Redis 连接异常，自动降级为不限流（或者降级为 Guava 单机限流），优先保证业务可用性。

Q4: 这里的 Key 一般怎么设计？
A: 看业务需求：
防刷：Limit:API:UserID (限制单个用户) 或 Limit:API:IP (限制单个IP)。
系统保护：Limit:API:Total (限制该接口的总并发量)。
### 4. Sentinel
“在微服务架构中，Sentinel 是我们的流量防卫兵。相比于 Hystrix 侧重于隔离和熔断，Sentinel 提供了更全面的流量治理能力。它的核心功能有三点：
流量控制 (Flow Control)：
    这是最基础的能力。它不仅支持基于 QPS 和 并发线程数 的限流，还支持调用关系限流（如 A 调 B 限流，C 调 B 不限流）和流量整形（如预热、匀速排队）。
熔断降级 (Circuit Breaking)：
    当检测到下游服务响应变慢（慢调用比例）或报错增多（异常比例）时，Sentinel 会自动熔断，快速拒绝请求，防止雪崩效应。它基于状态机实现（Closed -> Open -> Half-Open）。
系统自适应保护 (System Adaptive Protection)：
    这是 Sentinel 独有的。它从整体负载（如 CPU 使用率）的角度来保护应用。当 CPU 飙高时，自动拦截入口流量，给系统喘息机会。”

流控模式：   
a. 直接模式 (QPS/线程数)：最常见的模式。当资源的 QPS 或并发线程数超过阈值时，直接拒绝。
b. 关联模式：当关联资源的流量达到阈值时，限流当前资源。非常适合保护“写操作”被“读操作”影响的场景。  
例子：updateOrder 和 queryOrder 是两个资源。可以设置一条规则：当 queryOrder 的 QPS 超过 1000 时，限流 updateOrder，从而保证核心的下单流程不受查询流量的冲击。
c. 链路模式：只针对从特定入口（上游微服务或方法）调用当前资源的请求进行限流。   
例子：资源 getOrderDetail 被 ServiceA 和 ServiceB 同时调用。可以设置规则：只限制从 ServiceA 过来的调用链，每秒最多 20 次，而 ServiceB 的调用不受影响。

状态机模型：Sentinel 的熔断器有三个状态：   
Closed：正常状态，所有请求都能通过。  
Open：当满足熔断条件时，状态切换为 Open。在接下来的一个“熔断时长”内，所有对该资源的调用都会被立即拒绝，而不会发起真正的网络请求。   
Half-Open：熔断时长过后，状态切换为 Half-Open。此时，Sentinel 会允许一次请求通过，去“试探”下游服务是否已恢复。 如果这次请求成功，熔断器切换回 Closed 状态。 如果请求依然失败，熔断器重新切换回 Open 状态，并开始新一轮的“熔断时长”。

熔断策略：   
慢调用比例：当资源的平均响应时间 (RT) 超过一个阈值，并且在统计时间窗口内，慢调用的比例达到设定值时，触发熔断。   
异常比例：当资源的异常率（异常数 / 总请求数）超过阈值时，触发熔断。   
异常数：当资源在统计时间窗口内的异常总数超过阈值时，触发熔断。

2. 工作原理与核心架构  
“Sentinel 限流底层的统计算法是什么？和 Guava 的 RateLimiter 有什么区别？”

Sentinel 的统计核心：滑动时间窗口 (Leap Array)
原理：
   Sentinel 并没有使用简单的计数器，而是维护了一个滑动时间窗口。
   默认情况下，它将 1 秒钟划分为 2 个窗口（WindowBucket），每个窗口 500ms。
   每个 Bucket 统计自己的 Pass、Block、Exception 等指标。
   统计时，汇总当前滑动窗口内的所有 Bucket 数据。
优势：相比固定窗口，它解决了临界突刺问题；相比 Guava，它不仅支持 QPS，还支持并发线程数统计。
流量整形算法（流控效果）
   快速失败：默认。超过阈值直接抛异常。
   Warm Up (预热)：基于 令牌桶算法 (Token Bucket) 的变种。令牌发放速率从低到高爬升，适合秒杀冷启动。
   排队等待 (匀速器)：基于 漏桶算法 (Leaky Bucket)。请求必须间隔一定时间才能通过，削峰填谷。

Sentinel 的架构分为两部分：核心库和控制台。
A. 核心库 (sentinel-core)：以 JAR 包形式嵌入到你的应用中，是实际执行规则的地方。   
SphU / SphO：这是 Sentinel 的入口 API。@SentinelResource 注解的背后就是调用了这些 API。   
Slot Chain (插槽链)：这是 Sentinel 工作流的责任链模式实现。每个请求都会经过一个由多个“插槽 (Slot)”组成的链条。每个 Slot 都有特定的职责，例如：   
NodeSelectorSlot：构建资源节点的树状结构。   
ClusterBuilderSlot：构建资源的集群节点，用于聚合该资源在所有入口的统计信息。   
StatisticSlot：核心，负责实时统计资源的各项指标（QPS, RT, 异常数等）。   
FlowSlot / DegradeSlot / SystemSlot：分别负责执行流控、熔断和系统保护规则。
B. 控制台 (sentinel-dashboard)：一个独立的 Spring Boot 应用，提供了一个可视化的界面。   
功能：实时监控应用的各项指标、动态地创建和修改规则。   
通信：应用实例会作为 Sentinel 的客户端，通过心跳机制与控制台保持连接，并上报监控数据。控制台可以通过 API 将新的规则推送给客户端。   
规则持久化：默认情况下，在控制台创建的规则是内存态的，应用重启后会丢失。在生产环境中，必须集成 Nacos、Apollo、Zookeeper 或 Redis 等配置中心，实现规则的持久化。

热点参数限流 (Hot Param)
   场景：双11抢购，某个具体的 skuId=1001（iPhone）被疯狂点击，而其他商品没人买。
   普通限流失效：如果只限流接口，会误伤买其他商品的用户。
   Sentinel 解法：使用 ParamFlowRule。
   它可以统计参数的值。
   规则：当参数第 0 位（skuId）的值为 1001 时，QPS 限制为 100；其他值限制为 1000。
   底层：使用 LRU 缓存统计最近最热的参数值。
集群流控 (Cluster Flow)
   场景：你有 50 台机器，数据库只能抗 5000 QPS。如果单机限流配 100 QPS，总流量就是 5000。
   问题：如果流量分布不均（负载均衡不准），有的机器闲死，有的机器被打挂。
   Sentinel 解法：引入一个 Token Server (令牌服务器)。
   所有应用实例（Token Client）都去 Token Server 申请令牌。
   从而实现全局精确限流（不管几台机器，总共就给 5000 个牌子）。

blockHandler vs fallback 的区别：

blockHandler：只管 Sentinel 自己“惹的祸”（流控、熔断、系统保护等 BlockException）。  
fallback：管业务代码自己出的所有错（所有 Throwable）。   
如果同时配置，当发生 BlockException 时，只有 blockHandler 会生效。

4. Sentinel 的独特优势

 | 特性   |  Sentinel | Hystrix / Resilence4j                                                    |
 |------| --- |--------------------------------------------------------------------------|
 | 隔离策略 | 信号量隔离(默认) | 线程池隔离(Hystrix 默认) /信号量隔离 |
 | 核心优势 | 信号量开销极小，对应用的侵入性 低，RT损耗几乎没有。 | 线程池隔离更彻底，能应对依赖阻塞，但线程切换开销大。 |
 | 规则配置 | 动态配置，通过控制台实时修改，无 需重启。 | Hystrix 需修改代码或配置文件重启。Resilience4j支持动态配置但无原生控制台。|
 | 功能维度 | 非常丰富：流控、熔断、系统保护、 热点参数限流、授权等。 | 主要集中在熔断、降级、隔离。 |
 | 监控   | 强大的实时监控控制台 | Hystrix 有 Dashboard (基于 Turbine 聚合)，Resilience4j 需整合 Prometheus 等。       |
 | 生态   | 与 Spring Cloud Alibaba 生态深度集 成，支持 Nacos/Dubbo 等。 | Hystrix 已停止维护。Resilience4j 是 目前 Spring Cloud 官方推荐。|

### 5. Resilience4j
在 Hystrix 进入维护模式后，Resilience4j 成为了 Spring Cloud 官方推荐的容错库。我们选择它主要基于三点：
轻量级设计：
Hystrix 强依赖 Archaius（配置）和 RxJava，包很大。
Resilience4j 专为 Java 8 设计，只依赖 Vavr（函数式库），非常轻量。
模块化：
它把熔断、限流、重试拆成了独立的 Jar 包。我想用哪个就引哪个，不想用的功能（比如舱壁隔离）完全不占资源。
函数式组合：
它利用装饰器模式，可以像‘套娃’一样把重试、限流、熔断逻辑灵活地组装在业务 Lambda 表达式外面，代码非常优雅。”

**Resilience4j 提供了五个核心的容错模式模块。**
A. Circuit Breaker (熔断器)  
这是最核心的模块，用于防止级联失败。   
它是一个经典的状态机，与 Sentinel 类似，但配置和实现上更轻量。

B. Rate Limiter (限流器)  
用于控制单位时间内的请求访问量。   
它基于一种信号量的变体算法。在一个周期（limitRefreshPeriod）开始时，它会将许可数（limitForPeriod）重置。每次请求都会消耗一个许可。如果许可耗尽，请求线程需要等待下一个周期。

C. Retry (重试)   
可以配置对特定的异常进行重试。当被包装的方法抛出指定的异常时，Retry 模块会捕获它，并根据策略（如等待固定时间、指数退避）重新执行该方法，直到成功或达到最大重试次数。

D. Bulkhead (舱壁隔离)  
用于隔离资源，防止一个服务的故障耗尽整个系统的资源。   
它限制了对某个资源的同时并发调用量。   
基于信号量 (Semaphore Bulkhead)：默认模式。只限制并发数，不创建新线程。 开销极小但隔离不彻底，如果主线程卡死，依然会受影响。
基于线程池 (ThreadPool Bulkhead)：为服务单独开辟线程池。隔离更彻底，但线程切换有损耗。   

E. Time Limiter (时间限制器)  
用于为异步操作设置超时。   
它与 CompletableFuture 配合使用，为异步执行的方法设置一个超时时间。如果方法在规定时间内没有完成，TimeLimiter 会抛出一个 TimeoutException。   

Q1: “它的熔断器底层是怎么实现的？和 Sentinel 一样吗？”
熔断器原理：滑动窗口 (Sliding Window)
   Hystrix 的做法：基于时间窗口（Time-based），比如统计最近 10 秒。
   Resilience4j 的改进：提供了两种滑动窗口类型。
   基于计数 (Count-based)：默认推荐。比如统计最近 100 次请求。底层是一个 Ring Bit Buffer (环形位缓冲区)，内存占用极小，计算效率极高。
   基于时间 (Time-based)：统计最近 N 秒。

Q2: Sentinel 有控制台能看图表，Resilience4j 没有控制台，你们怎么监控熔断状态？

   “这确实是 Resilience4j 的设计理念——它只做库，不做运维平台。
   我们的落地方案是 Micrometer + Prometheus + Grafana。
   Resilience4j 提供了 resilience4j-micrometer 模块。
   它会自动把熔断器的状态（Closed/Open）、缓冲区的失败率、限流剩余次数等指标暴露给 Micrometer。
   Prometheus 定时拉取数据，我们在 Grafana 上配置好大盘。

**Resilience4j vs. Sentinel：如何选择？** 

| 特性    | 	Resilience4j                               | 	Sentinel                            |
|-------|---------------------------------------------|--------------------------------------|
| 定位    | 	轻量级故障容错库 (Library)                         | 	全方位流量治理平台 (Platform/Framework)      |
| 设计哲学  | 	函数式、可组合、代码即配置                              | 	声明式、规则驱动、平台化管理                      |
| 核心功能	 | 熔断、重试、限流、隔离、超时	                             | 流控、熔断、系统保护、授权、热点参数                   |
| 隔离策略	 | 信号量 和 线程池 都支持	                              | 主要基于信号量                         |
| 配置方式	 | 主要通过代码或配置文件，高度灵活	                           | 主要通过控制台动态配置，实时生效                     |
| 监控	   | 无原生控制台，需整合 Micrometer, Prometheus, Grafana	 | 自带强大的 Dashboard，监控和规则配置一体化           |
| 生态	   | 纯粹，与 Spring 生态良好集成                          | 	与 Spring Cloud Alibaba 生态深度绑定，功能更丰富 |
---------------------------------------------------------------
## 限购与预售管理 LimitPurchaseServiceImpl+PreSaleTicketServiceImpl
网关/入口：SETNX 幂等校验。
Redis 校验：
Lua 脚本校验限购（原子性）。
Redis 预扣库存（原子性）。
Service 逻辑：
SnapshotTime 确定时间点。
StrategyPattern 确定价格（早鸟/原价）。
落库与消息：
RocketMQ 事务消息 -> 本地事务（Order 落库）-> Commit。
异步后续：
库存服务扣减 DB。
支付服务回调。
延时服务（超时未付 -> 回滚库存 + 回滚限购）。

“限购和预售是在下单核心链路中实现的，为了保证高性能和数据准确性，我采用了Redis 缓存前置校验 + 策略模式 + 异步一致性的方案。
核心流程分为四步：
前置校验（Redis）：
    在请求进入 Service 层之前，先通过 Redis 校验幂等性。
    同时利用 Redis 原子操作校验限购规则（用户已买数量 + 当前购买数量 <= 限购数）。
价格计算（策略模式）：
    根据当前时间判定是否处于早鸟票预售窗口。
    加载预售策略计算最终价格（策略模式避免大量的 if-else）。
核心落库（事务消息）：
    这里我们使用 RocketMQ 的事务消息模型。
    先发 Half 消息，执行本地事务（生成订单、保存详情），成功后 Commit 消息。
异步扣减与释放：
    库存服务监听消息进行扣减。
    关键点：如果后续支付超时或取消订单，需要通过延时队列回调，不仅要回滚库存，还要回滚限购额度。”

早鸟票（预售）的时间边界问题
   问题：早鸟票只到 12:00 结束。用户 11:59:59 进来，下单处理了 2 秒，落库时已经 12:00:01 了，按哪个价格算？
   回答：
   原则：以下单接口接收到请求的时间为准（或者以用户进入结算页面的时间为准，看业务宽容度）。
   实现：
   下单请求进来时，立即记录一个 snapshotTime。
   后续所有价格计算逻辑，都基于这个 snapshotTime 对比预售时间区间，而不是每次 new Date()，防止处理过程中跨越时间线导致价格不一致。
-------------------------------------------------------------------
## 促销活动管理   拷贝| Java 8 Stream | SpringTask |缓存管理 | git冲突 | RedisTemplate    
支持限时优惠活动的定时开启与关闭，配合Spring Task实现自动化任务调度。

### 1. 拷贝
日常业务（浅拷贝）：
如果是简单的属性对拷，比如 Controller 层接收参数转 Entity，早期我用 Spring 的 BeanUtils.copyProperties，因为它利用了缓存优化，比 Apache Commons 的快。
现状：现在核心业务中，我强制推行 MapStruct。因为它在编译期就生成了 Getter/Setter 代码，支持复杂映射和自定义转换，且类型安全，能在编译期发现字段名不一致的问题。
特殊场景（深拷贝）：
如果需要完全隔离对象（比如原型模式、或者修改复本不影响原件），我会用 JSON 序列化（Jackson/Gson）。虽然有序列化开销，但代码最简洁，且能处理复杂的嵌套结构。
如果是极致性能要求的深拷贝，我会手动编写拷贝构造函数，或者使用 MapStruct 配置 deepClone。”

1. Spring 提供的 BeanUtils.copyProperties   
浅拷贝陷阱：这是最致命的。如果对象里有个 List<Address>，BeanUtils 只复制了 List 的引用。当你修改新对象的 List 时，原对象的 List 也会变！
类型转换：Spring BeanUtils 对类型要求较严，Apache BeanUtils 会尝试自动转换（比如 String "123" 转 Integer 123），这有时是便利，有时是 Bug 源头。
日志与异常：属性复制失败时，有时异常不够直观。

b. MapStruct (编译时映射)
为什么 MapStruct 吊打 BeanUtils？
BeanUtils (运行时反射)：虽然有缓存，但反射本身的 CPU 开销无法避免。运行时才报错。
MapStruct (编译时生成)：它是一个注解处理器（Annotation Processor）。根据注解自动生成一个 UserMapperImpl.class，里面全是硬编码的 target.setName(source.getName())。运行时完全没有反射，等同于手写代码。

2. 实现深拷贝的方法   
方法一：手动实现深拷贝 clone()  :代码复杂且易错,final字段无法重新赋值
方法二：Java 原生序列化 (Serializable)：性能最差，且流中包含大量元数据，所有相关类都必须实现 Serializable 接口。
方法三：JSON 序列化：性能中等，但在处理循环引用（A引用B，B引用A）时容易报 StackOverflow，需要特殊配置（如 Jackson 的 @JsonIdentityInfo）。 需要引入第三方库。
方法四：手动编写拷贝构造函数 (最灵活、性能最好)  需要为每个类都提供一个“拷贝构造函数”增删字段需同步修改代码，易退化为浅拷贝

### 2. 使用 Java 8 Stream 进行字段校验
“关于字段校验，相比于传统的 if-else 嵌套（命令式编程），我更倾向于使用 Java 8 的 Stream 结合 Predicate（声明式编程）。
它的核心思想是将**校验规则（Predicate）与数据处理（Stream）**解耦。
高复用：每个校验规则是一个独立的 Predicate，可以在不同业务中复用。
易组合：利用 .or(), .and(), .negate() 可以像搭积木一样组合复杂的业务规则。
灵活终端：通过 findFirst() 实现快速失败（Fail-Fast），或者用 collect() 收集所有错误数据。”

### 3. SpringTask:
“在 Spring Boot 中实现轻量级定时任务非常简单：
主类加 @EnableScheduling 开启支持。
方法加 @Scheduled(cron = "0 0 12 * * ?") 定义规则。
它非常适合单体应用内部的简单维护任务（如每天清理临时文件、刷新缓存）。”
Q1：Spring Task 默认是单线程的还是多线程的？会有什么问题？
默认是单线程的！因此必须配置线程池。
Q2：分布式环境下（多实例部署）怎么用 Spring Task？
问题：如果你的服务部署了 3 台机器。到了中午 12 点，这 3 台机器上的 @Scheduled 都会触发。导致任务重复执行（比如发了 3 遍生日邮件）。
解决方案：分布式锁：任务执行前先去 Redis 抢锁（SetNX），抢到的执行，没抢到的跳过（ShedLock 是个好用的库）。
## 4. 缓存管理
多级缓存架构（Caffeine+Redis+db）
一级缓存 (Caffeine)：
用来抗极热点数据（Top 100）。因为 Redis 也有网络 IO 开销，而 Caffeine 是进程内缓存，速度是纳秒级的。
我们选 Caffeine 是因为它采用了 W-TinyLFU 算法，相比传统 LRU，它能更好地抵抗‘稀疏流量扫描’（比如爬虫遍历），防止热点数据被误淘汰。
二级缓存 (Redis)：
用来抗海量数据的读请求，作为数据库的保护伞。
一致性保障：
对于 DB 和 Redis，我们采用标准的 Cache Aside (旁路缓存) 策略：先更新 DB，再删除 Redis。
对于 Redis 和 Caffeine，为了解决多节点本地缓存不一致的问题，我们引入了 Redis Pub/Sub 机制。当一个节点修改数据后，发布消息通知其他所有节点清理各自的本地缓存。”

1. 本地缓存的王者：Caffeine  
早期的 ConcurrentHashMap 手动实现，到 Google Guava Cache，再到目前性能最优的 Caffeine。  
Caffeine是基于 Java 8 对 Guava Cache 的优化，优势在于其先进的缓存淘汰算法：W-TinyLFU。  

传统算法的缺陷：  
LRU (最近最少使用)：容易受到偶然的批量扫描操作污染，导致热点数据被错误淘汰。
LFU (最不经常使用)：需要为每个缓存项维护一个复杂的计数器，内存开销和计算成本都很高，并且无法很好地处理时效性热点（一个曾经的热点数据可能永远不会被淘汰）。
W-TinyLFU (Window TinyLFU) ：结合了 LRU 和 LFU 的优点，它将数据分为三个区域：  
Window 区域 (LRU)：新进入的数据先放在这里，快速淘汰掉只被访问一次的“过客”数据，防止污染主缓存区。  
Probation (考验期) 区域 (LFU)：从 Window 区淘汰下来的数据会进入这里。它采用一种名为 Count-Min Sketch 的近似计数算法，用极小的内存开销来估算访问频率。  
Protected (保护期) 区域 (LFU)：只有在考验期被再次访问的数据，才有资格进入这个区域，成为真正的热点数据。这个区域占了大部分缓存空间。
当新数据 B 想进入主缓存区时，它必须和即将被淘汰的老数据 A 进行 PK。

2. 缓存的三大经典“灾难”及其应对策略  
   缓存穿透 (Penetration)：查不存在的数据。
   解法：布隆过滤器 (Bloom Filter) 或 缓存空对象 (Null Object)。
   缓存击穿 (Breakdown)：一个热点 Key 突然挂了。
   解法：互斥锁 (Mutex) 或 逻辑过期 (Logical Expiration)。
   缓存雪崩 (Avalanche)：一大片 Key 同时挂了。或缓存服务不可用
   解法：过期时间加随机值 (Jitter)。缓存高可用集群：如Redis Cluster+ 服务降级与限流 + 多级缓存

3. 缓存更新策略  
a. Cache Aside (旁路缓存) 先更新数据库。再删除缓存。
   动作：代码显式调用 DB 和 Cache。
   记忆锚点：“懒惰+谨慎”。
   懒惰：读的时候没有才去查（Lazy Load）。
   谨慎：写的时候怕不一致，所以选择删缓存而不是更缓存。  
       如果先删缓存：线程 A 先删除缓存，再去更新数据库。此时线程 B 来读取，发现缓存没有，就去数据库读到了旧值并写入缓存。然后线程 A 才完成数据库更新。缓存中就一直是旧数据了

b. Read/Write Through (读/写穿透):应用层只管找缓存，缓存变身“代理人”同步去操作数据库。
动作：应用层代码里看不到数据库连接，只调用 cache.get() 或 cache.put()。
缓存组件封装了数据库的读写细节，对应用层透明。数据库写完再更新缓存
“这在本地缓存（如 Guava/Caffeine）中很常见，因为它们支持 Loader 和 Writer。但在分布式缓存（Redis）中很难做，因为 Redis 通常只作为 K-V 存储，不负责主动连你的 MySQL。”

c. Write Back (Write Behind, 写回): 只写内存立刻返回，后续异步慢慢刷盘，追求极致性能但得承担丢数据风险。
谁主导：缓存组件 (异步线程)。
动作：Write Only Cache -> Ack -> Async Flush DB。
这其实是 Linux Page Cache 或者 MySQL Buffer Pool 的核心机制（脏页刷盘）。
“适合写密集型且非核心数据。比如：点赞数、浏览量统计。丢了几个赞无所谓，但如果每个赞都落库，数据库会死。”

“我们主要使用的是 Cache Aside (旁路缓存) 模式，也就是所谓的**‘手动挡’**。
为什么不选 Read/Write Through（托管模式）？
因为我们用的是 Redis，它不像 Caffeine 那样方便集成数据库加载逻辑。如果要强行实现 Through 模式，需要开发复杂的 Redis 插件，成本太高且侵入性强。
为什么不选 Write Back（先斩后奏）？
因为我们的业务（如订单、金融）对数据一致性要求极高。Write Back 虽然写得快，但如果缓存宕机，数据就永久丢失了，这是业务无法接受的。
所以 Cache Aside 是最优解：
虽然需要我们在代码里显式维护‘先更 DB 再删缓存’的逻辑，但它逻辑最清晰，且配合 Canal 等组件做兜底，能实现最终一致性。”

Cache Aside 的先更DB再删缓存，面试官一定会问：“如果删缓存失败了怎么办？”
“为了保证最终一致性，我们引入了 Canal。
业务解耦：业务代码只负责写数据库，不操作缓存，提升写性能。
异步删除：Canal 伪装成 MySQL Slave，监听数据库的 Binlog。一旦发现数据变更，Canal 解析 Binlog 并投递到 MQ。
重试机制：专门的消费者订阅 MQ，负责删除 Redis 缓存。如果删除失败，利用 MQ 的 ACK 机制 自动重试，直到成功为止。

Q：秒杀场景下，库存扣减怎么做缓存？
A：
Redis 预扣减：利用 decr 原子性，库存减到 0 直接拦截，不查 DB。
本地缓存标记：在 Caffeine 里存一个 boolean isSoldOut。如果 Redis 返回没货，把本地标记置为 true。后续请求连 Redis 都不用查，直接在 JVM 层拦截。

Q: 如果修改了数据库，Redis 里的数据怎么变？本地缓存 Caffeine 怎么变？
A:
DB vs Redis: 采用 Cache Aside (先更DB，再删缓存)。为了防止删除失败，可以配合 binlog 监听（Canal）进行异步删除。
Redis vs Caffeine: 这就是引入 Redis Pub/Sub (发布订阅) 的原因。当某个节点修改了数据，发送消息到 Redis Channel，所有应用节点监听该 Channel，收到消息后清除自己的本地 Caffeine 缓存。

### 4. git 冲突  
冲突发生在 git merge 或 git rebase 的时候，当 Git 发现两个不同的分支对同一个文件的同一部分都做了修改，它无法自动判断哪个修改是正确的

冲突主要发生在以下几种情况：
1.  内容冲突 (Content Conflict)
2.  修改/删除冲突 (Modify/Delete Conflict)
    *   描述： 一个分支修改了一个文件，而另一个分支删除了同一个文件。
    *   表现： `git status` 会显示类似 `deleted by them` 或 `deleted by us` 的信息。

3.  新增/新增冲突 (Add/Add Conflict)
    *   描述： 两个分支都添加了同名但内容不同的文件。或者，一个分支添加了文件，另一个分支在相同路径下添加了同名文件（内容可能相同或不同）。
    *   表现： `git status` 会显示类似 `both added` 的信息。

4.  重命名冲突 (Rename Conflict)
    *   描述： 一个分支重命名了文件，而另一个分支修改了原始文件，或者两个分支将同一个文件重命名为不同的名称。
    *   表现： Git 可能会显示文件被重命名并修改，或重命名冲突。Git 通常能很好地处理重命名，但如果重命名后又对内容进行了冲突修改，或者重命名本身冲突，则需要手动解决。

冲突解决的通用步骤
定位冲突：
首先通过 git status 明确哪些文件处于 Unmerged 状态。
如果是简单的文本冲突，我会直接看 IDE（如 IntelliJ IDEA 或 VS Code）的图形化对比界面，它能很清晰地展示 Current Change（当前分支）和 Incoming Change（传入分支）。
沟通确认：
如果冲突的代码是我自己写的，我会直接合并。
关键点：如果冲突涉及同事的代码逻辑，或者我不确定对方修改的意图，我绝不会擅自覆盖。我会立刻联系对方（面对面或通讯工具），确认保留哪一部分，或者是将两者的逻辑合并。
解决与提交：
在 IDE 中解决完所有冲突后，执行 git add 标记为已解决。
如果是 Merge 产生的冲突，最后执行 git commit。
如果是 Rebase 产生的冲突，执行 git rebase --continue，直到所有 Commit 应用完毕。”

“在 git merge 和 git rebase 时，--ours 和 --theirs 代表的含义一样吗？”

“不一样，这正好是相反的，
在 git merge 时：
--ours：代表当前分支（我正在工作的分支，HEAD）。
--theirs：代表传入分支（我要合并进来的那个分支）。
记忆法：Merge 是把别人拉进来，ours 就是我家，theirs 是客人家。
在 git rebase 时（反直觉）：
--ours：代表基底分支（Upstream，通常是 master/main）。
--theirs：代表当前正在变基的分支（也就是我正在写的代码）。
原因：Rebase 的本质是把我的提交一个个‘拆下来’，然后‘贴’到基底分支上去。在‘贴’的过程中，Git 实际上是站在基底分支（ours）的角度，把我的代码（theirs）作为一个个补丁打上去的。
结论：在 Rebase 遇到冲突时，如果我想保留我自己的代码，应该用 --theirs，而不是 --ours。”

git rebase 的冲突处理特点
Merge 冲突：只解决一次。Git 会尝试一次性合并所有差异，解决完提交一次即可。
Rebase 冲突：可能解决多次。Rebase 是逐个应用 Commit。如果你当前分支有 10 个 Commit，且都修改了同一个文件，你可能需要连续解决 10 次冲突（或者中途使用 git rebase --skip）。

### 5. RedisTemplate
你们项目中是怎么使用 Redis 的？为什么用 RedisTemplate？

“我们主要使用 Spring Data Redis 提供的 RedisTemplate。它封装了底层 Jedis 或 Lettuce 的连接管理，提供了高度抽象的 API（如 opsForValue, opsForHash）。
关于 RedisTemplate，我有两个核心的使用心得：
序列化策略（最重要）：
Spring 默认使用的是 JdkSerializationRedisSerializer，这会导致存储在 Redis 里的数据是乱码（二进制流），既不可读，体积又大。
所以我们通常会自定义配置：Key 使用 StringRedisSerializer（保证 Key 可读），Value 使用 GenericJackson2JsonRedisSerializer（转为 JSON 存储，兼容性好）。
StringRedisTemplate：
如果数据结构简单，全是字符串，我会直接用 StringRedisTemplate，它默认就是 String 序列化，省去了配置的麻烦。”

`RedisTemplate` 的高级用法
1.  事务 (Transactions)：
    *   `RedisTemplate` 支持 Redis 的事务（`MULTI`/`EXEC`）。
    *   使用 `template.execute(new SessionCallback<Object>() { ... })` 或 `template.multi(); ... template.exec();`。
    *   **注意：** Redis 事务不是 ACID 事务，它只保证原子性和隔离性（在 `EXEC` 命令执行期间，其他客户端的命令会被阻塞）。

2.  管道 (Pipelining)：
    *   `RedisTemplate` 支持 Redis 的管道，可以批量发送命令，减少网络往返时间，提高性能。
    *   使用 `template.executePipelined(new RedisCallback<Object>() { ... })`。

3.  发布/订阅 (Pub/Sub)：
    *   `RedisTemplate` 可以用于发布消息：`template.convertAndSend("channel", "message")`。
    *   订阅消息则需要配置 `MessageListenerAdapter` 和 `RedisMessageListenerContainer`。

4.  Lua 脚本：
    *   `RedisTemplate` 可以执行 Lua 脚本，实现原子性的复杂操作。
    *   使用 `template.execute(script, keys, args)`。
-----------------------------------------------------------------------------
## 消息通知管理  
基于RocketMQ异步处理订单状态变更通知、日志记录等后台任务，提高响应效率。

“在项目中，我利用 RocketMQ 实现了核心业务与辅助业务的解耦和异步化，主要应用在两个场景：
订单状态变更通知：
当订单状态流转（如下单、支付、发货）时，主流程只负责修改数据库状态，然后通过 RocketMQ 发送消息。
下游的通知服务（短信/邮件）、积分服务、仓储服务作为消费者订阅该 Topic，实现逻辑解耦。这不仅降低了下单接口的 RT（响应时间），还防止了下游服务故障拖垮主流程。
操作日志记录：
考虑到日志数据量大且对实时性要求不高，我使用了 RocketMQ 的 asyncSend (异步发送) 模式。
将日志信息快速丢入 MQ，消费者端在后台批量入库（ClickHouse 或 MySQL），从而保证了前台操作的极致流畅体验。”

“虽然代码中没有显式实现心跳，但 RocketMQ 底层有一套完整的心跳与下线机制：
心跳发送：
客户端（Producer/Consumer）启动后，会启动一个定时任务，每隔 30秒 向所有 Broker 发送心跳包。
心跳包里包含了 ClientID、ConsumerGroup、订阅的 Topic 等信息。
服务端检测：
Broker 也会每隔 10秒 扫描所有存活的连接。
如果发现某个连接超过 120秒 没有发送心跳，Broker 就会判定该客户端下线，断开连接。
触发重平衡 (Rebalance)：
一旦 Broker 判定消费者下线，它会通知该 Consumer Group 下的其他消费者触发重平衡，将挂掉的节点负责的 MessageQueue 重新分配给其他存活的节点，确保消息不丢失。”

1. 顺序消息问题（Ordering）
   问题：订单状态是从“待支付”->“已支付”->“已发货”。如果“已发货”的消息比“已支付”先被消费了，你的系统会不会乱套？
   你的漏洞：普通消息是无序的，或者说是部分有序的。
   解决方案：
   发送端：必须使用 syncSendOrderly (顺序发送)。
   Key选择：使用 OrderId 作为 HashKey（分区键）。
   原理：RocketMQ 保证同一个 OrderId 的消息投递到同一个 MessageQueue 中。消费者端采用 MessageListenerOrderly，锁定该 Queue 顺序消费。
   话术：“针对订单状态流转，我使用了 RocketMQ 的顺序消息特性，以 OrderId 为 HashKey，确保同一订单的状态变更消息严格有序。”
2. 消息丢失与可靠性（Reliability）
   问题：日志丢了没关系，但“订单支付成功”的消息丢了，用户没收到积分，会投诉的。怎么保证不丢？
   你的回答：
   生产端：订单消息使用 syncSend，捕获异常并重试。对于极重要消息，可开启 Broker 的 SYNC_MASTER + SYNC_FLUSH（同步刷盘、同步双写，但性能有损耗，看业务取舍）。
   消费端：手动 ACK（RocketMQ 默认是手动 ACK）。
   只有当业务逻辑（加积分、发短信）全部执行成功后，才返回 ConsumeConcurrentlyStatus.CONSUME_SUCCESS。
   如果报错，返回 RECONSUME_LATER，RocketMQ 会自动重试（默认 16 次）。
3. 消息积压（Backlog）
   问题：双11流量太大，操作日志瞬时产生了 100 万条，日志消费者处理不过来，MQ 报警了怎么办？
   你的回答：
   临时扩容：
   如果 Topic 下的 MessageQueue 数量够多（比如 16 个），直接增加消费者实例（加机器），提高并发度。
   如果 MessageQueue 数量不够（只有 4 个），加机器没用（一个 Queue 只能被一个消费者消费）。
   降级策略：
   编写一个临时的消费者，不处理业务逻辑，直接丢弃消息（或者只把消息落到简单的文本文件中）。
   先把积压的水位降下来，等高峰期过了，再写脚本重新把文件里的日志灌入数据库。

## 数据校验和异常处理
你们后端接口的参数校验和异常处理是怎么做的？

“我们遵循**‘防守式编程’和‘统一响应规范’**的原则。
参数校验：
我们拒绝在 Controller 层写大量的 if (param == null) 判断。
而是使用 Hibernate Validator，在 DTO 对象上加注解（如 @NotNull, @Pattern）。
并在 Controller 方法参数上加 @Validated 触发校验，将非法请求挡在业务逻辑之外。
异常处理：
我们利用 Spring 的 @RestControllerAdvice 结合 @ExceptionHandler 实现了全局异常捕获。
无论是参数校验失败抛出的 MethodArgumentNotValidException，还是业务层抛出的自定义 BusinessException，甚至是未知的 Exception，都会被拦截并转换为统一的 RestResult (code, msg, data) JSON 格式返回给前端。”

1. @Valid vs @Validated 的区别
  
   @Valid：
   来源：标准 JSR-303 规范 (javax.validation / jakarta.validation)。
   特技：支持嵌套校验。如果你有一个 OrderDTO，里面包含 List<ItemDTO>，必须在 list 字段上加 @Valid，里面的 ItemDTO 校验才会生效。
   @Validated：
   来源：Spring 框架特有 (org.springframework.validation)。
   特技：支持分组校验 (Groups)。
   场景：UserDTO 用于新增时，ID 必须为空；用于更新时，ID 必须不为空。这时可以用 @Validated({AddGroup.class}) 来区分。
   结论：一般建议在 Controller 方法参数上用 @Validated（支持分组），在 DTO 内部嵌套字段上用 @Valid（支持嵌套）。
2. Q: 新增和修改用的是同一个 DTO，但校验规则不一样（比如 ID），怎么处理？
   A:
   “使用 分组校验 (Grouping)。
   定义两个接口 CreateGroup 和 UpdateGroup。
   在 DTO 的 ID 字段上：@Null(groups = CreateGroup.class) 和 @NotNull(groups = UpdateGroup.class)。
   在 Controller 的新增接口用 @Validated(CreateGroup.class)，修改接口用 @Validated(UpdateGroup.class)。”
