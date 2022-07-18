package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
    @Autowired
    private RestHighLevelClient client;

    @Override
    public PageResult search(RequestParams params) {
        try {
            //1、准备Request
            SearchRequest request = new SearchRequest("hotel");
            //2、准备DSL
            //2.1query
            buildBasicQuery(params, request);

            //2.2分页
            int size = params.getSize();
            int page = params.getPage();
            request.source().from((page-1)*size).size(size);

            //2.3排序
            String location = params.getLocation();
            if(location!=null&&!location.equals("")){
                request.source().sort(SortBuilders
                        .geoDistanceSort("location", new GeoPoint(location))
                        .order(SortOrder.ASC)
                        .unit(DistanceUnit.KILOMETERS));
            }

            //3、发送请求
            SearchResponse response=client.search(request, RequestOptions.DEFAULT);
            //4、解析响应
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Map<String, List<String>> filters(RequestParams params) {
        try {
            //1、准备Request
            SearchRequest request=new SearchRequest("hotel");
            // 2、准备DSL
            //query
            buildBasicQuery(params, request);

            // 2.1设置size
            request.source().size(0);
            // 2.2聚合
            buildAggregation(request);
            //3、发送请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //4、解析结果
            Map<String,List<String>> result=new HashMap<>();
            Aggregations aggregations = response.getAggregations();
            //4.1根据品牌名称，获取品牌结果
            List<String> brandList = getAggByName(aggregations,"brandAgg");
            result.put("品牌",brandList);
            //4.2根据城市名称，获取品牌结果
            List<String> cityList = getAggByName(aggregations,"cityAgg");
            result.put("城市",cityList);
            //4.3根据星级名称，获取品牌结果
            List<String> starList = getAggByName(aggregations,"starAgg");
            result.put("星级",starList);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    List<String> getAggByName(Aggregations aggregations,String aggName) {
        // 4.1根据聚合名称获取聚合结构
        Terms brandTerms = aggregations.get(aggName);
        // 4.2获取buckets
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        // 4.3遍历
        List<String> brandList=new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            //4.4 获取key
            String key = bucket.getKeyAsString();
            brandList.add(key);
        }
        return brandList;
    }

    private void buildAggregation(SearchRequest request) {
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg")
                .field("brand")
                .size(100)
        );
        request.source().aggregation(AggregationBuilders
                .terms("cityAgg")
                .field("city")
                .size(100)
        );
        request.source().aggregation(AggregationBuilders
                .terms("starAgg")
                .field("starName")
                .size(100)
        );
    }

    void buildBasicQuery(RequestParams params, SearchRequest request) {
        //一、构造BooleanQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //关键字搜索must

        String key = params.getKey();
        if(key==null||"".equals(key)){
            boolQuery.must(QueryBuilders.matchAllQuery());
        }else {
            boolQuery.must(QueryBuilders.matchQuery("all",key));
        }
        //城市条件过滤
        if(params.getCity()!=null&&!params.getCity().equals("")){
            boolQuery.filter(QueryBuilders.termQuery("city",params.getCity()));
        }
        //品牌条件过滤
        if(params.getBrand()!=null&&!params.getBrand().equals("")){
            boolQuery.filter(QueryBuilders.termQuery("brand",params.getBrand()));
        }
        //星级条件过滤
        if(params.getStarName()!=null&&!params.getStarName().equals("")){
            boolQuery.filter(QueryBuilders.termQuery("starName",params.getStarName()));
        }
        //价格条件过滤
        if(params.getMinPrice()!=null&&params.getMaxPrice()!=null){
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(params.getMinPrice()).lte(params.getMaxPrice()));
        }

        //二、算分控制
        FunctionScoreQueryBuilder functionScoreQuery =
                QueryBuilders.functionScoreQuery(
                        //原始查询，相关性算分的查询
                        boolQuery,
                        //function score的数组
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                                //其中的一个function score元素
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                        //过滤条件，满足条件isAD，设为true
                                        QueryBuilders.termsQuery("isAD",true),
                                        //算分函数
                                        ScoreFunctionBuilders.weightFactorFunction(10)
                                )
                        });

        request.source().query(functionScoreQuery);
    }



    private PageResult handleResponse(SearchResponse response){
        //4、解析相应
        SearchHits searchHits = response.getHits();
        //4.1获取的总条数
        long total = searchHits.getTotalHits().value;
        //4.2文档数组
        SearchHit[] hits = searchHits.getHits();
        //4.3遍历
        List<HotelDoc> hotels=new ArrayList<>();
        for(SearchHit hit:hits){
            //获取文档source
            String json=hit.getSourceAsString();
            //反序列化成hotel对象数据
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            //获取排序值
            Object[] sortValues=hit.getSortValues();
            if(sortValues.length>0){
                Object sortValue=sortValues[0];
                hotelDoc.setDistance(sortValue);
            }

            hotels.add(hotelDoc);
        }
        //4.4封装返回
        return new PageResult(total,hotels);
    }
}
