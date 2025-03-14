package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.AttrResponseVo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result=null;

        SearchRequest searchResult = buildSearchRequrest(param);
        try {
            SearchResponse search = client.search(searchResult, GulimallElasticSearchConfig.COMMON_OPTIONS);
        result=buildSearchResult(search,param);
        } catch (Exception e) {

        }
        
        return result;
    }

    /**
     * 构建结果数据
     * @param search
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse search,SearchParam param) {
        SearchResult request = new SearchResult();
        SearchHits hits = search.getHits();

        List<SkuEsModel> esModels = new ArrayList<>();
            if(hits != null && hits.getHits().length>0){
                for (SearchHit hit : hits.getHits()) {
                    String sourceAsString = hit.getSourceAsString();
                    SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                    if(!StringUtils.isEmpty(param.getKeyword())){
                        HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                        String string = skuTitle.getFragments()[0].toString();
                        skuEsModel.setSkuTitle(string);
                    }
                    esModels.add(skuEsModel);
                }
            }
        request.setProduct(esModels);

    //当前所有商品涉及到的所有属性品牌
       List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = search.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
            for(Terms.Bucket bucket: attrIdAgg.getBuckets()){
                SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
                //1.属性id
                long attrId = bucket.getKeyAsNumber().longValue();
                //2.属性名
                String attrNameAgg = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();

                //3.属性值
                List<String> AttrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map((item) -> {
                    String keyAsString = ((Terms.Bucket) item).getKeyAsString();

                    return keyAsString;
                }).collect(Collectors.toList());
                attrVo.setAttrId(attrId);
                attrVo.setAttrName(attrNameAgg);
                attrVo.setAttrValue(AttrValues);

                attrVos.add(attrVo);

            }
            request.setAttrs(attrVos);
        //当前所有商品涉及到的所有品牌
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = search.getAggregations().get("brand_agg");
        for( Terms.Bucket bucket : brandAgg.getBuckets()){
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //1.品牌id
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            //2.品牌名
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            //3.品牌图片
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }


        request.setBrands(brandVos);
        //获取分类聚合结果
        ParsedLongTerms catalog_agg = search.getAggregations().get("catalog_agg");

        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();

        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name_agg = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name_agg);
            catalogVos.add(catalogVo);
        }

        request.setCatalogs(catalogVos);


        //获取总记录数
        Long total = hits.getTotalHits().value;
        request.setTotal(total);
         int totalHits=Integer.valueOf(String.valueOf(total));
        int totalPages =  totalHits%EsConstant.PRODUCT_PAGESIZE==0?  (totalHits / EsConstant.PRODUCT_PAGESIZE) :(totalHits/EsConstant.PRODUCT_PAGESIZE+1);
       // 获取总页码
        request.setTotalPages(totalPages);
        // 获取当前页码
        request.setPageNum(param.getPageNum());
        List<Integer> pageNavs = new ArrayList<>();
        for ( int i = 1; i <= totalPages; i++){
            pageNavs.add(i);
        }
        request.setPageNavs(pageNavs);

        List<String> attrs = param.getAttrs();
        if (attrs != null && attrs.size() > 0) {
            List<SearchResult.NavVo> navVos = attrs.stream().map(attr -> {
                String[] split = attr.split("_");
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //6.1 设置属性值
                navVo.setNavValue(split[1]);
                //6.2 查询并设置属性名
                try {
                    R r = productFeignService.attrInfo(Long.parseLong(split[0]));
                    if (r.getCode() == 0) {
                        AttrResponseVo attrResponseVo = JSON.parseObject(JSON.toJSONString(r.get("attr")), new TypeReference<AttrResponseVo>() {
                        });
                        navVo.setNavName(attrResponseVo.getAttrName());
                    }
                } catch (Exception e) {
                    log.error("远程调用商品服务查询属性失败", e);
                }

                //6.3 设置面包屑跳转链接
                String replace = getString(param, attr,"attrs");
                navVo.setLink("http://search.gulimall.com/list.html" + (replace.isEmpty()?"":"?"+replace));
                return navVo;
            }).collect(Collectors.toList());
            request.setNavs(navVos);

            //查询所有品牌

        }



        return request;
    }

    private static String getString(SearchParam param, String attr,String key) {
        String queryString = param.get_queryString();
        String replace = queryString.replace("&"+key+"=" + attr, "").replace(key+"=" + attr +"&", "").replace(key+"=" + attr, "");
        return replace;
    }

    /**
     * 准备检索请求
     * @return
     */
    private SearchRequest buildSearchRequrest(SearchParam param) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 1.构建查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
       // 1.1 must
        if(!StringUtils.isEmpty(param.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        // 1.2 按照catalogId
        if(param.getCatalog3Id()!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        //1.3按照brandId
        if(param.getBrandId()!=null && param.getBrandId().size()>0){
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandId",param.getBrandId()));

        }

        //按照库存
        if (param.getHasStock() != null&& param.getHasStock() != 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("hasStock", param.getHasStock() == 1));
        }

        // 1.4按照价格区间
        if(!StringUtils.isEmpty(param.getSkuPrice())){
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if(s.length==2){
                skuPrice.gte(s[0]).lte(s[1]);
            }else if (s.length==1){
                if(param.getSkuPrice().startsWith("_")){
                    skuPrice.lte(s[0]);
                }
                if(param.getSkuPrice().endsWith("_")){
                    skuPrice.gte(s[0]);
                }
            }

            boolQueryBuilder.filter(skuPrice);
        }
        // 1.5按照attrs
        if(param.getAttrs()!=null && param.getAttrs().size()>0) {
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));

                NestedQueryBuilder attrs = QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(attrs);
            }

        }

        searchSourceBuilder.query(boolQueryBuilder);
            //排序,分页，高亮
            if(!StringUtils.isEmpty(param.getSort())) {
                String sort = param.getSort();
                String[] s = sort.split("_");
                SortOrder asc = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
                searchSourceBuilder.sort(s[0], asc);

            }

            // 分页
            searchSourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
            searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
            //高亮
            if(!StringUtils.isEmpty(param.getKeyword())){
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                highlightBuilder.field("skuTitle");
                highlightBuilder.preTags("<b style='color:red'>");
                highlightBuilder.postTags("</b>");
                searchSourceBuilder.highlighter(highlightBuilder);
            }


//        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("aggr_brand").field("brandName").size(50);
//            brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brand_name"));
//            brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brand_img"));
//            searchSourceBuilder.aggregation(brand_agg);
//        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId");
//        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName"));
//        searchSourceBuilder.aggregation(catalog_agg);

        //TODO分类聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);

        //TODO属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName"));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue"));
        attr_agg.subAggregation(attr_id_agg);
        searchSourceBuilder.aggregation(attr_agg);

//        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
//        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
//        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
//        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
//        attr_agg.subAggregation(attr_id_agg);
//        searchSourceBuilder.aggregation(attr_agg);

        String string = searchSourceBuilder.toString();
        System.out.println("构建的DSL语句："+string);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }
}
