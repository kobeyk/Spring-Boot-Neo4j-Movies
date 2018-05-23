package com.appleyk.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.appleyk.process.ModelProcess;
import com.appleyk.repository.QuestionRepository;
import com.appleyk.service.QuestionService;
import com.hankcs.hanlp.dictionary.CustomDictionary;

@Service
@Primary
public class QuestionServiceImpl implements QuestionService {

	@Value("${rootDirPath}")
	private String rootDictPath;

	@Value("${HanLP.CustomDictionary.path.movieDict}")
	private String movieDictPath;

	@Value("${HanLP.CustomDictionary.path.genreDict}")
	private String genreDictPath;

	@Value("${HanLP.CustomDictionary.path.scoreDict}")
	private String scoreDictPath;

	@Autowired
	private QuestionRepository questionRepository;

	@Override
	public void showDictPath() {
		System.out.println("HanLP分词字典及自定义问题模板根目录：" + rootDictPath);
		System.out.println("用户自定义扩展词库【电影】：" + movieDictPath);
	}

	@Override
	public String answer(String question) throws Exception {

		ModelProcess queryProcess = new ModelProcess(rootDictPath);

		/**
		 * 加载自定义的电影字典 == 设置词性 nm 0
		 */

		loadMovieDict(movieDictPath);

		/**
		 * 加载自定义的类型字典 == 设置词性 ng 0
		 */
		loadGenreDict(genreDictPath);

		/**
		 * 加载自定义的评分字典 == 设置词性 x 0
		 */
		loadScoreDict(scoreDictPath);
		ArrayList<String> reStrings = queryProcess.analyQuery(question);
		int modelIndex = Integer.valueOf(reStrings.get(0));
		String answer = null;
		String title = "";
		String name = "";
		String type = "";
		Double score = 0.0;
		/**
		 * 匹配问题模板
		 */
		switch (modelIndex) {
		case 0:
			/**
			 * nm 评分 == 电影评分
			 */
			title = reStrings.get(1);
			score = questionRepository.getMovieRating(title);
			if (score != null) {
				BigDecimal b = new BigDecimal(score);
				// 四舍五入取两位小数
				answer = String.valueOf(b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());
			} else {
				answer = null;
			}
			break;
		case 1:
			/**
			 * nm 上映时间 == 电影上映时间
			 */
			title = reStrings.get(1);
			String releaseDate = questionRepository.getMovieReleaseDate(title);
			if (releaseDate != null) {
				answer = releaseDate;
			} else {
				answer = null;
			}
			break;
		case 2:
			/**
			 * nm 类型 == 电影类型
			 */
			title = reStrings.get(1);
			List<String> types = questionRepository.getMovieTypes(title);
			if (types.size() == 0) {
				answer = null;
			} else {
				answer = types.toString().replace("[", "").replace("]", "");
			}
			break;
		case 3:
			/**
			 * nm 简介 == 电影简介、详情
			 */
			title = reStrings.get(1);
			answer = questionRepository.getMovieInfo(title);
			break;
		case 4:
			/**
			 * nm 演员列表 == 电影演员列表
			 */
			title = reStrings.get(1);
			List<String> actors = questionRepository.getMovieActors(title);
			if (actors.size() == 0) {
				answer = null;
			} else {
				answer = actors.toString().replace("[", "").replace("]", "");
			}
			break;
		case 5:
			/**
			 * nnt 介绍 == 演员简介
			 */
			name = reStrings.get(1);
			answer = questionRepository.getActorInfo(name);
			break;
		case 6:
			/**
			 * nnt 电影类型 ng == 演员演过的x类型的电影有哪些
			 */
			name = reStrings.get(1);
			type = reStrings.get(2);
			if (type.indexOf("片") > 0) {
				type = type.substring(0, type.indexOf("片"));
			}
			// 模糊查询拼接参数 == 包含type的电影都查出来
			type = ".*" + type + "*.";
			List<String> movies = questionRepository.getActorMoviesByType(name, type);
			if (movies.size() == 0) {
				answer = null;
			} else {
				answer = movies.toString().replace("[", "").replace("]", "");
			}
			break;
		case 7:
			/**
			 * nnt 电影作品 == 演员的电影作品有哪些
			 */
			name = reStrings.get(1);
			List<String> actorMovies = questionRepository.getActorMovies(name);
			if (actorMovies.size() == 0) {
				answer = null;
			} else {
				answer = actorMovies.toString().replace("[", "").replace("]", "");
			}
			break;
		case 8:
			/**
			 * 1 2 3 4 nnt 参演评分 大于 x == 演员参演的电影评分大于x的有哪些
			 */
			name = reStrings.get(1);
			score = Double.parseDouble(reStrings.get(4));
			List<String> actorMoviesByScore = questionRepository.getActorMoviesByHScore(name, score);
			if (actorMoviesByScore.size() == 0) {
				answer = null;
			} else {
				answer = actorMoviesByScore.toString().replace("[", "").replace("]", "");
			}
			break;
		case 9:
			/**
			 * 1 2 3 4 nnt 参演评分 小于 x == 演员参演的电影评分小于x的有哪些
			 */
			name = reStrings.get(1);
			score = Double.parseDouble(reStrings.get(4));
			List<String> actorMoviesByLScore = questionRepository.getActorMoviesByLScore(name, score);
			if (actorMoviesByLScore.size() == 0) {
				answer = null;
			} else {
				answer = actorMoviesByLScore.toString().replace("[", "").replace("]", "");
			}

			break;
		case 10:
			/**
			 * nnt 电影类型 == 演员参演的电影类型有哪些
			 */
			name = reStrings.get(1);
			List<String> movieTypes = questionRepository.getActorMoviesType(name);
			if (movieTypes.size() == 0) {
				answer = null;
			} else {
				answer = movieTypes.toString().replace("[", "").replace("]", "");
			}
			break;
		case 11:
			/**
			 * 1 2 3 4 nnt nnr 合作 电影列表 == 演员A和演员B合作的电影有哪些
			 */
			name = reStrings.get(1);
			List<String> actorMoviesA = questionRepository.getActorMovies(name);
			/**
			 * 如果演员A的电影作品无，那么A和演员B无合作之谈
			 */
			if (actorMoviesA.size() == 0) {
				answer = null;
				break;
			}

			name = reStrings.get(2);
			List<String> actorMoviesB = questionRepository.getActorMovies(name);
			/**
			 * 如果演员B的电影作品无，那么B和演员A无合作之谈
			 */
			if (actorMoviesB.size() == 0) {
				answer = null;
				break;
			}

			/**
			 * A的作品与B的作品求交集
			 */
			actorMoviesA.retainAll(actorMoviesB);

			if (actorMoviesA.size() == 0) {
				answer = null;
			} else {
				answer = actorMoviesA.toString().replace("[", "").replace("]", "");
			}
			break;
		case 12:
			name = reStrings.get(1);
			Integer count = questionRepository.getMoviesCount(name);
			if (count == null) {
				answer = null;
			} else {
				answer = String.valueOf(count) + "部电影";
			}
			break;
		case 13:
			/**
			 * nnt 出生日期 == 演员出生日期
			 */
			name = reStrings.get(1);
			answer = questionRepository.getActorBirth(name);
			break;
		default:
			break;
		}

		System.out.println(answer);
		if (answer != null && !answer.equals("") && !answer.equals("\\N")) {
			return answer;
		} else {
			return "sorry,我没有找到你要的答案";
		}
	}

