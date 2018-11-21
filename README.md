# Spring-Boot-Neo4j-Movies
Spring-Boot集成Neo4j并利用Spark的朴素贝叶斯分类器实现基于电影知识图谱的智能问答系统
博客地址：https://blog.csdn.net/appleyk


升级Spark依赖，由原来的2.3升级到2.4，GitHub官方提醒> = 1.0.0，<= 2.3.2之间的版本容易受到攻击
spark2.4  == >scala2.11 and scala2.12


<!-- https://mvnrepository.com/artifact/org.apache.spark/spark-core -->
<dependency>
	<groupId>org.apache.spark</groupId>
	<artifactId>spark-core_2.12</artifactId>
	<version>2.4.0</version>
</dependency>
<!-- https://mvnrepository.com/artifact/org.apache.spark/spark-mllib -->
<dependency>
	<groupId>org.apache.spark</groupId>
	<artifactId>spark-mllib_2.12</artifactId>
	<version>2.4.0</version>
	<scope>runtime</scope>
</dependency>




如果down下来的demo在本地无法运行，请自行降低版本，保证本地spark环境的版本号和pom中的spark依赖的jar包版本一致！
