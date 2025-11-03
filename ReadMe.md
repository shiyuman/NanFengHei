# 南峰黑私房手作系统
# 目录--设计接口+知识点
## 用户管理
实现用户的注册、登录功能，集成JWT进行身份认证与权限控制，确保系统安全访问。

**核心逻辑**
1. 用户注册  

首先用户传入registerDTO[用户名，密码，手机号]，   
然后UserService进行注册，并给用户分配ID,创建用户对象（加密密码）。  
若库中无重名数据则注册成功，返回data[用户ID:name]到前端；
2. 用户登录  

用户传入UserLoginDTO[name,password]进login  

login:1.查找username；2.验证password 3.生成token并返回  
jwtUtil.generateToken(userDO.getUsername()):  
1.创建当前时间和过期时间（当前时间+配置的过期秒数）   
2.使用JJWT库构建JWT token：设置主题(subject)为传入的用户标识+设置签发时间+设置过期时间+使用HS512算法和密钥签名  
3.JWT身份认证  

3. 在JWT中，Token由三部分组成，用点(.)分隔：

Header（头部）：由JJWT库自动创建,包含算法信息：{"alg": "HS512", "typ": "JWT"}   
Payload（载荷）：sub (subject)：用户名+iat (issued at)：签发时间+exp (expiration)  
Signature（签名）：   
由JJWT库使用HS512算法生成   
通过对Header和Payload的Base64编码字符串，加上密钥(jwtConfig.getSecret())进行签名
4. JWT配置管理 

JWT配置类的作用：   
读取配置属性：

从application.yml或application.properties文件中读取JWT相关配置  
使用@ConfigurationProperties(prefix = "jwt")注解绑定配置

存储配置参数：

secret：JWT签名密钥，用于token的签名和验证   
expiration：token过期时间（秒），控制token的有效期   
提供getter/setter方法：允许其他类获取和设置JWT配置参数
## 商品管理
支持商品的上架、下架、分类管理及库存控制，满足商品信息维护和展示需求。

**核心代码：**  

1.**创建工作簿和工作表**：

    Workbook workbook = new XSSFWorkbook();  
    Sheet sheet = workbook.createSheet("商品数据");
   
首先创建了一个新的Excel工作簿（`Workbook`），然后在其中创建了一个名为“商品数据”的工作表（`Sheet`）。  
`XSSFWorkbook`是Apache POI库中用于处理.xlsx格式文件的类。

2.**设置列标题行**：

    Row headerRow = sheet.createRow(0);   
    headerRow.createCell(0).setCellValue("商品ID");   
    headerRow.createCell(1).setCellValue("商品名称");   
    
在工作表的第一行（即索引为0的行）创建了一个标题行（`Row headerRow`），并在该行的各个单元格（`Cell`）中分别设置了列标题  

3.**创建日期格式化对象**：   

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   

如果移除日期格式化的功能，那么在Excel表格中显示的创建时间将会是原始的Date对象toString()的结果，通常是类似"Mon Nov 03 15:30:45 CST 2025"，而不是"2025-11-03 15:30:45"。   

4.**填充商品数据**：

    int rowNum = 1;
    for (ProductDO product : productList) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(product.getId());
        row.createCell(1).setCellValue(product.getName());
        if (product.getCreateTime() != null) {
            row.createCell(7).setCellValue(sdf.format(product.getCreateTime()));
        }
    }
   对于每个商品，代码会在工作表中创建一个新的行（`Row`），并从该行的索引1开始为每一列填充数据。  

5.**设置HTTP响应头**：
1. `response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");`  
设置响应的内容类型为Excel文件格式（xlsx）。浏览器会根据这个MIME类型识别这是一个Excel文件。

2. `ContentDisposition contentDisposition = ContentDisposition.attachment()
        .filename("商品数据.xlsx", StandardCharsets.UTF_8)
        .build();`  
使用Spring框架的ContentDisposition工具类创建了一个附件类型的Content-Disposition头部信息，并指定了文件名为"商品数据.xlsx"，同时使用UTF-8字符集编码以支持中文文件名。

3. `response.setHeader("Content-Disposition", contentDisposition.toString());`  
将上面构建好的Content-Disposition对象转换成字符串并设置到HTTP响应头中。这告诉浏览器应该将响应内容作为附件下载，并提示默认文件名。

4. `workbook.write(response.getOutputStream());`  
将Apache POI创建的Excel工作簿直接写入HTTP响应的输出流中，这样客户端就可以接收到完整的Excel文件内容。

整个过程就是：
- 告诉浏览器返回的是一个Excel文件
- 告诉浏览器应该如何处理这个文件（作为附件下载）
- 把实际的Excel文件内容发送给浏览器

6.**关闭工作簿**：
    workbook.close();

知识点总结：
1. Apache POI库   

Q1：读取 Excel 的典型步骤？
-  通过 WorkbookFactory.create(inputStream) 创建 Workbook
-  遍历 Sheet → Row → Cell
-  使用 DataFormatter 格式化单元格值（避免类型错误）  

Q2：常见异常处理
-  EncryptedDocumentException：文件加密时抛出，需解密后读取
-  InvalidFormatException：文件格式不匹配（如用 XSSF 读取 .xls 文件）  

