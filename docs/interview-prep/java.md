# Java 后端八股 · 24 道题

按简历的 Java 专业技能编排。先读一个场景，再合上答案做题。★ 是第一轮优先题，◇ 是追问。第一轮每题用 60–90 秒回答，尽量说出「结果、机制、一个具体例子」。

## 一、集合、泛型与异常

**场景。** 地图系统用站点 ID 做索引。编辑站点名称后，用户还能通过原 ID 找到它。可把 ID 做成不可变键，站点详情作为值独立更新。

**概念和示范。** HashMap 先用哈希值定位桶，再用相等性确认键。比如 `record StationKey(String mapId, String stationId) {}` 把两个稳定的字符串组成键。两个同地图、同站点的键应指向同一条数据。有 1000 条路线时，按站点查索引和遍历所有路线是两种不同成本。

### 题目

- **J01 ★** `HashMap.put` 和 `get` 各靠什么定位？两个对象的哈希相同，后写的一定覆盖前面的吗？
- **J02 ★** 一个自定义键在 `put` 后改了参与 `hashCode` 的字段。请预判同一个对象再 `get` 时可能发生什么。
- **J03 ◇** `ArrayList` 末尾添加为什么是摊还 O(1)？在下标 0 插入会发生什么？
- **J04 ◇** 方法要从一组车辆中读取 `Vehicle`，另一个方法要向集合写入 `Vehicle`。什么时候用 `? extends Vehicle`，什么时候用 `? super Vehicle`？
- **J05 ◇** `try-with-resources` 的主体和资源的 `close()` 都抛异常，调用方看到哪个？从哪里找另一个？

## 二、并发、线程池与异步

**场景。** 两个请求看到 `count=0`，分别计算 `count+1`，最后都把 1 写回。明明执行了两次，计数只有 1。

**概念和示范。** **可见性**关心线程何时看得到其他线程的写入，**原子性**关心一组操作中间是否可能被穿插。即使字段加了 `volatile`，读、加、写仍可能交错。路由表一题用 `volatile` 发布快照，同时用锁保护「比较版本并发布」这个整体。 [15 版本化路由](../../15-route-snapshot-lab/README.md)

### 题目

- **J06 ★** `volatile int count` 的 `count++` 为什么可能丢计数？写出安全的单变量方案和涉及多个字段时的方案。
- **J07 ★** 用一句话解释 happens-before。线程 A 写好不可变快照再更新 `volatile` 引用，线程 B 读到新引用时可以依赖什么？
- **J08 ★** 线程池核心 2、最大 4、队列长度 2，假设每个任务都阻塞且不释放，依次放进 7 个任务，各去哪？
- **J09 ◇** `thenApply` 和 `thenCompose` 有什么区别？为什么网关的阻塞 RPC 常要指定独立的执行器？
- **J10 ◇** `ConcurrentHashMap` 里用 `get` 后接 `put` 做计数还会丢更新吗？如果上下游两把锁顺序不同，还可能遇到什么？

## 三、JVM 与故障排查

**场景。** 进程 CPU 突然持续偏高。先找哪个线程热，再看它连续几次都在哪个调用栈，才能知道是业务忙循环、序列化重负载，还是 GC。

**概念和示范。** 方法执行时有线程栈帧，普通对象通常从 Java 堆角度分析，类元数据在 HotSpot 通常与本地 Metaspace 关联。一次调用栈只能说明某个时刻在做什么，结合多次采样与时间范围才能建立证据。

### 题目

- **J11 ★** 堆、线程栈、Metaspace 分别放什么？递归过深和堆耗尽常见的报错各是什么？
- **J12 ◇** 类的加载、链接、初始化分别做什么？双亲委派解决什么问题？同名类为何仍可能互不兼容？
- **J13 ★** 什么是 GC Roots？一个 Map 中对象已经没业务用途，为什么还能造成堆内存持续增长？
- **J14 ★** CPU 飙高和大量请求超时时，你分别怎样用线程栈、GC 指标和堆证据缩小范围？

## 四、Spring、MVC、MyBatis 与服务治理

**场景。** `OrderService.submit()` 通过 `this.save()` 调用同类内带 `@Transactional` 的方法。本以为开始一个新事务，实际上事务代理没有拦到这个调用。

**概念和示范。** Spring 的 **IoC** 管理对象创建和装配，**DI** 把依赖交给对象，**AOP** 把事务等横切逻辑织入调用边界。默认 Spring 事务代理会对经过代理的适用方法应用事务规则。可以把需要单独事务的方法拆到另一个 Bean，由调用者通过代理调用。 [Spring 声明式事务](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)

### 题目

