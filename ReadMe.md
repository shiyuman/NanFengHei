# 南峰黑私房手作系统

# 目录--设计接口+知识点

## 用户管理

实现用户的注册、登录功能，集成JWT进行身份认证与权限控制，确保系统安全访问。

1. 用户登录  
   用户传入UserLoginDTO[name,password]进login  
   login:  1.查找username； 2.验证password 3.jwtUtil生成token并返回   
   1.创建当前时间和过期时间（当前时间+配置的过期秒数）   
   2.使用JJWT库构建JWT token

2. 在JWT中，Token由三部分组成，用点(.)分隔：   
   Header（头部）：由JJWT库自动创建,包含算法信息：{"alg": "HS512", "typ": "JWT"}   
   Payload（载荷）：sub (subject)：用户名+iat (issued at)：签发时间+exp (expiration)  
   Signature（签名）：   
   由JJWT库使用HS512算法生成   
   通过对Header和Payload的Base64编码字符串，加上密钥(jwtConfig.getSecret())进行签名
3. JWT配置类的作用：   
   a. 读取配置属性：   
   从application.yml或application.properties文件中读取JWT相关配置  
   使用@ConfigurationProperties(prefix = "jwt")绑定配置   
   b. 存储配置参数：  
   secret：JWT签名密钥
   expiration   
   c. 提供getter/setter方法：允许其他类获取和设置JWT配置参数
-----------------------------------------------------------------------------------
## 商品管理
支持商品的上架、下架、分类管理及库存控制，满足商品信息维护和展示需求。  
### 1. Apache POI库  
   解决在 Java 程序中读写 Microsoft Office 格式文件的问题  
   HSSF  → XSSF  →  SXSSF   
   处理旧版的 Excel 文件 → 处理新版的 Excel 文件 → 流式扩展，用于解决在生成海量数据 Excel 时OOM

内存消耗巨大：
HSSF 和 XSSF 采用的是基于 DOM 的解析模型，需要将整个文件加载到内存中构建成一个对象树。   
为了解决OOM，POI 提供了 SXSSF。  
它是一个基于流的写入 API，只在内存中保留一个固定大小的行（滑动窗口），当行数超过这个窗口时，就会将最旧的行数据刷到磁盘的临时文件中，从而大大降低了内存占用。但 SXSSF 主要是为写入设计的，读取大文件仍然是个难题。   

一个非常优秀的替代方案是阿里巴巴开源的 EasyExcel。   
内存优化：底层对 POI 进行了封装，读取和写入都采用基于 SAX 的流式解析，内存占用极低，可以轻松处理百万级别的数据。   
API 设计简洁：通过注解（Annotation）将 Java 对象（POJO）与 Excel 的行数据直接映射，代码量大大减少。

POI工作流程：  
1.**创建工作簿和工作表**：Workbook→sheet→cell
```java
Workbook workbook = new XSSFWorkbook();
Sheet sheet = workbook.createSheet("商品数据");
```
首先创建了一个新的Excel工作簿（`Workbook`），然后在其中创建了一个名为“商品数据”的工作表（`Sheet`）。  
`XSSFWorkbook`是Apache POI库中用于处理.xlsx格式文件的类。
WorkbookFactory.create() 方法的优势在于可以根据输入流自动识别 Excel 文件的格式（.xls还是.xlsx），并创建相应的工作簿对象。而目前代码中直接使用 new XSSFWorkbook() 的方式只能处理 .xlsx 格式的文件。
```java
// 从输入流创建Workbook（适用于读取现有Excel文件）
Workbook workbook = WorkbookFactory.create(inputStream);

// 创建新的空Workbook（适用于创建新的Excel文件）
Workbook workbook = WorkbookFactory.create(true);
```

2.**设置列标题行**：
```
    Row headerRow = sheet.createRow(0);   
    headerRow.createCell(0).setCellValue("商品ID");   
```
在工作表的第一行（即索引为0的行）创建了一个标题行（`Row headerRow`），并在该行的各个单元格（`Cell`）中分别设置了列标题

3.**创建日期格式化对象**：

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   

若不格式化，则在Excel表格中显示的创建时间会是原始的Date对象toString()，通常是类似"Mon Nov 03 15:
30:45 CST 2025"，而不是"2025-11-03 15:30:45"

4.**填充商品数据**

5.**设置HTTP响应头**：

1. `response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");`  
   设置响应的内容类型为Excel文件格式（xlsx）。浏览器会根据这个MIME类型识别这是一个Excel文件。

2. `ContentDisposition contentDisposition = ContentDisposition.attachment()
        .filename("商品数据.xlsx", StandardCharsets.UTF_8)
        .build();`  
   使用Spring框架的ContentDisposition工具类创建了一个附件类型的Content-Disposition头部信息，并指定了文件名为"
   商品数据.xlsx"，同时使用UTF-8字符集编码以支持中文文件名。

3. `response.setHeader("Content-Disposition", contentDisposition.toString());`  
   将上面构建好的Content-Disposition对象转换成字符串并设置到HTTP响应头中。这告诉浏览器应该将响应内容作为附件下载，并提示默认文件名。

4. `workbook.write(response.getOutputStream());`  
   将Apache POI创建的Excel工作簿直接写入HTTP响应的输出流中，这样客户端就可以接收到完整的Excel文件内容。

整个过程就是：
- 告诉浏览器返回的是一个Excel文件
- 告诉浏览器应该如何处理这个文件（作为附件下载）
- 把实际的Excel文件内容发送给浏览器

### 2. 各种判空方式的总结对比：   
空：假设有个盒子   
null:无盒子。一个变量没有指向任何内存地址  
""/Empty:有盒子，但无东西。长度=0的字符串  
" "/Blank:有盒子又透明东西。含空格、Tab键、换行符的字符串   
判空：   
a. null检查  
== null / != null 最原始  
Objects.isNull() / Objects.nonNull() [java8+]  上面的优雅版，可读性更强   
b. 字符串内容检查，前提是有盒子(非null)  
isEmpty（） 只在乎长度是否为0，就算有透明填充物也算”非空“   
trim().isEmpty()  先去除透明填充物再判空，效果=Blank但效率低，因为可能创建新字符串对象，且只能去除前后空格   
isBlank()[java11+] 就算有透明填充物也算空(毕竟语义上翻译为空白)  

最好办法：  
a. (要求java11+)
```
username == null || username.isBlank()
```
b. Apache Commons Lang 的 StringUtils [ null-safe（你不需要自己先判断 null）]
```
// 需要引入 commons-lang3 依赖
import org.apache.commons.lang3.StringUtils;

// 检查 null, empty ("")
if (StringUtils.isEmpty(username)) {
    System.out.println("用户名为空！");
}

// 检查 null, empty (""), blank ("   ") (最常用)
if (StringUtils.isBlank(username)) {
    System.out.println("用户名无效！");
}
```

### 3. MetaObjectHandler自动填充创建时间和更新时间等字段

MetaObjectHandler是MyBatis Plus提供的元数据对象处理器，用于自动处理字段的填充操作，无需在业务代码中手动设置创建时间、更新时间等公共字段。

a. 给要填充的字段加Annotation:@TableField(fill = FieldFill.INSERT/UPDATE)  
b. 实现 MetaObjectHandler 接口，并告诉它具体的填充规则
```    
//非null才更新
@Component
public void insertFill(MetaObject metaObject) {
   //参数分别是：通用对象(反射赋值)，要填充的字段名，字段的数据类型(消除方法重载 (Overloading) 的歧义)，具体的值
   this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
 }    
```
c. 加@Component，MP的自动配置机制会自动检测到这个bean并使其生效

### 4. 添加乐观锁机制防止并发更新冲突

乐观锁是一种并发控制机制，假设数据一般不会发生冲突，只在提交更新时检查是否违反了并发控制规则。

