# 项目记录
1. AI-Alibaba-Model是一个停止的项目，原因在于Alibaba依赖对其公司的QWen模型支持都不到位。可以通过配置DashScope配置使用Qwen-Max，但对于多模态的Qwen-mini-turbo却不支持，无法支撑后续的多模态开发，不然就得在其他使用阶段切换模型。其次是DashScope类的Model集成内容太多，不喜欢使用。

# 思路记录
1. AOT的作用和几个继承类的作用（或许可以作为提前进行config参数和自动装配类编译的选择），该处AOT源自几个AI相关jar包下均有的内容
   1. AOT让类的静态初始化提前，但是不会让类实例化（静态初始化后是字段实例化、代码块实例化），提前配置类和自动装配类在编译期进行处理，减少了其在运行过程中初始化、实例化的反射调用（比如bean的注册中的反射调用）
   2. AOT适用于POJO类，无静态代码块，或静态代码块中无I/O操作或其他bean依赖的情况
   3. Spring-Aot作用于maven项目compile之后，进行aot生成会让运行中会使用到的诸如cglib代理类直接生成，不再动态生成，提高运行效率
   4. SpringBoot3.2开始支持AOT，SpringBoot3.3开始默认启用AOT，可以自动打包项目中依赖的可以进行aot的类
2. 同时发现ollama包和openai包下都存在api的调用类，其实之前的client类应该是可以被完全替代，并且之后的ImageModel和Audio调用也应该可以替代，只是需要考虑差异
   1. 即便是spring-ai下的包，目前的1.0.0-M6的版本中，针对chat、image、audio的调用也并非完全统一，封装包始终是有差异的，如prompt的封装，spring-ai下的多模态调用虽然秉承使用model.call，但各自有各自的prompt封装类，且各自封装类使用各自的Message和options，用于功能适应，尽管这些封装类都继承子ModelRequest
3. 可以内置一个sqlite的数据库，升级上下文缓存子项目的适配范围
4. 虽然在controller中使用了类变量，但是没做到绑定用户，应该使用一个map进行chat参数类的关联，这个参数类包括integrationType、message（message主要预存user信息），key可以使用Token，再通过redis设置ttl
5. 一直在思考，是否可以通过某种协议，将python的大模型应用和java的应用进行结合，毕竟目前大模型的生态还是在python这边比较完善

# 问题记录
1. dashscope没有chatClient，之前的client错误，当前项目仅能通过Openai和ollama调用ChatClient
   1. 其实使用的chatClient是通过spring-ai的ChatClient类使用各自模型的model构建的，并无错误
   2. 但image和audio的调用却没有client类进行适配，导致需要直接使用各自的model类
2. 语音识别下alibaba组件使用MultiModalConversation进行处置