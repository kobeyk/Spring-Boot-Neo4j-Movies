package com.appleyk.repository;

import com.appleyk.node.Movie;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;


/**
 * 基于电影知识图谱的自问自答的查询接口
 * @author yukun24@126.com
 * @blob   http://blog.csdn.net/appleyk
 * @date   2018年5月10日-下午3:48:51
 */
public interface QuestionRepository extends Neo4jRepository<Movie,Long> {

	/**
	 * 0 对应问题模板0 == nm(电影) 评分
	 * 
	 * @param title
	 *            电影标题
	 * @return 返回电影的评分
	 */
	@Query("match(n:Movie) where n.title={title} return n.rating")
	Double getMovieRating(@Param("title") String title);

	/**
	 * 1 对应问题模板1 == nm(电影) 上映时间
	 * 
	 * @param title
	 *            电影标题
	 * @return 返回电影的上映日期
	 */
	@Query("match(n:Movie) where n.title={title} return n.releasedate")
	String getMovieReleaseDate(@Param("title") String title);

	/**
	 * 2 对应问题模板2 == nm(电影) 类型
	 * 
	 * @param title
	 *            电影标题
	 * @return 返回电影的类型、风格
	 */
	@Query("match(n:Movie)-[r:is]->(b:Genre) where n.title={title} return b.name")
	List<String> getMovieTypes(@Param("title") String title);

	/**
	 * 3 对应问题模板3 == nm(电影) 简介
	 * 
	 * @param title
	 *            电影标题
	 * @return 返回电影的剧情、简介
	 */
	@Query("match(n:Movie) where n.title ={title} return n.introduction")
	String getMovieInfo(@Param("title") String title);

	/**
	 * 4 对应问题模板4 == nm(电影) 简介
	 * 
	 * @param title
	 *            电影标题
	 * @return 返回电影中出演的演员都有哪些
	 */
	@Query("match(n:Person)-[:actedin]-(m:Movie) where m.title ={title} return n.name")
	List<String> getMovieActors(@Param("title") String title);

	/**
	 * 5 对应问题模板5 == nnt(演员) 简介
	 * 
	 * @param name
	 *            演员名
	 * @return 返回演员的出生日期
	 */
	@Query("match(n:Person) where n.name={name} return n.birthplace")
	String getActorInfo(@Param("name") String name);

	/**
	 * 6 对应问题模板6 == nnt(演员) ng(电影类型) 电影作品
	 * 
	 * @param name
	 *            演员名
	 * @param gname
	 *            电影类型名称    
	 * @return 返回电影名称列表
	 */
	@Query("match(n:Person)-[:actedin]-(m:Movie) where n.name ={name} "
			+ "match(g:Genre)-[:is]-(m) where g.name=~{gname} return distinct  m.title")
	List<String> getActorMoviesByType(@Param("name") String name, @Param("gname") String gname);

	/**
	 * 7对应问题模板7 == nnt(演员) 电影作品
	 * 
	 * @param name
	 * @return
	 */
	@Query("match(n:Person)-[:actedin]->(m:Movie) where n.name={name} return m.title")
	List<String> getActorMovies(@Param("name") String name);

	/**
	 * 8对应问题模板8 == nnt 参演评分 大于 x(电影评分)
	 * 
	 * @param name 演员姓名
	 * @param score 电影分数
	 * @return
	 */
	@Query("match(n:Person)-[:actedin]-(m:Movie) where n.name ={name} and m.rating > {score} return m.title")
	List<String> getActorMoviesByHScore(@Param("name") String name,@Param("score") Double score);
	
	
	/**
	 * 9对应问题模板9 == nnt 参演评分 小于 x(电影评分)
	 * 
	 * @param name 演员姓名
	 * @param score 电影分数
	 * @return
	 */
	@Query("match(n:Person)-[:actedin]-(m:Movie) where n.name ={name} and m.rating < {score} return m.title")
	List<String> getActorMoviesByLScore(@Param("name") String name,@Param("score") Double score);
	
	
	/**
	 * 10 对应问题模板10 == nnt(演员) 电影类型
	 * 
	 * @param name
	 *            演员名
	 * @return 返回演员出演过的所有电影的类型集合【不重复的】
	 */
	@Query("match(n:Person)-[:actedin]-(m:Movie) where n.name ={name} "
			+ "match(p:Genre)-[:is]-(m) return distinct  p.name")
	List<String> getActorMoviesType(@Param("name") String name);

	
	/**
	 * 12 对应问题模板12 == nnt(演员) 电影数量
	 * 
	 * @param name
	 *            演员名
	 * @return 返回演员出演过的所有电影的类型集合【不重复的】
	 */
	@Query("match(n)-[:actedin]-(m) where n.name ={name} return count(*)")
	Integer getMoviesCount(@Param("name") String name);
	
	/**
	 * 13 对应问题模板13 == nnt(演员) 出生日期
	 * 
	 * @param name
	 *            演员名
	 * @return 返回演员的出生日期
	 */
	@Query("match(n:Person) where n.name={name} return n.birth")
	String getActorBirth(@Param("name") String name);
	
}