乐观锁的实现主要有三种主流方式：  
版本号机制 (Versioning)：最常用、最可靠的方式。  
时间戳机制 (Timestamping)：一种变体，利用时间戳判断。  
CAS (Compare-And-Swap)：这更多是思想层面的体现，版本号和时间戳机制都是其在数据库领域的具体应用。

1. 版本号机制 (Versioning)  
这是实现乐观锁的黄金标准，也是最推荐的方式。   

MyBatis Plus通过version字段实现乐观锁机制。
- 在实体类中的version 字段上添加@Version注解
- 在 MyBatis-Plus 的配置类中注册 OptimisticLockerInnerInterceptor
- 更新数据时会自动在SQL中添加version条件，确保只有version匹配时才能更新成功；如果更新影响行数为0，则说明版本已变化，抛出异常。

业务代码：正常进行更新即可，MyBatis-Plus 会自动处理 version 的比对和自增。

2. 时间戳机制 (Timestamping)

逻辑上与版本号相似，但使用时间戳字段。

优缺点分析   
优点：  
字段本身具有业务含义，可以直接看出数据的最后修改时间。   
无需在应用层手动管理 version 的自增。   
缺点（非常重要）：   
存在时钟偏移问题：在分布式环境下，不同服务器的系统时间可能存在微小差异，这会导致乐观锁失效。   
精度问题：如果并发极高，在同一毫秒（或数据库支持的最小时间单位）内发生多次更新，时间戳可能无法区分先后顺序，导致并发问题。

3. CAS (Compare-And-Swap) 思想  

CAS 是一种底层的原子操作，它包含三个操作数：内存位置（V）、预期原值（A）和新值（B）。执行 CAS 操作时，当且仅当内存位置 V 的值与预期原值 A 相匹配时，处理器才会用新值 B 更新 V 的值，否则不执行任何操作。整个过程是原子的。

与乐观锁的关系： 数据库中基于 version 的 UPDATE ... WHERE version = ? 操作，本质上就是一种宏观的 CAS 实现。

在编程语言中的应用： Java 的 java.util.concurrent.atomic 包（如 AtomicInteger）就是基于 CPU 提供的 CAS 指令实现的，用于无锁化编程。

乐观锁失败后的处理策略   
自动重试： 最常见的策略。当更新失败时，程序可以重新读取最新数据，再次尝试业务逻辑和更新。通常会设置一个最大重试次数，避免在冲突持续发生时陷入死循环。

乐观锁优缺点：   
优点
1. 避免了悲观锁独占对象的现象，提高了并发能力，读可以并发   

缺点
1. 乐观锁只能保证一个共享变量的原子操作，互斥锁可以控制多个。乐观锁本身只对单次 UPDATE 的单行数据生效，无法原生保证跨多行或多表的业务原子性。
2. 长时间自旋导致开销大
3. ABA问题，CAS比较内存值和预期值是否一致，可能是A→B→A了，A可以是对象中的某个属性，但是在ABA过程中其他属性变了，会被CAS认为没有改变。要解决，需加一个版本号(Version)
### 5. 实现逻辑删除而非物理删除
1. 手动实现逻辑删除的痛点   
查询和更新都必须手动加上 WHERE deleted = 0   
DELETE 操作都必须被改成 UPDATE 操作
2. MyBatis-Plus 的插件机制   

A. 第一步：实体类注解   
你需要在实体类中用@TableLogic 明确告诉 MP 哪个字段是逻辑删除的标记字段。

B. 第二步：全局配置 - application.yml  
虽然 @TableLogic 可以在每个实体类上单独配置，但通常一个项目中的逻辑删除规则是统一的。因此，在 application.yml 中进行全局配置是最佳实践。
```yml
mybatis-plus:
global-config:
db-config:
logic-delete-field: deleted # 全局逻辑删除的实体字段名
logic-delete-value: 1       # 逻辑已删除值(默认为 1)
logic-not-delete-value: 0   # 逻辑未删除值(默认为 0)
```
logic-delete-field: 如果你的实体类中逻辑删除字段不叫 deleted，比如叫 is_deleted，那么你可以在这里全局指定。

@TableLogic vs application.yml   
特殊>一般，若所有逻辑字段名一样，只需app即可；若有特例就用@TableLogic

C. 第三步：SQL 的自动改写  
插入操作 (INSERT) INSERT 操作不受影响，MyBatis-Plus 会使用你在实体类中为 deleted 字段设置的默认值（或全局配置的未删除值 logic-not-delete-value）进行插入。

4. 注意事项   

a. 唯一索引问题： 假设 name 字段有唯一约束 UNIQUE(name)。当你逻辑删除一个名为 "admin" 的用户后，再想创建一个新的名为 "admin" 的用户，数据库会因为唯一约束而插入失败。  

解决方案一：使用联合唯一索引 UNIQUE(name, deleted)。但这会导致你可以有多个 name 相同但 deleted 状态不同的记录，需要根据业务调整。  
解决方案二：在逻辑删除用户时，将 name 字段的值修改为一个带特殊标记的唯一值，例如 name + "_deleted_" + id。   

b. 性能考量： 随着被逻辑删除的数据越来越多，表会变得越来越臃肿。查询性能可能会下降，因为数据库索引也需要维护这些“已删除”的数据。

解决方案：必须为逻辑删除字段 deleted 创建索引，最好是与其他查询条件字段（如 id, user_id）建立联合索引。定期对这些无用的数据进行归档或物理删除.

5. 如何查询被删除的数据？ 既然框架自动屏蔽了已删除数据，那如果我就是想看回收站里的内容怎么办？  

解决方案：自己编写 SQL 语句。MyBatis-Plus 的逻辑删除功能只对它提供的 BaseMapper 方法和 QueryWrapper 生效。对于你在 XML 文件中或使用 @Select 注解自定义的 SQL，它不会进行任何修改，你可以自由地查询 deleted = 1 的数据


### 6. MP插件总结
|              插件/功能               |   核心作用    | 解决的问题           | 典型场景        | 
|:--------------------------------:|:---------:|-----------------|-------------|
|    PaginationInnerInterceptor    |   物理分页    | 手写分页 SQL 繁琐且易错  | 数据列表展示      |  
| OptimisticLockerInnerInterceptor |    乐观锁    | 高并发下的数据更新冲突     | 商品库存、账户余额   |
|    IllegalSqlInnerInterceptor    | 防止全表更新/删除 | 误操作导致全表数据被修改或删除 | 生产环境必备      |
| DynamicTableNameInnerInterceptor |  动态修改表名   | 数据量大需要分表存储      | 按月/年分表、日志系统 | 
|    TenantLineInnerInterceptor    |  多租户数据隔离  | 多租户系统中的数据安全隔离   | SaaS 应用     |     
|          Logical Delete          |    软删除    | 数据误删恢复、审计需求     | 用户管理、订单系统   |       

自 3.4.0 版本以后，MP 推荐使用新的插件体系。所有的插件（除了个别特殊功能）都作为“内部拦截器”（InnerInterceptor）被添加到一个总的 MybatisPlusInterceptor 中。这种链式结构使得插件的管理和扩展更加清晰。

标准配置方式如下：
```java
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 1. 定义一个总的 MybatisPlusInterceptor 拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 2. 在其中添加你需要的具体功能插件（内部拦截器）
        // 注意：插件的添加顺序有时会影响执行，分页插件建议放在最后
        
        // 添加乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 添加分页插件，并指定数据库类型（例如 MySQL）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        return interceptor;
    }
}
```
以下是 MP 提供的主要插件及其功能：
1. 分页插件 (PaginationInnerInterceptor)   
工作原理：它会拦截你的查询请求，自动分析你传入的分页参数（Page 对象），然后根据你配置的数据库类型，动态地在原始 SQL 语句的末尾拼接上对应的分页查询语句（如 MySQL 的 LIMIT，Oracle 的 ROWNUM）。  
使用场景：任何需要列表展示并进行分页的场景。开发者只需调用 mapper.selectPage(new Page<>(1, 10), queryWrapper) 即可，无需手写分页 SQL。   
 

