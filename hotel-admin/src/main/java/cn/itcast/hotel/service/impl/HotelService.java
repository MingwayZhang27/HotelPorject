package cn.itcast.hotel.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.service.IHotelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result selectById(Long id) {
        String key="cache:hotel"+id;
        //1、从redis查询商铺缓存
        String hotelJson = stringRedisTemplate.opsForValue().get("cache:hotel" + id);

        //2、判断是否存在
        if(StrUtil.isNotBlank(hotelJson)){
            //3、存在，直接返回
            Hotel hotel= JSONUtil.toBean(hotelJson,Hotel.class);
            return Result.ok(hotel);
        }

        //4、不存在，根据id查询数据库
        Hotel hotel=getById(id);

        //5、不存在，返回错误
        if(hotel==null){
            return Result.fail("酒店不存在！");
        }

        //6、存在，写入redis
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(hotel));

        //7、返回
        return Result.ok(hotel);
    }
}
