package cn.itcast.hotel;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.ml.EvaluateDataFrameRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static cn.itcast.hotel.HotelConstants.MAPPING_TEMPLATE;

public class HotelIndexTest {
    private RestHighLevelClient client;

    @Test
    void testInit() {
        System.out.println(client);
    }

    //一、创建索引库
    @Test
    void createHotelIndex() throws IOException {
        //1、创建Request对象
        CreateIndexRequest request = new CreateIndexRequest("hotel");

        //2、准备请求的参数：DSL语句
        request.source(MAPPING_TEMPLATE, XContentType.JSON);

        //3、发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    //二、删除索引库
    @Test
    void testDeleteHotelIndex() throws IOException {
        //1、创建Request对象
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        //2、发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    //三、判断索引库是否存在
    @Test
    void testExistsHotelIndex() throws IOException {
        //1、创建Request对象
        GetIndexRequest request = new GetIndexRequest("hotel");
        //2、发送请求
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        //3、输出
        System.err.println(exists?"索引已存在！":"索引不存在！");
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