2. 乐观锁插件 (OptimisticLockerInnerInterceptor)  
工作原理：当你执行更新操作时，它会自动检查实体对象中被 @Version 注解标记的字段。在生成 UPDATE 语句时，它会将这个版本号字段作为 WHERE 条件的一部分，并在 SET 子句中将其值加一。   
原始调用：product.setStock(99); productMapper.updateById(product);   
实际 SQL：UPDATE product SET stock=99, version=version+1 WHERE id=? AND version=?;


3. 防全表更新与删除插件 (IllegalSqlInnerInterceptor)  
工作原理：在 SQL 执行前，该插件会解析即将执行的 UPDATE 和 DELETE 语句。如果发现语句中缺少 WHERE 子句，它会直接抛出异常，从而阻止这条危险的 SQL 执行。


4. 多租户插件 (TenantLineInnerInterceptor)  
核心功能：为 SaaS（软件即服务）应用提供数据隔离支持，自动为所有 SQL 操作添加租户 ID 条件。   
工作原理：你需要配置一个租户 ID 字段（如 tenant_id）。之后，该插件会拦截所有的 SELECT, UPDATE, DELETE 语句，并自动在 WHERE 条件中追加 AND tenant_id = ?。对于 INSERT 语句，它会自动填充租户 ID 字段的值。
   

5. 动态表名插件 (DynamicTableNameInnerInterceptor)  
核心功能：允许在运行时动态地改变 SQL 语句中要操作的表名。   
工作原理：通过 ThreadLocal 存储需要动态替换的表名。在 SQL 执行前，插件会根据 ThreadLocal 中的值，将 SQL 中的原始表名替换为指定的动态表名。   
使用场景：   
a. 分库分表：根据年份、用户 ID 等规则将数据存放在不同的表中（如 order_2024, order_2025）。   
b. 多租户的一种实现：为每个租户创建独立的表。


6. 特殊功能（非内部拦截器） ：逻辑删除 (Logical Delete)

### 7. Project Lombok
一个通过在编译时自动生成代码来减少 Java 样板代码的库。
1. 三种注解
A. @NoArgsConstructor 生成一个无参数的构造函数。  
使用场景：许多框架（包括 MyBatis-Plus、JPA/Hibernate、Jackson 等）在进行反序列化或通过反射创建对象时，都依赖于一个公共的无参构造函数。它们需要先创建一个“空”的对象实例，然后再通过 setter 方法或直接操作字段来填充数据。  

B. @AllArgsConstructor  生成一个包含所有字段的构造函数。   
使用场景：方便开发者在代码中快速创建一个已完全初始化的对象，常用于 DTO（数据传输对象）或测试代码中。

C. @RequiredArgsConstructor  生成一个针对“必需”字段的构造函数。

“必需”字段的定义：  
所有被 final 修饰的字段。   
所有被 @NonNull 注解标记的字段。

```java
import lombok.RequiredArgsConstructor;
import lombok.NonNull;

@RequiredArgsConstructor
public class User {
private final Long id; // final 字段，是必需的
@NonNull private String name; // @NonNull 标记的字段，是必需的
private Integer age; // 普通字段，不是必需的
}
```
使用场景：非常适合用于依赖注入（Dependency Injection）。通过将依赖项声明为 final 字段，并使用 @RequiredArgsConstructor，可以实现优雅的构造器注入，同时保证了依赖的不可变性。

2. 可以共存：Java 的方法重载（Overloading）机制

3. 与 Lombok 和 MyBatis-Plus 的关系   

A. 与 Lombok 的关系   
这三个注解是 Lombok 库的核心组成部分。Lombok 是一个编译时的工具，它通过注解处理器（Annotation Processor）在 Java 源代码编译成字节码的过程中，动态地修改抽象语法树（AST），将生成的构造函数、getter/setter 等代码“注入”到最终的 .class 文件中。   
所以，它们的关系是：Lombok 提供了这些注解，并在编译期将它们翻译成实际的 Java 构造函数代码。  

B. 与 MyBatis-Plus (MP) 的关系  

MP 强依赖 @NoArgsConstructor

当 MyBatis-Plus 从数据库查询数据并试图将其映射成一个 Java 对象时，它的工作流程是：  
1.通过反射机制，调用实体类的无参构造函数来创建一个空的实例。   
2.从 数据库返回的结果集 ResultSet 中逐个读取字段值。 通过反射调用相应字段的 setter 方法（或直接设置字段值）来填充这个空实例。  
结论：如果没有无参构造函数，MP 在第一步就会失败，抛出 NoSuchMethodException 或类似的实例化异常。   
因此，为所有 MP 的实体类提供一个 @NoArgsConstructor 是必须的。

@AllArgsConstructor 和 @RequiredArgsConstructor 为开发者服务   
MP 的内部工作流并不需要这两个构造函数。   
但是，它们为开发者在业务逻辑代码中创建和初始化实体对象提供了极大的便利。  
例如，当你要向数据库插入一条新记录时，使用 @AllArgsConstructor 可以非常简洁地创建一个完整的对象。

在开发中，对于 MyBatis-Plus 的实体类，最常见和推荐的组合是：
```java
@Data // 包含了 @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class User {
// ... 字段定义
}
```
###  8. @Data

@Data 是 Lombok 提供的一个“组合注解”，是一个极其方便的快捷方式。当你给一个类加上 @Data 注解时，Lombok 会在编译时自动为你生成以下几个注解的功能：   
@Getter：为所有非静态字段生成 get 方法。   
@Setter：为所有非 final 的非静态字段生成 set 方法。   
@ToString：生成一个 toString() 方法，输出类名和所有字段的值。   
@EqualsAndHashCode：生成 equals() 和 hashCode() 方法，默认会使用所有非静态、非瞬态（transient）的字段。   
@RequiredArgsConstructor：生成一个包含所有 final 字段和被 @NonNull 注解标记的字段的构造函数。


在编译后，Lombok 会自动生成类似下面的代码：
``` java
public class User {
    private final Long id;
    private String name;
    private int age;

    // 来自 @RequiredArgsConstructor
    public User(Long id) {
        this.id = id;
    }

    // 来自 @Getter
    public Long getId() { return this.id; }
    public String getName() { return this.name; }
    public int getAge() { return this.age; }

    // 来自 @Setter (注意：没有 final 字段 id 的 setter)
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }

    // 来自 @EqualsAndHashCode
    @Override
    public boolean equals(Object o) { ... }
    @Override
    public int hashCode() { ... }

    // 来自 @ToString
    @Override
    public String toString() { ... }
}
```

2. 为什么 @Data 不包含 @NoArgsConstructor？

既然像 MyBatis-Plus 这样的框架强依赖无参构造，为什么 Lombok 的 @Data 这么一个“全家桶”注解，却偏偏包含了 @RequiredArgsConstructor 而不是更常用的 @NoArgsConstructor 呢？

核心冲突：final 字段与无参构造函数

Java 语言规则：  
如果一个类中含有 final 字段，那么这个字段必须在对象创建时被初始化（要么在声明时赋值，要么在构造函数中赋值）。   
如果你没有定义任何构造函数，Java 编译器会为你生成一个默认的无参构造函数。   
但是，一旦你手动定义了任何一个构造函数，编译器就不会再为你生成默认的无参构造函数了。

@Data 的行为：
如果你的类中有 final 字段，@RequiredArgsConstructor 就会为你生成一个包含这些 final 字段的构造函数。

