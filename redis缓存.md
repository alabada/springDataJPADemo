### �����̼�����redis����
#### ��1��.maven��
```
   <!-- Redis -->
    <dependency>
      <groupId>redis.clients</groupId>
      <artifactId>jedis</artifactId>
      <version>2.7.3</version>
    </dependency>

```
#### ��2��.һ��redis���ã�����spring����
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <!-- ��jedis��jarΪ2.7.0�汾 -->


    <!-- jedisPool������Ϣ -->
    <bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
        <property name="maxTotal" value="1000"/> <!-- ����һ��pool�ɷ�����ٸ�jedisʵ�� -->
        <property name="maxIdle" value="200" />   <!-- ����һ��pool����ж��ٸ�״̬Ϊidle(����)��jedisʵ�� -->
        <property name="maxWaitMillis" value="2000" />  <!-- ��ʾ��borrowһ��jedisʵ��ʱ�����ĵȴ�ʱ�䣬��������ȴ�ʱ�䣬��ֱ���׳�JedisConnectionException -->
        <property name="testOnBorrow" value="true" /> <!-- ��borrowһ��jedisʵ��ʱ���Ƿ���ǰ����validate���������Ϊtrue����õ���jedisʵ�����ǿ��õ� -->
    </bean>

    <!-- jedis�ͻ��˵��������� -->
    <bean id="jedisPool" class="redis.clients.jedis.JedisPool" scope="singleton">
        <constructor-arg name="poolConfig" ref="jedisPoolConfig" />
        <constructor-arg name="host" value="127.0.0.1"></constructor-arg>
        <constructor-arg name="port" value="6379"></constructor-arg>
    </bean>



    <bean id="jedisClient" class="com.ima.service.JedisClientSingle" />

</beans>
```
```
//��д�������ļ��󣬼ǵ���web.xmlע���spring
  <!-- Spring ���� -->
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:META-INF/spring/spring-jpa.xml,classpath:META-INF/spring/spring-jedis.xml</param-value>
  </context-param>
```
#### ��3�����Խӿڣ�
```
public interface JedisClient {

    public String get(String key);
    public String set(String key, String value);
    public String hget(String hkey, String key);
    public long hset(String hkey, String key, String value);
    public long incr(String key);
    public long expire(String key, int second);
    public long ttl(String key);
    public long del(String key);
    public long hdel(String hkey, String key);

}
```
#### ��4���ӿ�ʵ���ࣺ
```
@Service
public class JedisClientSingle implements JedisClient {


    @Bean
    public JedisPool getJedisPool(){
        return new JedisPool("127.0.0.1",6379);
    }
    @Override
    public String get(String key) {
        Jedis jedis = getJedisPool().getResource();
        String string = jedis.get(key);
        jedis.close();
        return string;
    }

    @Override
    public String set(String key, String value) {
        Jedis jedis = getJedisPool().getResource();
        String string = jedis.set(key, value);
        jedis.close();
        return string;
    }

    @Override
    public String hget(String hkey, String key) {
        System.out.println("jedisPool   "+getJedisPool());
        Jedis jedis = getJedisPool().getResource();
        System.out.println("jedis   "+jedis);
        String string = jedis.hget(hkey, key);
        jedis.close();
        return string;
    }

    @Override
    public long hset(String hkey, String key, String value) {
        Jedis jedis = getJedisPool().getResource();
        Long result = jedis.hset(hkey, key, value);
        jedis.close();
        return result;
    }

    @Override
    public long incr(String key) {
        Jedis jedis = getJedisPool().getResource();
        Long result = jedis.incr(key);
        jedis.close();
        return result;
    }

    @Override
    public long expire(String key, int second) {
        Jedis jedis = getJedisPool().getResource();
        Long result = jedis.expire(key, second);
        jedis.close();
        return result;
    }

    @Override
    public long ttl(String key) {
        Jedis jedis = getJedisPool().getResource();
        Long result = jedis.ttl(key);
        jedis.close();
        return result;
    }

    @Override
    public long del(String key) {
        Jedis jedis = getJedisPool().getResource();
        Long result = jedis.del(key);
        jedis.close();
        return result;
    }

    @Override
    public long hdel(String hkey, String key) {
        Jedis jedis = getJedisPool().getResource();
        Long result = jedis.hdel(hkey,key);
        jedis.close();
        return result;
    }
}
```
#### ��5����д���Բ�����
```
  //����ʷ��¼
    @RequestMapping(value = "/findHistory", method = {RequestMethod.GET, RequestMethod.POST})
    public String findHistory(Long id) {
        DTO dto = new DTO();
        List<IDouChange> iDouChangeList = null;
        System.out.println("jedisClient  :" + jedisClient);
        try {
            String resulthget = jedisClient.hget("���˻��ּ�¼", id + "");
            if (resulthget != null) {
                //�ַ���תΪlist
                System.out.println("�л���������������");
                JSONArray array = JSONArray.parseArray(resulthget);
                iDouChangeList = (List) array;
            } else {
                System.out.println("���˻��ּ�¼û���");
                iDouChangeList = aiDouService.getHistory(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (iDouChangeList == null) {
            dto.code = "-1";
            dto.msg = "Have not updateAvatar";
        }
        try {
            String cacheString = JsonUtils.objectToJson(iDouChangeList);
            jedisClient.hset("���˻��ּ�¼", id + "", cacheString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSON.toJSONString(iDouChangeList);
    }
```