Q3: 性能优化技巧   
- 避免频繁创建对象：复用 CellStyle 和 Font   
- 批量写入数据：使用 sheet.flushRows() 控制内存   
- 关闭资源：确保 workbook.close() 和 inputStream.close()   
- 使用事件模型：对于只读场景，采用 SAX 解析（如 XSSF and SAX (Event API)）

2. 各种判空方式的总结对比表：

| 方法 | 用途 | 是否检查null | 是否检查空字符串 | 是否检查空白字符 | 需要先决条件 |
|------|------|------|-----------|----------|--------|
| `== null` | 检查对象是否为null | ✅  | ❌  | ❌ |  |
| `!= null` | 检查对象是否存在 | ✅  | ❌  | ❌ |  |
| `isEmpty()` | 检查字符串长度是否为0 | ❌  | ✅  | ❌ | 对象不能为null |
| `trim().isEmpty()` | 检查去除空白后是否为空 | ❌  | ✅  | ✅ (间接) | 对象不能为null |
| `isBlank()` (Apache Commons) | 检查是否为空或仅含空白字符 | ✅  | ✅  | ✅  |  |
| `Objects.isNull()` | 检查对象是否为null(JDK 8+) | ✅  | ❌  | ❌  |  |
| `Objects.nonNull()` | 检查对象是否存在(JDK 8+) | ✅  | ❌  | ❌  |  |

注意事项：
- `isEmpty()`和`trim()`都需要对象不为null，否则会抛出`NullPointerException`
- Apache Commons Lang的`isBlank()`是最全面的方法，但需要额外依赖
- 在实际开发中，通常组合使用`== null || isEmpty()`来保证安全  

3. MyBatis Plus高级特性知识点总结

1.实现MetaObjectHandler自动填充创建时间和更新时间等字段

MetaObjectHandler是MyBatis Plus提供的元数据对象处理器，用于自动处理字段的填充操作，无需在业务代码中手动设置创建时间、更新时间等公共字段。

```    
public void insertFill(MetaObject metaObject) {
        //从要插入的对象中获取名为"createTime"的字段值
        Object createTime = getFieldValByName("createTime", metaObject);
        if (createTime == null) {
            //参数分别是：要操作的对象，要填充的字段名，字段的数据类型，具体的值
            this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        }
```
**实现要点**：
- 创建自定义MetaObjectHandler实现类，实现insertFill和updateFill方法
- 在实体类字段上使用@TableField(fill = FieldFill.INSERT/UPDATE/INSERT_UPDATE)注解标记需要自动填充的字段
- INSERT表示插入时填充，UPDATE表示更新时填充，INSERT_UPDATE表示插入和更新都填充

**面试重点**  
Q1：为什么要使用自动填充？   
A：避免在每个业务方法中重复设置公共字段（如创建时间、更新时间、操作人等），减少样板代码，统一管理公共字段的赋值逻辑，降低维护成本。

Q2：自动填充是如何实现的？  
A：通过MetaObjectHandler接口，在MyBatis执行SQL前拦截，通过反射机制获取并设置指定字段的值。分为insertFill（插入填充）和updateFill（更新填充）两个时机。

Q3：如何自定义自动填充逻辑？   
A：继承MetaObjectHandler类，重写insertFill和updateFill方法，在方法中通过getFieldValByName判断字段是否已设置，使用strictInsertFill或strictUpdateFill方法设置字段值。

2.添加乐观锁机制防止并发更新冲突

乐观锁是一种并发控制机制，假设数据一般不会发生冲突，只在提交更新时检查是否违反了并发控制规则。  
MyBatis Plus通过version字段实现乐观锁机制。

**实现要点**
- 在实体类中添加@Version注解的version字段
- 配置OptimisticLockerInnerInterceptor插件
- 更新数据时会自动在SQL中添加version条件，确保只有version匹配时才能更新成功；如果更新影响行数为0，则说明版本已变化，抛出异常。

**面试重点**  
Q1：乐观锁的优缺点？  
A：  
性能好，不会阻塞其他线程；  
缺点是在高并发写场景下可能导致更新失败率较高，需要业务方处理重试逻辑。

Q2：乐观锁适用场景？  
A：读多写少的场景，如电商商品信息、用户资料等更新频率较低但并发可能较高的场景。

3.实现逻辑删除而非物理删除  
**实现要点**
- 在实体类中添加@TableLogic注解的deleted字段
- 在application.yml中配置全局逻辑删除规则
- 查询、更新操作会自动添加未删除条件，删除操作会转为更新操作

**面试重点**  
Q1：逻辑删除的优缺点？   
A：优点是可以恢复误删数据，便于审计追踪，保持数据完整性；缺点是增加存储开销，查询需要额外过滤条件，可能影响性能。

Q2：什么场景适合使用逻辑删除？   
A：对数据安全性要求高的场景，如订单记录、财务数据、用户信息等重要业务数据，以及需要数据恢复和审计功能的系统。

## 订单管理
处理用户通过微信下单的操作，支持自取和快递两种配送方式的选择，并记录完整订单流程。
## 优惠券管理
提供优惠券的发放、使用规则配置、有效期管理等功能，增强营销活动灵活性。
## 限购与预售管理
对特定商品设置购买数量上限，同时支持早鸟票预售时间及价格策略设定。
## 促销活动管理
支持限时优惠活动的定时开启与关闭，配合Spring Task实现自动化任务调度。
## 缓存管理
利用Redis缓存热点数据提升系统性能，结合Spring Cache实现本地多级缓存机制。
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
