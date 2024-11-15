package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.dto.CacheData;
import com.hmdp.entity.Shop;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


@SpringBootTest
class ShopServiceImplTest {

    @Resource
    private ShopServiceImpl shopService;
    @Test
    public void test1() {
        shopService.save(3L);
    }
}