冲突产生：  
因为 @Data 已经为你生成了一个构造函数（RequiredArgsConstructor），根据 Java 的规则，编译器不会再自动添加无参构造函数。   
此时，如果 Lombok 的 @Data 强行再给你加上一个 @NoArgsConstructor，这个无参构造函数该如何处理那个 final 字段呢？它无法初始化 final 字段，这会导致一个编译错误。
```java
public class User {

    // 一个未在声明时初始化的 final 字段
    private final String username; 

    // 一个无参构造函数
    public User() {
        // 我被调用了，但我没有接收任何参数。
        // 我拿什么值去给那个必须被赋值的 final 字段 'username' 呢？
        // 我给不了！
        
        // 编译器检查到这里，发现构造函数即将执行完毕，
        // 但 'username' 字段还没有被赋值。
        // 这就违反了 final 的铁律！
    } 
    // 编译错误: The blank final field username may not have been initialized
}

```
最推荐的组合是在实体类上同时使用三个注解：@Data+@NoArgsConstructor+@AllArgsConstructor
    
### 9. ORM  (Object-Relational Mapping)

ORM 允许开发者用面向对象的思维来操作数据库，而无需直接编写繁琐、重复的 SQL 语句。
直接体现在DO中
```java
// 告诉 ORM，这个类对应数据库中的 'user' 表
@TableName("user")
public class User {

    // 告诉 ORM，这个 'id' 属性对应表的主键 'id' 列
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 告诉 ORM，这个 'userName' 属性对应表的 'user_name' 列
    @TableField("user_name")
    private String userName;

    // 属性名和列名一致时（忽略大小写），可以不写注解，ORM 会自动映射
    private Integer age;

    // 告诉 ORM，这个属性在 Java 对象中存在，但在数据库表中不存在，操作时请忽略它
    @TableField(exist = false)
    private String userRole; 
}

```
-----------------------------------------------------------------------------------
## 订单管理

处理用户通过微信下单的操作，支持自取和快递两种配送方式的选择，并记录完整订单流程。

### 1. totalAmount
Q1：为什么在计算订单总金额时使用AtomicReference？
```
AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
```
A：
1. 线程安全性：AtomicReference提供了线程安全的操作，在多线程环境下可以安全地更新totalAmount值
2. Lambda表达式限制：在Java中，lambda表达式内部只能访问final或等效final的变量，而AtomicReference允许我们在lambda内部修改其包装的值
3. 避免重复创建对象：通过set/get方法可以不断更新同一个引用中的值，而不需要创建新的BigDecimal对象

```
// 错误的代码 - 无法通过编译
BigDecimal totalAmount = BigDecimal.ZERO;
orderItems.forEach(item -> {
    BigDecimal subtotal = calculateSubtotal(item);
    // 编译错误: Variable used in lambda expression should be final or effectively final
    totalAmount = totalAmount.add(subtotal); 
});
```
AtomicReference 提供了一个完美的解决方案。它本身是一个 final 的对象（一个引用容器），但它内部包装的值是可以通过 set() 或 getAndSet() 等方法来修改的。
```
// 正确的代码 - 使用 AtomicReference 作为 final 的容器
final AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
orderItems.forEach(item -> {
    BigDecimal subtotal = calculateSubtotal(item);
    // 正确: 修改 AtomicReference 内部的值，而不是修改引用本身
    totalAmount.set(totalAmount.get().add(subtotal));
});
```

Q2：为什么使用BigDecimal而不是double或float来计算金额？   
A：精度问题：
- float/double是二进制浮点数，无法精确表示某些十进制小数（如0.1）
- BigDecimal提供任意精度的定点数，可以精确表示任何有理数
   ```java
   // 错误示例 - 使用double会有精度问题
   double result = 0.1 + 0.2; // 结果是0.30000000000000004
   
   // 正确示例 - 使用BigDecimal保证精度
   BigDecimal result = BigDecimal.valueOf(0.1).add(BigDecimal.valueOf(0.2)); // 结果是0.3
   ```

Q3：为什么在循环中计算总金额而不是数据库聚合查询？  
A：
1. 数据一致性：循环计算可以确保使用的是当前最新的商品价格和库存信息
2. 优惠活动应用：可能需要应用单品折扣、满减、优惠券等复杂规则。
3. 性能考虑：订单商品数量通常较少（一般几到几十个），循环计算性能影响很小
4. 错误处理：可以及时发现商品下架或库存不足等问题

Q4：如何优化订单金额计算的性能？   
A：   
1. 批量查询商品信息
2. 缓存热点商品：对经常购买的商品信息进行缓存
3. 并行计算：对多个商品的小计计算可以并行处理
4. 预计算优化：在下单前的商品详情页面就显示预估总价

Q5：如果订单商品数量巨大，如何优化计算？  
A：
- 分批处理：将大量商品分批处理，避免内存溢出
- 流式计算：使用Java 8 Stream API进行流式处理
- 异步计算：将金额计算放到异步任务中处理

### 2. 事务传播行为和隔离级别

传播行为常见场景：

- REQUIRED：如果当前存在事务就加入，否则新建（最常用）【要么凑桌，要么单开一桌】 ：绝大多数业务
- REQUIRES_NEW：新建事务，如果当前有事务则挂起【后到的vip只能单开一桌吃饭】 ：独立的辅助业务（如日志、审计）
- SUPPORTS：支持当前事务，没有则以非事务方式执行【只凑桌，否则自己吃】：大部分只读或不重要的操作

隔离级别应用场景：

- READ_COMMITTED：避免脏读，适用于大多数业务场景
- REPEATABLE_READ：避免不可重复读，适用于需要多次读取相同数据的场景
- SERIALIZABLE：最高隔离级别，完全避免并发问题但性能最差

**事务并发问题**  
关于三个并发问题我的理解：   
事务A所做的事：读写[commit]读   
1.脏读：B在A写完commit前读数据   
2.不可重复读：B在A写前读一次，在A写完commit后又读一次，两次结果不同   
3.幻读：B在A写前查一次，在A删完commit后又查一次，两次结果不同

幻读是因为在同一个事务同时存在快照读和当前读，数据又被修改了，读到的数据一个是之前版本的快照，一个是当前数据库的数据

1. 核心概念

* **快照读**：
    * 普通的 `SELECT` 语句（非加锁查询）。
    * 在 **可重复读** 隔离级别下，它读取的是事务开始时的**一致性视图**
      。这个视图是该事务启动时数据库的一个“快照”，无论其他事务如何修改并提交数据，这个快照在整个事务期间都保持不变。
    * **目的**：实现**非阻塞读**，保证可重复读。
* **当前读**：
    * 加锁的读操作，如 `SELECT ... FOR UPDATE`, `SELECT ... LOCK IN SHARE MODE`，以及 `UPDATE`, `DELETE`, `INSERT` 操作本身。
    * 它读取的是数据库的**最新提交版本**的数据，并且会对其读取的数据加上锁，以防止其他事务并发修改。
    * **目的**：保证在修改数据时，基于的是最新的、准确的数据状态。

2. 幻读产生的典型场景
   假设在 **可重复读** 隔离级别下，事务B执行以下操作：  
   1.**事务B开始**。   
   2.**快照读**：`SELECT * FROM users WHERE age > 20;` // 返回了10条记录。这次读取基于事务开始时的快照。   
   3.**此时，事务A插入了一条 `age=25` 的新记录并提交**。这条记录对于事务B的**快照读**是不可见的，因为它是在事务B之后创建的。
   4.**事务B执行当前读**：`SELECT * FROM users WHERE age > 20 FOR UPDATE;` // 准备修改这些记录。
    * 这个 `FOR UPDATE` 是**当前读**，它必须看到最新的、已提交的数据，以确保它锁住的是正确的、最新的数据集。
    * 于是，它看到了事务A刚刚插入的那条新记录（`age=25`）。
      5.**矛盾出现**：
    * 同一个事务B中，第一次的**快照读**返回10条记录。
    * 第二次的**当前读**返回11条记录。
    * 这个“多出来”的记录，就是幻影行。**幻读发生了**。