- **J15 ★** IoC、DI、AOP 分别是什么？用 Controller 注入 Service 和 Service 开事务举例。
- **J16 ★** 默认代理模式下同类自调用为什么常让 `@Transactional` 失效？方法内部吞了异常后又可能怎样？
- **J17 ◇** HTTP 请求从 Spring MVC 到 MyBatis Mapper，再变成 JSON 响应，经过哪些层？`#{id}` 和 `${column}` 的区别是什么？
- **J18 ◇** Spring Boot 自动配置如何决定是否装配一个 Bean？服务注册发现、运行时配置更新、负载均衡分别解决什么问题？

## 五、MySQL、Redis 与消息队列

**场景。** 用户更新订单状态后，缓存仍短暂显示旧值。稍后消费者收到两次同一通知。分别找出陈旧缓存和重复消费产生的时间线，再决定各修哪一层。

**概念和示范。** **Cache-aside** 常见流程是读缓存、未命中查库并回填，写时先提交数据库再删除缓存。并发旧读可能在删除后回填旧值，因此要根据业务要求选择短 TTL、按版本控制回填等方案。**幂等**关心同一个业务事件被重复处理时业务效果是否重复。可以在同一数据库事务内用唯一事件键保护业务写入。 [18 事件幂等](../../18-event-idempotency-lab/README.md)

### 题目

- **J19 ★** 有联合索引 `(tenant_id, status, created_at)`。按租户、状态等值并按时间查范围时，为什么适用？只按状态查询时应怎样判断？
- **J20 ★** InnoDB 的 MVCC 怎样帮助普通 SELECT 获得一致结果？RR 同一事务的两次普通 SELECT 和 RC 有什么差别？
- **J21 ★** Redis 穿透、击穿、雪崩各是什么？分别给一个首选治理手段。为什么 TTL 随机化也有边界？
- **J22 ◇** Redis 做订单查询缓存时，库和缓存怎样配合？把一个旧值回填的并发序列画出来。RDB 与 AOF 分别记录什么？
- **J23 ★** 消费者写库成功但提交 Kafka offset 或发送 RocketMQ ACK 失败，接下来会发生什么？你怎样设计业务层防重复？
- **J24 ◇** Kafka 按订单 ID 分区能解决哪种顺序问题？发布事件时怎样避免「库提交成功但事件没有投递」？

## 参考答案与易错点

### 集合、泛型与异常

- **J01** 哈希用于选桶，相等性用于定位键。碰撞的两个不同键可以共存，满足相等语义的后一次 `put` 更新值。约定是两个 `equals` 相等的对象必须有相同哈希，相同哈希的对象仍可不等。以 Java 17 的实现为例，冲突较多时桶在一定条件下会树化。 [Java 17 HashMap](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/HashMap.html)
- **J02** 可能取不到刚插入的值。存储时记录的哈希或位置对应修改前的键。即使引用还是原来那个，查找可能根本走不到原条目。用不可变键，或者删除旧键后用新键插入。
- **J03** 大部分尾插只写一个位置，偶尔扩容需要分配数组并复制元素。把总成本分摊到一串追加操作，单次摊还为 O(1)。头插要移动已有元素，通常为 O(n)。 [Java 17 ArrayList](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ArrayList.html)
- **J04** 只读入参用 `List<? extends Vehicle>`，读取可当 `Vehicle`，无法把任意 `Vehicle` 安全放进去，因为实际可能是 `List<Truck>`。写入用 `List<? super Vehicle>`，可放 `Vehicle` 与它的子类，读出来的静态保证只有 `Object`。
- **J05** 主体的异常向外传播，关闭失败作为 suppressed exception，可从 `getSuppressed()` 找。若主体成功，关闭异常可以是主异常。多个资源按声明的反序关闭。

### 并发与异步

