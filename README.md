# 项目记录
1. AI-Alibaba-Model是一个停止的项目，原因在于Alibaba依赖对其公司的QWen模型支持都不到位。可以通过配置DashScope配置使用Qwen-Max，但对于多模态的Qwen-mini-turbo却不支持，无法支撑后续的多模态开发，不然就得在其他使用阶段切换模型。其次是DashScope类的Model集成内容太多，不喜欢使用。

# 思路记录
1. AOT的作用和几个继承类的作用（或许可以作为提前进行config参数和自动装配类编译的选择），该处AOT源自几个AI相关jar包下均有的内容
   2. AOT让类的静态初始化提前，但是不会让类实例化（静态初始化后是字段实例化、代码块实例化），提前配置类和自动装配类在编译期进行处理，减少了其在运行过程中初始化、实例化的反射调用（比如bean的注册中的反射调用）
   3. AOT适用于POJO类，无静态代码块，或静态代码块中无I/O操作或其他bean依赖的情况
2. spring-ai-core下的aot类值得研究
3. 同时发现ollama包和openai包下都存在api的调用类，其实之前的client类应该是可以被完全替代，并且之后的ImageModel和Audio调用也应该可以替代，只是需要考虑差异
4. 可以内置一个sqlite的数据库，升级上下文缓存子项目的适配范围
5. 虽然在controller中使用了类变量，但是没做到绑定用户，应该使用一个map进行chat参数类的关联，这个参数类包括integrationType、message（message主要预存user信息），key可以使用Token，再通过redis设置ttl

# 问题记录
1. dashscope没有chatClient，之前的client错误，当前项目仅能通过Openai和ollama调用ChatClient