3. 为什么InnoDB的“可重复读”不能完全防止这种幻读？   
   InnoDB的MVCC通过**一致性读视图**完美解决了**快照读**的幻读问题。在同一个事务里，无论你进行多少次快照读，结果都是一致的。
   但是，当你混入**当前读**时，情况就变了。当前读**不受**该事务快照的约束，它必须去读取最新的数据并加锁，否则：

* `UPDATE` 会更新到错误的数据。
* `DELETE` 会删除错误的数据。
* `SELECT ... FOR UPDATE` 会锁住错误的数据集，导致后续的更新基于过时信息。
  解决方案：Next-Key Locks
  为了在 **可重复读** 级别下解决幻读，InnoDB引入了 **临键锁**。
* 当执行 `SELECT ... FOR UPDATE` 时，它不仅会锁住满足条件的**已有记录**（行锁），还会锁住这些记录之间的**间隙**（Gap Lock）。
* 在上面的例子中，当事务B执行 `SELECT ... FOR UPDATE` 时，它除了锁住那10条已有的记录，还会锁住 `age > 20`
  这个范围内的所有“间隙”。这样，事务A试图插入一条 `age=25` 的新记录时，会因为要插入的“位置”被间隙锁锁住而**阻塞等待**
  ，直到事务B提交。
* 这就保证了在事务B中，快照读和当前读看到的数据范围是一致的，从而彻底避免了幻读。

总结

1. **标准定义**：幻读是同一事务内两次查询**结果集**数量不同。
2. **技术根源（以InnoDB为例）**：幻读是由于在**可重复读**隔离级别下，同一个事务中混合使用了**快照读**（读取历史快照）和**当前读
   **（读取最新数据并加锁）导致的可见性差异。
3. **解决机制**：InnoDB通过 **Next-Key Locking（行锁+间隙锁）** 来阻止其他事务的插入操作，从而保证了即使在有当前读的情况下，也能避免幻读。

### 3. @Transactional(readOnly = true)

一个专门用于优化只读操作的事务注解配置。应该在任何确定只包含数据查询操作的Service方法上使用它。   
通过向底层的数据库和持久化框架（如JPA/Hibernate）提供一个明确的“提示，告知它们本次事务中不包含任何写入操作

主要从以下三个层面进行优化：

1. 数据库层面 (JDBC Driver)  
   
**读写分离路由：**  
   在配置了读写分离数据源的架构中，readOnly = true 是最重要的路由依据。  
   当Spring框架检测到这个标志时，它会指示数据源路由器将SQL查询发送到只读副本（Read Replica）数据库。  
   这极大地减轻了主数据库（Master）的压力，使其能专注于处理写请求，从而提升整个系统的吞吐量。   

   **禁止不必要的日志记录：**  
   数据库知道这个事务是只读的，因此可以跳过为该事务生成回滚日志（Undo/Redo Log）。这减少了磁盘I/O和CPU的开销。   

   **驱动级别的优化：**  
   某些JDBC驱动在接收到只读提示后，可能会执行一些内部优化，例如设置更合适的抓取大小（Fetch Size）或调整网络包的协议。
2. 持久化框架层面 (JPA/Hibernate)  
   这是最显著的优化点之一，尤其是在使用Hibernate时。    

**关闭脏检查（Dirty Checking）：**  
在标准的读写事务中，Hibernate会将从数据库加载的实体（Entity）放入一级缓存（Session Cache）中，并保留一个原始状态的快照。   
当事务提交时，Hibernate必须遍历缓存中的所有实体，将它们的当前状态与快照进行比较，以找出被修改过的“脏”数据，然后生成UPDATE语句同步到数据库。这个过程称为脏检查。   
当设置了 readOnly = true时，Hibernate会完全跳过脏检查这个步骤。对于加载了大量实体的事务，这可以节省大量的CPU时间和内存，避免了成百上千次的对象比较。  

   **设置Flush模式：**  
   Hibernate会将 FlushMode 设置为 MANUAL 或 NEVER。  
   这意味着即使你在代码中意外地修改了实体对象的状态，Hibernate也不会在事务提交时将这些变更刷新（flush）到数据库。这不仅提升了性能，也从侧面保证了只读事务的“只读”特性。
3. Spring框架层面  
   Spring本身不直接执行优化，但它扮演着至关重要的“指挥官”角色。  
   它负责解析 @Transactional 注解，并将 readOnly 这个标志正确地传递给底层的事务管理器（Transaction
   Manager）、数据源（DataSource）和JPA提供者（JPA Provider）。

注意事项与常见误区   
它是一个“君子协定”：而不是一个强制约束。如果你在标记为只读的事务中尝试执行写操作，其结果取决于你的持久化提供者和数据库驱动。大多数情况下（如使用Hibernate），会在事务提交时抛出异常，但并非所有情况都如此。   
方法调用链：如果一个读写方法（readOnly=false）调用了一个只读方法（readOnly=true），并且传播级别为
REQUIRED，那么只读方法会加入到已存在的读写事务中，readOnly=true 的设置会被忽略。事务的读写属性由最外层的事务发起者决定。   
不仅仅是SELECT：只要是不改变数据的操作，都可以认为是只读的。例如，调用数据库的只读存储过程。

### 4. 乐观锁+RocketMQ实现一致性：

该方案实现了订单服务与库存服务的解耦，通过异步消息确保最终一致性。
订单创建流程：
1. 验证订单参数和商品信息
2. 创建订单记录和订单详情
3. 发送库存扣减消息到RocketMQ，订单状态为"待支付"
4. 立即返回订单号给用户

库存扣减流程（RocketMQ消费者）：
1. 接收库存扣减消息
2. 使用乐观锁机制更新商品库存（通过version字段确保并发安全）
3. 扣减成功：订单继续后续流程
4. 扣减失败：抛出异常触发RocketMQ消息重试机制
5. 重试次数达到上限仍未成功：执行库存回滚机制，更新订单状态为"已取消"

这种设计的优势：
1. 提升用户体验：用户下单后无需等待库存扣减完成
2. 系统解耦：订单服务不直接依赖库存服务
3. 高并发支持：通过乐观锁避免库存超卖
4. 数据一致性保障：通过消息重试和回滚机制确保数据最终一致
5. 容错性：即使某个环节失败，也能通过补偿机制恢复

### 5. 读写分离一致性保障机制： 
1. 创建了动态数据源路由类：就像一个智能的交通警察，根据不同的情况指挥数据流向不同的数据库
2. 实现了数据源上下文持有者：就像一个记录本，记录当前应该使用哪个数据库
3. 创建了数据源注解和切面：就像标签和自动分拣机，通过标签自动把不同的操作分发到对应的数据库在OrderServiceImpl的关键方法上添加了数据源注解：
   好的，您对这三个核心组件的比喻非常生动且准确。这套组合是实现数据库读写分离和动态数据源切换的经典设计模式。下面，我将基于您的比喻，对这三个组件进行更详细、更深入的讲解。

1. 数据源上下文持有者 (DataSourceContextHolder)：那个“记录本”

在当前线程中安全地记录和传递“应该使用哪个数据源”这个决定。  
核心技术：ThreadLocal

```java
public class DataSourceContextHolder {
    // 使用 ThreadLocal 来存储数据源的键（例如 "master", "slave1"）
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    public static final String MASTER = "master";
    public static final String SLAVE = "slave";

    /**
     * 设置当前线程要使用的数据源类型
     * @param dataSourceType 数据源类型
     */
    public static void setDataSourceType(String dataSourceType) {
        CONTEXT_HOLDER.set(dataSourceType);
    }

    /**
     * 获取当前线程正在使用的数据源类型
     * @return 数据源类型
     */
    public static String getDataSourceType() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除数据源类型
     */
    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }
}
```
2. 数据源注解和切面 (@DataSource & DataSourceAspect)：那套“标签和自动分拣机”