- **J06** 两线程能先后读到同一个旧值，各自算后写回。单变量用 `AtomicInteger.incrementAndGet()`。若还要同时保护「余额减少、记录增加」等跨字段不变量，可用同一把锁覆盖整个过程。
- **J07** happens-before 是 Java 内存模型中用于约束读写可见性与顺序的关系。线程 A 在发布引用前做的初始化，经对同一 `volatile` 字段的写和随后的读传递给 B。快照发布后保持不可变，避免另外引入未同步修改。 [Java 内存模型](https://docs.oracle.com/javase/specs/jls/se17/html/jls-17.html#jls-17.4.5)
- **J08** 1、2 启动核心线程。3、4 入队。5、6 队列满后创建额外线程。7 走拒绝策略。别漏掉「先入队，再尝试扩到最大线程数」的顺序。队列容量、拒绝策略、下游承载能力要一起定。 [ThreadPoolExecutor](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html)
- **J09** `thenApply` 映射值。若回调本身返回 Future，`thenCompose` 把结果接成一层 Future。阻塞 RPC 用有界的独立执行器可隔离线程占用，并结合超时与拒绝策略。不指定执行器的异步方法可能共享公共池。 [CompletableFuture](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/CompletableFuture.html)
- **J10** 会。容器内部安全不意味着自己组合的两步是原子的。同键计数可用 `merge` 或 `compute` 处理。若 A 先拿锁 1 等锁 2，B 先拿锁 2 等锁 1，存在死锁。统一加锁顺序可避免这类环形等待。

### JVM

- **J11** 线程栈存方法栈帧，其中有局部变量和操作数栈。对象与数组通常谈 Java 堆。HotSpot Metaspace 存类元数据。递归过深常见 `StackOverflowError`，堆不足常见 `OutOfMemoryError: Java heap space`，OOM 要结合具体区域和报错。
- **J12** 加载把类定义读入 JVM，链接包括验证、准备和解析，初始化执行类初始化逻辑。常见父优先机制让上层定义优先复用。类身份由二进制名和定义它的类加载器共同确定，同名类可由不同加载器分别定义。
- **J13** GC Roots 是可达性分析的起点，例如活动线程里可达的引用。若某个全局缓存仍经引用链可达，Map 中条目也可达。旧数据失去业务价值后要有过期或淘汰边界。
- **J14** 先确认故障时间、负载和 GC 指标，再用重复的线程栈或剖析采样区分持续运行和等待。对高 CPU 关联热点线程，对请求超时查阻塞锁、线程池队列与下游耗时，对存活对象持续上涨再看堆直方图或 dump 的保留引用。单次线程栈不足以定因。 [JDK 故障排查指南](https://docs.oracle.com/en/java/javase/17/troubleshoot/)

### Spring

- **J15** IoC 是由容器负责组件创建和装配。DI 是实现方式，例如 Controller 的构造器接收 Service。AOP 把事务等重复逻辑放在调用边界，例如 Spring 代理在适用方法调用周围开启和结束事务。
- **J16** 同类内部调用绕过默认代理，内层方法标注的事务属性没有机会被该代理应用。若内层抛出异常但外层捕获后正常返回，代理可能看不到应触发回滚的异常。默认回滚规则通常涵盖运行时异常和 Error，也要留意 rollback-only、显式规则和传播行为。 [Spring 声明式事务](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
- **J17** `DispatcherServlet` 找处理器，绑定请求参数，Controller 调 Service，再由 Mapper 执行 SQL。响应由消息转换器序列化。MyBatis `#{id}` 绑定参数，`${column}` 是字符串替换，只应在受控的列名等场景按白名单映射。 [MyBatis SQL 映射](https://mybatis.org/mybatis-3/sqlmap-xml.html)
- **J18** 自动配置常按类路径、配置属性和是否已有 Bean 等条件决定装配。注册发现回答有哪些实例，配置更新回答运行参数和规则怎样分发，负载均衡回答一次调用选哪个实例。 [Spring Boot 自动配置](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html)

### MySQL、Redis 与消息队列

- **J19** 前两列等值固定了索引前缀，再对时间做连续范围扫描。只按 `status` 缺少首列，常规前缀范围定位受限。优化器仍可能因覆盖、跳跃扫描或成本模型选择索引。看 `EXPLAIN` 的访问方式、索引和估算行数，再用真实数据验证。 [MySQL 多列索引](https://dev.mysql.com/doc/refman/8.4/en/multiple-column-indexes.html)
- **J20** 普通一致性读按读视图判定可见版本，必要时沿历史版本读取。在 InnoDB 通常的 RR 下，同事务的普通一致性读复用第一次此类读取建立的快照。RC 下每次一致性读拿新快照。自己的修改与锁定读有另外语义，答题要限定观察方式。 [InnoDB 一致性读](https://dev.mysql.com/doc/refman/8.4/en/innodb-consistent-read.html)
- **J21** 穿透是大量不存在的键直达数据库，可短缓存空值。击穿是热点键失效引发并发重建，可合并同键重建。雪崩是大量缓存同时失效或节点故障，可分散 TTL 并控制回源负载。TTL 随机化解决集中到期，单独使用仍挡不住实例整体不可用。
- **J22** 写库提交后删缓存，读侧未命中才回源。旧读先查到旧值，写者提交新值并删缓存，旧读最后回填，仍会短暂陈旧。按容忍度选短 TTL、版本条件、可靠的失效事件。RDB 定期保存快照，AOF 记录写操作以供重放。 [Redis 持久化](https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/)
- **J23** 消费者可能再处理同一消息。用业务稳定的 `event_id` 配合数据库唯一约束，将去重记录和业务更新放在同一事务，提交后再推进 offset 或确认消息。提交前偏移量先推进会制造漏处理窗口。 [Kafka 消息投递语义](https://kafka.apache.org/41/design/design/#message-delivery-semantics)
- **J24** 相同订单的事件可进同一分区，顺序还要求同分区按序产生与处理。跨分区没有全局顺序，消费侧并行完成也可能打乱业务顺序。可用 Transactional Outbox，把业务更新和待发事件同事务提交，后台重试发布，消费者仍做幂等。

[Agent 题库](agent.md) · [闭卷模拟题](mock.md) · [回到面试学习路线](../interview.md)