	/**
	 * 加载自定义电影字典
	 * 
	 * @param path
	 */
	public void loadMovieDict(String path) {

		File file = new File(path);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			addCustomDictionary(br, 0);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * 加载自定义电影类别字典
	 * 
	 * @param path
	 */
	public void loadGenreDict(String path) {

		File file = new File(path);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			addCustomDictionary(br, 1);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 加载自定义电影评分字典
	 * 
	 * @param path
	 */
	public void loadScoreDict(String path) {

		File file = new File(path);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			addCustomDictionary(br, 2);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 添加自定义分词及其词性，注意数字0表示频率，不能没有
	 * 
	 * @param br
	 * @param type
	 */
	public void addCustomDictionary(BufferedReader br, int type) {

		String word;
		try {
			while ((word = br.readLine()) != null) {
				switch (type) {
				/**
				 * 设置电影名词词性 == nm 0
				 */
				case 0:
					CustomDictionary.add(word, "nm 0");
					break;
				/**
				 * 设置电影类型名词 词性 == ng 0
				 */
				case 1:
					CustomDictionary.add(word, "ng 0");
					break;
				/**
				 * 设置电影评分数词 词性 == x 0
				 */
				case 2:
					CustomDictionary.add(word, "x 0");
					break;
				default:
					break;
				}
			}
			br.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