A. 数据源注解 (@DataSource)：“标签”   
标记 Service 层的方法，声明这个方法应该使用哪个数据源。

```java
/**
* 数据源注解
* 用于标记方法使用主库还是从库
  */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface DataSource {

  String value() default DataSourceContextHolder.MASTER;// 默认使用主库
  }
```
在查询方法上添加： @DataSource(DataSourceContextHolder.SLAVE)  

B. 数据源切面 (DataSourceAspect)：“自动分拣机”   
在方法执行之前拦截到这个方法，检查上面有没有贴 @DataSource 标签，然后根据标签内容，去操作那个“记录本”(DataSourceContextHolder)。

3. 动态数据源路由类 (DynamicDataSource)：那位“智能的交通警察”

这是最终执行数据源切换的组件。它本身也是一个 DataSource，供 Spring 框架使用。但它很特殊，它内部管理了多个真实的数据源（主库、从库等），并且懂得如何根据“记录本”的内容来选择一个正确的数据源。  
核心技术：继承 AbstractRoutingDataSource

Spring 提供了一个抽象类 AbstractRoutingDataSource，它就是专门为这种“动态路由”场景设计的。我们只需要继承它，并实现一个核心方法 determineCurrentLookupKey()。

determineCurrentLookupKey() 的作用：每当框架需要一个数据库连接时，它就会调用这个方法来询问：“我这次应该用哪个数据源？”   

就一句：return DataSourceContextHolder.getDataSourceType();

配置：在 Spring 的配置类中，你需要创建主库、从库的 DataSource Bean，然后将它们都注册到 DynamicDataSource 中。
```java
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource masterDataSource() {
        // ... 创建并配置主数据源
    }

    @Bean
    public DataSource slaveDataSource() {
        // ... 创建并配置从数据源
    }

    @Bean
    @Primary // 将动态数据源设置为主数据源
    public DynamicDataSource dataSource(DataSource masterDataSource, DataSource slaveDataSource) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", masterDataSource);
        targetDataSources.put("slave", slaveDataSource);
        
        // 设置所有目标数据源
        dynamicDataSource.setTargetDataSources(targetDataSources);
        // 设置默认数据源（当记录本中没有指定时使用）
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource);
        
        return dynamicDataSource;
    }
}
```
过程：  
HTTP 请求到达，调用了 userService.getUserById(1L) 方法。   
“自动分拣机” (DataSourceAspect) 拦截到这个调用，发现方法上有 @DataSource("slave") 这个“标签”。  
分拣机立即在“记录本” (DataSourceContextHolder)中为当前线程记下：“使用 slave”。  
业务方法 getUserById 开始执行，它内部会调用 Mapper 进行数据库查询。   
Spring 框架需要获取数据库连接，于是向“交通警察” (DynamicDataSource) 发出请求。   
交通警察调用 determineCurrentLookupKey() 方法，它去翻阅当前线程的“记录本”，发现上面写着 slave。   
交通警察根据 slave 这个键，从它管理的多个数据源中，找到了从库的数据源，并返回一个从库的连接。   
数据库查询在从库上成功执行。  
getUserById 方法执行完毕，控制权返回到“自动分拣机”的 finally 代码块。  
分拣机调用 DataSourceContextHolder.clear()，将“记录本”上为本次请求做的记录擦除干净，线程被干净地回收到线程池中。


### 6. 总结在分布式系统中常见的几种数据一致性保障策略

数据一致性是指在分布式系统中，多个数据副本在同一时刻是否具有相同的值。

1. 强一致性策略   
   a. 两阶段提交 (2PC - Two-Phase Commit)  
   核心思想: 引入一个“协调者”来统一管理所有“参与者”的事务提交。   
   阶段一 (Prepare): 协调者询问所有参与者是否可以执行事务，参与者执行事务但不提交，并锁定资源。   
   阶段二 (Commit/Abort): 如果所有参与者都回复“可以”，协调者就通知所有参与者commit；否则，通知所有参与者回滚。   
   优点: 原理简单，实现了数据的强一致性。   
   缺点: 同步阻塞，性能差；协调者存在单点故障；在第二阶段，如果协调者宕机，参与者会一直锁定资源。  

   b. 三阶段提交 (3PC - Three-Phase Commit)  
   核心思想: 在2PC的基础上增加了一个“CanCommit”阶段，并引入超时机制，以解决2PC的阻塞问题。   
   优点: 相比2PC，降低了阻塞的风险。   
   缺点: 依然无法完全避免数据不一致，且协议更复杂，性能更低。   

   c. Paxos / Raft 算法   
   核心思想:
   一种基于“共识”的算法，通过“投票”机制让分布式系统中的多个节点对某个值达成一致。Raft是Paxos的一个更易于理解和实现的工程变体。   
   优点: 保证了强一致性，且相比2PC/3PC有更好的可用性和容错性。   
   缺点: 算法复杂，实现难度高，性能开销较大。   
   适用场景: 分布式锁（如Zookeeper、Etcd）、分布式数据库（如TiDB）的底层实现。


2. 最终一致性策略   
   a. RocketMQ/Kafka 事务消息   
   核心思想: 利用消息队列的“事务消息”或“半消息”机制，确保本地事务的执行与消息的发送这两个操作成为一个原子单元。   
   实现方式 (以RocketMQ为例):  
   发送半消息(Prepare Message): 生产者先向MQ Server发送一条“半消息”，该消息对消费者不可见。   
   执行本地事务: 生产者执行本地数据库操作。   
   提交/回滚消息:  
   如果本地事务成功，生产者向MQ Server发送COMMIT，MQ将消息标记为可投递，消费者可以消费。   
   如果本地事务失败，生产者向MQ Server发送ROLLBACK，MQ将删除该半消息。   
   状态回查: 如果生产者在第二步后宕机，MQ
   Server会定期向生产者集群回查该消息对应的本地事务状态，以决定是COMMIT还是ROLLBACK。   
   优点: 将分布式事务解耦，实现了业务的最终一致性，系统吞吐量高。   
   缺点: 依赖消息中间件的可靠性；对业务的侵入性较低但仍需改造。   
   适用场景: 跨服务的异步业务通知，如用户注册后发送欢迎邮件、创建订单后通知库存系统等。

   b. TCC (Try-Confirm-Cancel) 模式    
   优点: 性能较高（无长期资源锁定），业务层面的原子性，不依赖底层数据库的事务支持。   
   缺点: 对业务代码侵入性强，需要为每个操作实现Try-Confirm-Cancel三个接口，开发成本高。   
   适用场景: 对一致性要求较高，且流程较长的分布式业务，如复杂的金融、电商下单流程。   

   c. Saga 模式  
   核心思想:
   将一个长事务拆分为多个本地事务，每个本地事务都有一个对应的补偿操作。当Saga中的某个本地事务失败时，会依次调用前面已成功事务的补偿操作来回滚。  
   优点: 无长期资源锁定，适合长流程业务，各服务耦合度低。  
   缺点: 不保证隔离性（可能看到中间状态的数据），补偿逻辑设计复杂。  
   适用场景: 业务流程长、需要保证最终一致性的场景，如机票+酒店+租车预订服务。


