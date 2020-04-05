package com.appleyk.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.appleyk.core.CoreProcessor;
import com.appleyk.repository.QuestionRepository;
import com.appleyk.service.QuestionService;


/**
 * <p>核心问答业务实现类</p>
 *
 * @author Appleyk
 * @version V.0.1.2
 * @blob https://blog.csdn.net/Appleyk
 * @date updated on 21:21 2020/3/31
 */
@Service
@Primary
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CoreProcessor queryProcess;

    @Override
    public String answer(String question) throws Exception {

        List<String> reStrings = queryProcess.analysis(question);
        int modelIndex = Integer.valueOf(reStrings.get(0));
        String answer =null;
        String title;
        String name;

        /**匹配问题模板*/
        switch (modelIndex) {
            case 0:
                answer = getMovieRating(reStrings);
                break;
            case 1:
                answer = getMovieReleaseDate(reStrings);
                break;
            case 2:
                answer = getMovieTypes(reStrings);
                break;
            case 3:
                /** nm 简介 == 电影简介、详情*/
                title = reStrings.get(1);
                answer = questionRepository.getMovieInfo(title);
                break;
            case 4:
                answer = getMovieActors(reStrings);
                break;
            case 5:
                /** nnt 介绍 == 演员简介*/
                name = reStrings.get(1);
                answer = questionRepository.getActorInfo(name);
                break;
            case 6:
                answer = getMoviesByType(reStrings);
                break;
            case 7:
                /** nnt 电影作品 == 演员的电影作品有哪些*/
                name = reStrings.get(1);
                List<String> actorMovies = questionRepository.getActorMovies(name);
                if (actorMovies.size() == 0) {
                    answer = null;
                } else {
                    answer = actorMovies.toString().replace("[", "").replace("]", "");
                }
                break;
            case 8:
                answer = getMoviesByHScore(reStrings);
                break;
            case 9:
                answer = getActorMoviesByLScore(reStrings);
                break;
            case 10:
                answer = getActorMoviesType(reStrings);
                break;
            case 11:
                answer = getActorMovies(reStrings);
                break;
            case 12:
                answer = getMoviesCount(reStrings);
                break;
            case 13:
                /** nnt 出生日期 == 演员出生日期*/
                name = reStrings.get(1);
                answer = questionRepository.getActorBirth(name);
                break;
            default:
                break;
        }
        System.out.println(answer);
        if (answer != null && !"".equals(answer) && !("\\N").equals(answer)) {
            return answer;
        } else {
            return "sorry,小主,我没有找到你要的答案";
        }
    }

    /**nm 评分 == 电影评分*/
    private String getMovieRating(List<String> reStrings) {
        String title;
        Double score;
        String answer;
        title = reStrings.get(1);
        score = questionRepository.getMovieRating(title);
        if (score != null) {
            BigDecimal b = new BigDecimal(score);
            // 四舍五入取两位小数
            answer = String.valueOf(b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());
        } else {
            answer = null;
        }
        return answer;
    }

    /** nm 上映时间 == 电影上映时间*/
    private String getMovieReleaseDate(List<String> reStrings) {
        String title;
        String answer;
        title = reStrings.get(1);
        String releaseDate = questionRepository.getMovieReleaseDate(title);
        if (releaseDate != null) {
            answer = releaseDate;
        } else {
            answer = null;
        }
        return answer;
    }

    /**nm 类型 == 电影类型*/
    private String getMovieTypes(List<String> reStrings) {
        String title;
        String answer;
        title = reStrings.get(1);
        List<String> types = questionRepository.getMovieTypes(title);
        if (types.size() == 0) {
            answer = null;
        } else {
            answer = types.toString().replace("[", "").replace("]", "");
        }
        return answer;
    }

    /** nm 演员列表 == 电影演员列表*/
    private String getMovieActors(List<String> reStrings) {
        String title;
        String answer;
        title = reStrings.get(1);
        List<String> actors = questionRepository.getMovieActors(title);
        if (actors.size() == 0) {
            answer = null;
        } else {
            answer = actors.toString().replace("[", "").replace("]", "");
        }
        return answer;
    }

    /** nnt 电影类型 ng == 演员演过的x类型的电影有哪些*/
    private String getMoviesByType(List<String> reStrings) {
        String name;
        String type;
        String answer;
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
        return answer;
    }

    /** 1 2 3 4 nnt 参演评分 大于 x == 演员参演的电影评分大于x的有哪些*/
    private String getMoviesByHScore(List<String> reStrings) {
        String name;
        Double score;
        String answer;
        name = reStrings.get(1);
        score = Double.parseDouble(reStrings.get(4));
        List<String> actorMoviesByScore = questionRepository.getActorMoviesByHScore(name, score);
        if (actorMoviesByScore.size() == 0) {
            answer = null;
        } else {
            answer = actorMoviesByScore.toString().replace("[", "").replace("]", "");
        }
        return answer;
    }

    private String getMoviesCount(List<String> reStrings) {
        String name;
        String answer;
        name = reStrings.get(1);
        Integer count = questionRepository.getMoviesCount(name);
        if (count == null) {
            answer = null;
        } else {
            answer = String.valueOf(count) + "部电影";
        }
        return answer;
    }

    /** 1 2 3 4 nnt 参演评分 小于 x == 演员参演的电影评分小于x的有哪些 */
    private String getActorMoviesByLScore(List<String> reStrings) {
        String name;
        Double score;
        String answer;
        name = reStrings.get(1);
        score = Double.parseDouble(reStrings.get(4));
        List<String> actorMoviesByLScore = questionRepository.getActorMoviesByLScore(name, score);
        if (actorMoviesByLScore.size() == 0) {
            answer = null;
        } else {
            answer = actorMoviesByLScore.toString().replace("[", "").replace("]", "");
        }
        return answer;
    }

    /**= nnt 电影类型 == 演员参演的电影类型有哪些*/
    private String getActorMoviesType(List<String> reStrings) {
        String name;
        String answer;
        name = reStrings.get(1);
        List<String> movieTypes = questionRepository.getActorMoviesType(name);
        if (movieTypes.size() == 0) {
            answer = null;
        } else {
            answer = movieTypes.toString().replace("[", "").replace("]", "");
        }
        return answer;
    }

    /**1 2 3 4 nnt nnr 合作 电影列表 == 演员A和演员B合作的电影有哪些*/
    private String getActorMovies(List<String> reStrings) {
        String name;
        String answer;
        name = reStrings.get(1);
        List<String> actorMoviesA = questionRepository.getActorMovies(name);
        /**如果演员A的电影作品无，那么A和演员B无合作之谈*/
        if (actorMoviesA.size() == 0) {
            answer = null;
            return answer;
        }

        name = reStrings.get(2);
        List<String> actorMoviesB = questionRepository.getActorMovies(name);
        /**如果演员B的电影作品无，那么B和演员A无合作之谈*/
        if (actorMoviesB.size() == 0) {
            answer = null;
            return answer;
        }

        /** A的作品与B的作品求交集*/
        actorMoviesA.retainAll(actorMoviesB);

        if (actorMoviesA.size() == 0) {
            answer = null;
        } else {
            answer = actorMoviesA.toString().replace("[", "").replace("]", "");
        }
        return answer;
    }

}
