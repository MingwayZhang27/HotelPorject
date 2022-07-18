package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.watcher.ResourceWatcherService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.itcast.hotel.HotelConstants.MAPPING_TEMPLATE;

public class HotelSearch {
    private RestHighLevelClient client;

   @Test
   void testMatchAll() throws IOException {
       //1、准备Request
       SearchRequest request = new SearchRequest("hotel");
       //2、准备DSL
//       request.source().query(QueryBuilders.matchAllQuery());
       request.source().query(QueryBuilders.matchQuery("all","如家"));

       //3、发送请求
       SearchResponse response=client.search(request,RequestOptions.DEFAULT);
       //4、解析响应
       handleResponse(response);
   }

    @Test
    void testBool() throws IOException {
        //1、准备Request
        SearchRequest request = new SearchRequest("hotel");
        //2、准备DSL
        //2.1准备BooleanQuery
        BoolQueryBuilder boolQuery=QueryBuilders.boolQuery();
        //2.2添加term
//        boolQuery.must(QueryBuilders.termQuery("city","杭州"));
        //2.3添加range
        boolQuery.filter(QueryBuilders.rangeQuery("price").lte(250));

        request.source().query(boolQuery);
        //3、发送请求
        SearchResponse response=client.search(request,RequestOptions.DEFAULT);
        //4、解析响应
        handleResponse(response);
    }

    @Test
    void testPageAndSort() throws IOException {
       //页码，每页大小
        int page=1,size=5;
        //1、准备Request
        SearchRequest request = new SearchRequest("hotel");
        //2、准备DSL
        //2.1query
        request.source().query(QueryBuilders.matchAllQuery());
        //2.2排序sort
        request.source().sort("price", SortOrder.ASC);
        //2.3分页from,size
        request.source().from((page-1)*size).size(size);

        //3、发送请求
        SearchResponse response=client.search(request,RequestOptions.DEFAULT);
        //4、解析响应
        handleResponse(response);
    }

    @Test
    void testHighlight() throws IOException {
        //1、准备Request
        SearchRequest request = new SearchRequest("hotel");
        //2、准备DSL
        //2.1query
        request.source().query(QueryBuilders.matchQuery("all","如家"));
        //2.2高亮
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        //3、发送请求
        SearchResponse response=client.search(request,RequestOptions.DEFAULT);
        //4、解析响应
        handleResponse(response);
    }

    void handleResponse(SearchResponse response) {
        //4、解析相应
        SearchHits searchHits = response.getHits();
        //4.1获取的总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("共搜索到"+total+"条数据");
        //4.2文档数组
        SearchHit[] hits = searchHits.getHits();
        //4.3遍历
        for(SearchHit hit:hits){
            //获取文档source
            String json=hit.getSourceAsString();
            //反序列化成hotel对象数据
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            //获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            //健壮性判断
            if(!CollectionUtils.isEmpty(highlightFields)){
                //根据字段名获取高亮结果
                HighlightField highlightField = highlightFields.get("name");
                if(highlightField!=null){
                    //获取高亮值
                    String name = highlightField.getFragments()[0].toString();
                    //覆盖高亮结果
                    hotelDoc.setName(name);
                }
            }
            System.out.println(hotelDoc);
        }

    }

    @Test
    void testAggregation() throws IOException {
        //1、准备Request
        SearchRequest request=new SearchRequest("hotel");
        // 2、准备DSL
        // 2.1设置size
        request.source().size(0);
        // 2.2聚合
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg")
                .field("brand")
                .size(10)
        );
        //3、发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4、解析结果
        Aggregations aggregations = response.getAggregations();
        // 4.1根据聚合名称获取聚合结构
        Terms brandTerms = aggregations.get("brandAgg");
        // 4.2获取buckets
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        // 4.3遍历
        for (Terms.Bucket bucket : buckets) {
            //4.4 获取key
            String key = bucket.getKeyAsString();
            System.out.println(key);
        }

    }

    //客户端初始化
    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.32.3:9200")
        ));
    }

    //用完之后销毁
    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

}