3. 兜底与辅助策略  

   a. 定时任务补偿机制  
   核心思想: 通过定时任务（如XXL-Job, Quartz）定期扫描数据库中的中间状态或不一致的数据，并执行修复或重试逻辑。  
   实现方式:
   在数据表中增加一个状态字段（如status: 0-处理中, 1-成功, 2-失败）和更新时间字段。  
   定时任务扫描那些长时间处于“处理中”状态的记录。  
   查询关联系统的状态，根据查询结果修复当前记录的状态。  
   优点: 实现简单，健壮可靠，是最终一致性方案的完美兜底。  
   缺点: 非实时，数据不一致会存在一个时间窗口，可能对数据库造成周期性压力。  
   适用场景: 作为所有异步或分布式事务方案的最后一道防线。  

   b. 数据校验机制  
   核心思想: 在关键操作前后，或定期对数据进行比对和校验，主动发现不一致。  
   事前校验: 如使用乐观锁。  
   事后校验: 定期或在业务流程结束后，比对源系统和目标系统的数据，生成对账单，发现差异后进行人工或自动修复。  
   优点: 能主动发现问题，特别是乐观锁能有效防止并发冲突。  
   缺点: 对账通常有延迟，且需要额外的开发和计算资源。
   适用场景: 财务系统、订单与库存对账等。

   c.重试机制 (Retry Mechanism)  
   重试机制是一种容错策略，用于应对分布式系统中的瞬时故障（如网络抖动、服务临时不可用、数据库死锁等），通过重复执行失败的操作来期望最终成功，从而保障最终一致性。  
   核心思想:
   当一个操作失败时，不立即判定为最终失败，而是等待一小段时间后再次尝试。通常会结合退避策略（如指数退避，即每次重试的等待时间逐渐变长）和重试次数限制。  
   关键前提：幂等性 (Idempotency)
   重试机制必须应用于幂等的操作上。如果操作不幂等（如“给用户账户加10元”），重试会导致数据错误（重复加钱）。  
   保证幂等性的方法：为每次请求生成唯一的请求ID，在服务端记录已处理的请求ID，后续重复请求直接返回成功结果。   
   实现方式:  
   代码层面: 使用 AOP 框架（如 Spring Retry）或第三方库（如 Guava Retrying）为方法添加重试逻辑。  
   消息队列: MQ 的消费者消费失败后，MQ 会自动进行重试投递。
   任务调度: 定时任务补偿本身就是一种宏观的重试机制。


4. 架构层面的保障策略
   a. 读写分离一致性保障  
   问题: 主从数据库之间存在复制延迟，导致刚在主库写入的数据，立即去从库读可能读不到。  
   保障策略:
   强制读主库: 对于一致性要求高的读请求（如用户刚修改完个人信息后立即查看），直接路由到主库读取。  
   延迟双删/缓存更新: 在写入主库后，先淘汰缓存，等待一个主从延迟的时间（如1秒），再次淘汰缓存，确保缓存中的脏数据被清除。  
   半同步复制: 配置MySQL等数据库的主从复制为半同步模式，确保事务日志至少被一个从库接收后，主库才向客户端返回成功。  

   b. 缓存一致性保障
   Cache Aside Pattern (旁路缓存):
   最常用模式。读操作先读缓存，缓存未命中则读数据库，再将数据写入缓存。写操作先更新数据库，然后删除缓存。  
   Read/Write Through (读/写穿透):   
   由缓存服务自身负责与数据库的同步，应用层只与缓存交互。  
   Write Back (回写):  
   写操作只更新缓存，缓存定期批量将数据刷回数据库。  
   订阅Binlog:
   通过Canal等工具订阅数据库的binlog，当数据发生变更时，由订阅程序自动更新或删除对应的缓存。这是目前较为推荐的自动化方案。  
  
|    策略名称    | 一致性级别 | 核心思想                  | 复杂度 | 典型场景                      | 
|:----------:|:-----:|-----------------------|-----|---------------------------|
|  2PC/3PC   | 强一致性  | 协调者统一调度，同步阻塞          | 高   | 单体应用跨库事务 (已较少用)           | 
| Paxos/Raft | 强一致性  | 多数派投票达成共识             | 极高  | 分布式组件底层 (Zookeeper, Etcd) |
|    事务消息    | 最终一致性 | 本地事务与消息发送原子绑定         | 中   | 跨服务异步通知、解耦                |
|    TCC     | 最终一致性 | Try-Confirm-Cancel 补偿 | 高   | 核心交易链路、支付流程               |
|    Saga    | 最终一致性 | 长事务拆分 + 补偿            | 高   | 业务流程长、需要回滚的场景             |
|   定时任务补偿   | 最终一致性 | 定期扫描和修复兜底             | 低   | 所有异步方案的最终保障               |
|    数据校验    | 辅助保障  | 事前预防(乐观锁)或事后对账        | 中   | 并发更新、财务对账                 |
|    乐观锁     | 辅助保障  | 通过版本号机制检测并发冲突，防止数据覆盖  | 中   | 高并发更新（库存、余额）、数据校验         |
|    重试机制    | 辅助保障  | 应对瞬时故障，通过重复执行保证操作最终成功 | 中   | RPC调用、MQ消费、幂等操作           |
|   读写分离策略   | 架构保障  | 解决主从延迟问题              | 中   | 读多写少的系统架构                 |
|  缓存一致性策略   | 架构保障  | 保证缓存与DB数据同步           | 中   | 高性能、高并发系统                 |

### 7. 深度分页问题
传统分页应该是按页码查找，游标分页是按书签查找。因此传统慢，游标只能查找下一页或上一页

游标分页（Cursor Pagination）
1. 传统分页的困境：Offset/Limit 分页
```sql
SELECT * FROM products ORDER BY id ASC LIMIT 10 OFFSET 10000;
```
这条SQL语句的含义是：跳过前10000条记录，然后返回接下来的10条记录。

问题：   
性能瓶颈： 当 OFFSET 值很大时，数据库仍然需要扫描或读取前面 OFFSET 数量的记录，然后丢弃它们，最后才返回 LIMIT 数量的记录。即使有索引，数据库也可能需要遍历索引树的大部分节点。  
数据一致性问题： 在高并发写操作的场景下，如果在用户请求第N页和第N+1页之间，有新的数据插入或删除，那么后续页面的数据可能会发生漂移。

2. 游标分页（Cursor Pagination）的原理  
游标分页的核心思想是：不使用偏移量（OFFSET）来跳过记录，而是使用上一页最后一条记录的某个唯一且有序的字段值（即“游标”）来定位下一页的起始位置。

工作流程：
1.  初始请求（第一页）：
    *   客户端首次请求数据时，不带游标。
    *   服务器执行查询，通常会根据一个或多个唯一且有序的字段进行排序，并限制返回的数量。
        ```sql
        SELECT id, name, created_at FROM orders
        ORDER BY created_at ASC, id ASC -- 必须有排序字段，且最好是唯一或组合唯一
        LIMIT 10;
        ```
    *   服务器返回第一页的10条数据。同时，它会从这10条数据中，取出**最后一条记录的排序字段值**（例如 `created_at` 和 `id`），将其编码成一个“游标”字符串，并返回给客户端。

2.  后续请求（下一页）：
    *   客户端收到第一页数据和游标后，如果需要加载下一页，它会将这个游标发送给服务器。
    *   服务器解析游标，获取到上一页最后一条记录的排序字段值（例如 `last_created_at`, `last_id`）。
    *   服务器执行查询，条件是排序字段值大于（或小于，取决于排序方向）上一个游标的值。
        ```sql
        SELECT id, name, created_at FROM orders
        WHERE (created_at > 'last_created_at' OR (created_at = 'last_created_at' AND id > 'last_id'))
        ORDER BY created_at ASC, id ASC
        LIMIT 10;
        ```
        *   **解释 `WHERE` 子句：**
            *   `created_at > 'last_created_at'`：这是主要的条件，查找所有创建时间晚于上一页最后一条记录的订单。
            *   `OR (created_at = 'last_created_at' AND id > 'last_id')`：这是一个“tie-breaker”（打破平局）条件。如果有多条记录具有相同的 `created_at` 值，我们就需要用 `id` 字段来进一步排序和定位，确保顺序的唯一性。`id` 通常是主键，保证了唯一性。
    *   服务器返回下一页的10条数据，并生成新的游标。

