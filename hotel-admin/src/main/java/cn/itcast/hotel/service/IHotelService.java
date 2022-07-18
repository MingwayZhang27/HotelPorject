package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;

public interface IHotelService extends IService<Hotel> {

    Result selectById(Long id);

}
