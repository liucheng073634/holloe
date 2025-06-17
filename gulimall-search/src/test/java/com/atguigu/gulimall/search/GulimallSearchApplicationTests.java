package com.atguigu.gulimall.search;

import cn.hutool.json.JSONUtil;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import springfox.documentation.spring.web.json.Json;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;
    @Test
    void contextLoads() {
        System.out.println(client);
    }
    @Test
    void indexdata()throws Exception {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User();
        user.setUserName("张三");
        user.setGender("男");
        user.setAge(18);
        String jsonStr = JSONUtil.toJsonStr(user);
        indexRequest.source(jsonStr, XContentType.JSON);
        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
    }

    @Test
    void searchdata()throws Exception {
         SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchRequest.source(searchSourceBuilder);

        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        //按照年龄聚合
        TermsAggregationBuilder size = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(size);
        AvgAggregationBuilder field = AggregationBuilders.avg("balanceAvgAgg").field("balance");
        searchSourceBuilder.aggregation(field);

        System.out.println(searchSourceBuilder.toString());
        SearchResponse search = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(search.toString());
    }


    @Data
    class User {
        private String userName;
        private String gender;
        private Integer age;
    }

}
