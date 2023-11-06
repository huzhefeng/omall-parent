package com.offcn.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.common.result.Result;
import com.offcn.list.repostiory.GoodsRepository;
import com.offcn.list.service.SearchService;
import com.offcn.model.list.*;
import com.offcn.model.product.*;
import com.offcn.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    //注入ES数据操作接口
    @Autowired
    private GoodsRepository goodsRepository;

    //注入商品微服务Feign接口
    @Autowired
    private ProductFeignClient productFeignClient;

    //注入redis操作工具对象
    @Autowired
    private RedisTemplate redisTemplate;

    //注入操作ES客户端
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void upperGoods(Long skuId) {

        //创建一个商品对象
        Goods goods = new Goods();

        //调用feign接口获取对应sku编号的平台属性
        List<BaseAttrInfo> attrInfoList = productFeignClient.getAttrList(skuId);
        //判断平台属性是否为空
        if(!CollectionUtils.isEmpty(attrInfoList)){
            List<SearchAttr> searchAttrList = attrInfoList.stream().map(baseAttrInfo -> {
                //创建封装平台属性对象
                SearchAttr searchAttr = new SearchAttr();
                //设置属性1
                searchAttr.setAttrId(baseAttrInfo.getId());
                //设置属性2
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                //从baseAttrInfo获取属性值集合
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                //判断集合是否为空
                if (!CollectionUtils.isEmpty(attrValueList)) {
                    //提取第一组平台属性值设置到属性3
                    searchAttr.setAttrValue(attrValueList.get(0).getValueName());
                }
                return searchAttr;
            }).collect(Collectors.toList());

            //把获取到平台属性集合设置goods
            goods.setAttrs(searchAttrList);
        }

        //调用feign接口查询sku信息
        Result<SkuInfo> skuInfoResult = productFeignClient.getSkuInfo(skuId);
        SkuInfo skuInfo = skuInfoResult.getData();
        //调用feign接口查询品牌数据
        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
        //判断品牌对象是否为空
        if(trademark!=null){
            //把品牌相关参数设置到goods
            goods.setTmId(trademark.getId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());
        }

        //调用分类数据接口
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        if(categoryView!=null){
            //把获取分类参数设置到goods
            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Name(categoryView.getCategory3Name());
        }

        //设置配图
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        //设置价格
        goods.setPrice(skuInfo.getPrice().doubleValue());
        //sku编号
        goods.setId(skuInfo.getId());
        //商品名称
        goods.setTitle(skuInfo.getSkuName());
        //设置创建时间
        goods.setCreateTime(new Date());

        //调用ES数据操作接口，保存到搜索引擎
        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
       goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
       //定义一个key，记录商品浏览次数
        String hotKey="hotScore";
        //操作redis，使用zset记录浏览次数 +1
        Double score = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);
        //把返回浏览次数去和10求余数
        if(score%10==0){
            //把数据更新到ES
            //根据sku编号，去ES查询数据
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            //更新浏览次数
            goods.setHotScore(Math.round(score));
            //更新保存到ES
            goodsRepository.save(goods);
        }
    }

    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {
        SearchRequest searchRequest = buildQueryDsl(searchParam);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("响应数据:"+searchResponse);
        //调用解析方法，解析响应对象
        SearchResponseVo searchResponseVo = parseSearchResult(searchResponse);
        //设置当前页码
        searchResponseVo.setPageNo(searchParam.getPageNo());
        //设置每页显示记录数
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //计算总页码
        long totalPages=(searchResponseVo.getTotal()+searchParam.getPageSize()-1)/searchParam.getPageSize();
        searchResponseVo.setTotalPages(totalPages);
        return searchResponseVo;
    }

    //单独定义一个方法：解析响应结果，组装成SearchResponseVo
    private SearchResponseVo parseSearchResult(SearchResponse response){
        SearchResponseVo searchResponseVo=new SearchResponseVo();

        //获取搜索结果封装对象
        SearchHits hits = response.getHits();
        //从响应对象，获取聚合结果
        Map<String, Aggregation> aggregationMap  = response.getAggregations().getAsMap();
        //在去获取 品牌聚合结果
       ParsedLongTerms tmIdAgg= (ParsedLongTerms) aggregationMap.get("tmIdAgg");

        List<SearchResponseTmVo> SearchResponseTmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
            //创建品牌响应Vo对象
            SearchResponseTmVo tmVo = new SearchResponseTmVo();
            //设置品牌编号
            tmVo.setTmId(Long.parseLong(bucket.getKeyAsString()));

            //获取子分组:按照品牌名称进行分组
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) bucket.getAggregations().asMap().get("tmNameAgg");
            tmVo.setTmName(tmNameAgg.getBuckets().get(0).getKeyAsString());

            //获取子分组，按照品牌logo进行分组
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) bucket.getAggregations().asMap().get("tmLogoUrlAgg");
            tmVo.setTmLogoUrl(tmLogoUrlAgg.getBuckets().get(0).getKeyAsString());
            return tmVo;
        }).collect(Collectors.toList());

        //把获取到品牌分组结果设置到统一响应对象
        searchResponseVo.setTrademarkList(SearchResponseTmVoList);

        //处理搜索结果

        SearchHit[] subHits = hits.getHits();
        //定义一个集合存储当前页商品数据
        List<Goods> goodsList=new ArrayList<>();
        //判断搜索结果数组是否为空
        if(subHits!=null&&subHits.length>0){
            //遍历数组
            for (SearchHit searchHit : subHits) {
                Goods goods = JSON.parseObject(searchHit.getSourceAsString(), Goods.class);
                //获取标题高亮结果
                if(searchHit.getHighlightFields().get("title")!=null){
                    //获取标题高亮结果
                    Text title = searchHit.getHighlightFields().get("title").getFragments()[0];
                    //使用高亮标题替换掉商品标题
                    goods.setTitle(title.toString());
                }

                //把商品对象加入到goodsList
                goodsList.add(goods);

            }
        }
        //把商品对象集合封装到响应对象
        searchResponseVo.setGoodsList(goodsList);

        //获取平台属性聚合结果
        ParsedNested attrAgg= (ParsedNested) aggregationMap.get("attrAgg");
       ParsedLongTerms attrIdAgg= attrAgg.getAggregations().get("attrIdAgg");
       //获取attrIdAgg 存储桶
        List<? extends Terms.Bucket> idAggBuckets = attrIdAgg.getBuckets();
        //判断存储桶是否为空
        if(!CollectionUtils.isEmpty(idAggBuckets)){
            List<SearchResponseAttrVo> SearchResponseAttrVoList = idAggBuckets.stream().map(bucket -> {
                //创建平台属性响应封装对象
                SearchResponseAttrVo attrVo = new SearchResponseAttrVo();
                //设置编号
                attrVo.setAttrId(bucket.getKeyAsNumber().longValue());

                //获取子分组
                ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
                //设置平台属性名称
                attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());

                //获取属性值分组结果
                ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
                //判断是否为空
                if (attrValueAgg != null) {
                    List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                    //遍历存储桶
                    List<String> attrValueList = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    //把attrValueList设置平台属性响应封装对象
                    attrVo.setAttrValueList(attrValueList);
                }

                return attrVo;

            }).collect(Collectors.toList());

            //关联到响应对象
            searchResponseVo.setAttrsList(SearchResponseAttrVoList);
        }

        //设置总记录数
        searchResponseVo.setTotal(hits.getTotalHits());


        return searchResponseVo;
    }

    //单独定义一个方法，拼接查询dsl语句，返回查询参数请求对象
    private SearchRequest buildQueryDsl(SearchParam searchParam){
        //定义查询参数请求对象
        SearchRequest searchRequest = new SearchRequest("goods");


        //创建主查询条件构建器对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //创建多条件查询器对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //判断查询参数是否传递了查询关键字
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            //创建按照查询关键字查询对象
           /* "match": {
                "title": "手机"
            }*/
            MatchQueryBuilder titleQuery = QueryBuilders.matchQuery("title", searchParam.getKeyword());

            //把关键字查询条件对象关联到多条件查询器对象
            boolQueryBuilder.must(titleQuery);
        }

        //处理品牌查询条件
        String trademark = searchParam.getTrademark();
        //判断品牌查询条件是否为空
        if(!StringUtils.isEmpty(trademark)){
            //2:华为 使用 ：切开变成数组
            String[] split = trademark.split(":");
            //判断数组是否为空
            if(split!=null&&split.length==2){
                //创建按照品牌查询条件
                TermQueryBuilder tmQuery = QueryBuilders.termQuery("tmId", split[0]);
                //关联品牌查询条件到多条件查询器对象
                boolQueryBuilder.filter(tmQuery);
            }
        }

        //判断一级分类查询条件是否为空
        if(!StringUtils.isEmpty(searchParam.getCategory1Id())){
            TermQueryBuilder category1Query = QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id());
           //把一级分类查询条件关联到多条件查询器对象
            boolQueryBuilder.filter(category1Query);
        }

        //判断2级分类查询条件是否为空
        if(!StringUtils.isEmpty(searchParam.getCategory2Id())){
            TermQueryBuilder category2Query = QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id());
            //把一级分类查询条件关联到多条件查询器对象
            boolQueryBuilder.filter(category2Query);
        }

        //判断3级分类查询条件是否为空
        if(!StringUtils.isEmpty(searchParam.getCategory3Id())){
            TermQueryBuilder category3Query = QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id());
            //把一级分类查询条件关联到多条件查询器对象
            boolQueryBuilder.filter(category3Query);
        }

        //处理平台属性查询

        //获取平台属性参数值
        String[] props = searchParam.getProps();
        if(props!=null&&props.length>0){
            //遍历数组
            for (String prop : props) {
                // 23:4G:运行内存
                //使用 ：切开成数组
                String[] split = prop.split(":");
                //判断数组是否为空
                if(split!=null&&split.length==3){
                    //创建一个 多条件查询对象
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    //创建一个子多条件查询对象
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                    //在子多条件查询对象，设置查询条件
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));

                    //使用nested类型关联子查询条件对象
                    boolQuery.must(QueryBuilders.nestedQuery("attrs",subBoolQuery, ScoreMode.None));
                    //把多条件查询对象 关联到总的 多条件查询对象
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }

        //把多条件查询对象关联到主 查询构建器对象
        searchSourceBuilder.query(boolQueryBuilder);

        //处理分页

        //计算开始页码
        int from=(searchParam.getPageNo()-1)*searchParam.getPageSize();
        //设置开始页码到查询构建器对象
        searchSourceBuilder.from(from);
        //设置每页显示记录数
        searchSourceBuilder.size(searchParam.getPageSize());

        //处理排序
        String order = searchParam.getOrder();
        //判断排序字符串是否为空
        if(!StringUtils.isEmpty(order)){
            //1:asc 使用 ：切开成数组
            String[] split = order.split(":");
            //判断数组是否为空
            if(split!=null&&split.length==2){
                //定义一个排序字段名
                String field=null;
                //判断排序编号
                switch (split[0]){
                    case "1":
                        field="hotScore";
                        break;
                    case "2":
                        field="price";
                        break;
                }

                //设置排序条件
                searchSourceBuilder.sort(field, "asc".equalsIgnoreCase(split[1]) ? SortOrder.ASC : SortOrder.DESC);

            }else {
                //设置默认排序
                searchSourceBuilder.sort("hotScore",SortOrder.DESC);
            }
        }

        //处理高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //设置高亮参数
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        //把高亮对象关联到主查询构建器对象
        searchSourceBuilder.highlighter(highlightBuilder);

        //处理品牌分组
        TermsAggregationBuilder TmAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName").size(10))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl").size(10));
        //把品牌分组关联到主查询构建器对象
        searchSourceBuilder.aggregation(TmAggregationBuilder);


        //处理平台属性分组
        NestedAggregationBuilder nestedAttrsAggregationBuilder = AggregationBuilders.nested("attrAgg", "attrs");

        //创建attrIdAgg 分组
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").size(10);

        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName").size(10));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue").size(10));

        //把attrIdAgg 分组关联到平台属性分组
        nestedAttrsAggregationBuilder.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(nestedAttrsAggregationBuilder);

        //定制要显示字段
        searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"},null);


        //把查询构建器对象关联设置请求参数封装对象
        searchRequest.source(searchSourceBuilder);

        //打印dsl语句
        System.out.println("DSL:"+searchSourceBuilder.toString());

        return searchRequest;
    }
}