3. 游标的构成与编码

*   游标内容： 游标通常包含用于排序和定位的字段值。例如，如果 `ORDER BY created_at ASC, id ASC`，那么游标可能就是 `(created_at, id)` 的组合。
*   编码： 为了安全和简洁，游标通常会被编码成一个不透明的字符串，例如使用 Base64 编码。这可以防止客户端篡改游标内容，也方便在URL或HTTP头中传递。

4. 游标分页的优势

*   高性能：
    *   避免全表扫描
    *   固定查询成本
*   数据一致性：
    *   由于是基于实际数据值进行定位，即使在分页过程中有新的数据插入或删除，也不会导致页面数据错乱。新插入的数据会自然地出现在后续页面，删除的数据则会消失。

5. 游标分页的缺点与限制

*   无法直接跳转到任意页： 游标分页只能“下一页”或“上一页”（如果设计了反向游标）。你不能直接跳到第50页，因为没有“页码”的概念。
*   必须有稳定的排序字段： 需要至少一个或一组唯一且有序的字段作为游标。如果排序字段不唯一，需要额外的“tie-breaker”字段（如主键）来保证游标的唯一性。
*   复杂性略高： 客户端需要管理游标，而不是简单的页码。服务器端也需要处理游标的编码/解码和复杂的 `WHERE` 子句。
*   “上一页”实现： 实现“上一页”功能比“下一页”稍微复杂一些。通常需要：
    1.  在客户端存储前一个游标。
    2.  在服务器端反转 `ORDER BY` 顺序，并使用 `<` 而不是 `>` 进行条件判断。
    3.  获取结果后，再将结果集反转，以保持页面内的顺序。

 6. 适用场景

*   无限滚动（Infinite Scroll）： 当用户向下滚动页面时，自动加载更多内容，非常适合游标分页。
*   API设计： 许多RESTful API在返回大量数据时，会采用游标分页来提高性能和稳定性。
*   日志、消息流等时间序列数据： 这类数据通常按时间排序，天然适合使用时间戳作为游标。
*   后台管理系统中的数据导出： 需要一次性或分批导出大量数据时，游标分页可以确保高效。

### 8. SQL注入防护优化方案
a. 使用MyBatis的@Param注解进行参数绑定：  
用 @Param 给参数命名后，MyBatis 就能更清晰地知道哪个值对应SQL中的哪个占位符（例如 # {username}），方便后续的预编译机制。   

b. 使用#{}占位符而不是\${}字符串拼接   
#{} 会告诉 MyBatis，这里是一个参数，而不是SQL代码的一部分。MyBatis 在执行SQL之前，会用预编译语句来处理它。PreparedStatement 会将SQL语句和参数分开，先编译SQL模板，然后再安全地将参数值填充进去。这样，无论参数值是什么（即使包含了恶意SQL代码），它都只会被当作数据来处理，而不会改变SQL语句本身的结构。
${} 则是直接将变量的值 原封不动地拼接到SQL字符串中。如果变量的值包含恶意SQL代码，这些代码就会直接成为SQL语句的一部分，从而引发SQL注入。

c. 使用MyBatis Plus提供的LambdaQueryWrapper，它会自动处理参数绑定   
它允许你通过链式调用的方式，以类型安全的方式构建复杂的查询条件，而无需手写SQL。   
当你调用 eq(), like(), ge() 等方法并传入参数时，MyBatis Plus 会自动将这些参数作为 安全的预编译参数 传递给数据库

d. 创建了SqlInjectionUtil工具类，用于检测和过滤潜在的SQL注入字符   
检查用户输入字符串中是否含有常见的SQL注入攻击特征字符（如单引号 '、双引号 "、分号 ;、--、OR、AND 等），或者对这些字符进行转义、过滤。

e. 在Service层和Controller层都增加了参数安全性检查

### 9. 敏感数据脱敏方案
1. 注解驱动的脱敏机制
   创建了@SensitiveData注解，可以标记需要脱敏的字段
```java
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveData {
    SensitiveType value() default SensitiveType.NONE;
    Class<? extends SensitiveStrategy> strategy() default DefaultSensitiveStrategy.class;
}
```
   SensitiveType 枚举定义了不同的脱敏类型，如价格、金额等
   实现了可扩展的脱敏策略接口
2. 透明的脱敏处理
   通过工具类可以方便地对对象进行脱敏处理
   不改变原有业务逻辑，只需在需要脱敏的地方调用工具类
   提供了专门的脱敏响应结果类，便于前端识别
3. 普通用户返回脱敏数据，授权用户返回完整数据

### 10. 接口幂等性设计
1. 注解驱动的幂等性机制
   创建了@Idempotent注解，可以标记需要幂等性保障的接口
   定义了多种幂等标识获取策略，如从请求参数、请求头、Token等获取
   支持自定义过期时间和过期单位
2. 基于Redis的原子性操作
   使用Redis的SETNX命令保证幂等性检查的原子性
   支持设置过期时间，避免内存泄漏
   提供了幂等标识的清理机制
3. AOP切面统一处理幂等性检查
4. 工作流程
   当请求到达被 @Idempotent 注解标记的方法时，AOP切面会首先拦截请求。
   根据配置的策略生成幂等标识。
   使用Redis的原子操作检查该标识是否已存在。
   如果标识不存在，则设置标识并执行业务逻辑。
   如果标识已存在，则根据配置决定是抛出异常还是返回默认值。

## 优惠券管理

提供优惠券的发放、使用规则配置、有效期管理等功能，增强营销活动灵活性。

## 限购与预售管理

对特定商品设置购买数量上限，同时支持早鸟票预售时间及价格策略设定。

## 促销活动管理

支持限时优惠活动的定时开启与关闭，配合Spring Task实现自动化任务调度。

## 缓存管理

利用Redis缓存热点数据提升系统性能，结合Spring Cache实现本地多级缓存机制。
多级缓存架构（本地缓存+Caffeine+Redis）
- 本地缓存（Caffeine）：第一级缓存，速度最快，存储最热的数据
- Redis缓存：第二级缓存，分布式共享，容量大
- 数据库：最终数据源，缓存失效时从这里获取数据
```
// 多级缓存访问顺序：本地缓存 -> Redis -> 数据库
ProductDO product = (ProductDO) localCache.getIfPresent(cacheKey);
if (product != null) return product;

String productJson = redisTemplate.opsForValue().get(cacheKey);
// ...
product = productMapper.selectById(productId);

```
缓存问题解决方案
- 缓存穿透：对空值也进行缓存，但设置较短的过期时间
- 缓存击穿：使用分布式锁保证同一时间只有一个请求去加载数据
- 缓存雪崩：设置不同的过期时间，避免大量缓存同时失效
缓存更新策略（Cache Aside、Read/Write Through等）
- 采用Cache Aside模式：先更新数据库，再删除缓存
- 提供专门的缓存预热接口和定时任务
缓存预热和失效机制
- 提供手动预热接口，可根据需要预热热点数据
- 定时任务自动预热热门商品数据
- 合理设置缓存过期时间，避免内存泄漏

## 消息通知管理

基于RocketMQ异步处理订单状态变更通知、日志记录等后台任务，提高响应效率。

## 实时通信管理

通过WebSocket实现实时订单状态推送以及客服在线聊天功能，增强用户体验。

## 文件存储管理

集成阿里云OSS实现图片及文档类资源的安全上传与管理，保障多媒体内容存储稳定。

## 数据导入导出管理

基于POI组件提供Excel格式的数据批量导入与导出能力，方便运营人员操作。

## 外部接口调用管理

使用HttpClient发起对外部系统的HTTP请求，用于对接第三方服务如支付网关或物流平台。

## 数据库访问管理

采用MyBatis框架进行MySQL数据库的CRUD操作，并结合PageHelper实现数据分页查询功能。
