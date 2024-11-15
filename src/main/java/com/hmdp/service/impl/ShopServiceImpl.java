package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.config.BaseException;
import com.hmdp.dto.CacheData;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    //使用逻辑过期解决缓存击穿问题
    public Result queryShopById2(Long id){
        // 从redis中查询
        String shopJSON = stringRedisTemplate.opsForValue().get("shop:" + id);
        if (shopJSON == null) {
            //设置空值缓存，防止缓存穿透
            stringRedisTemplate.opsForValue().set("shop:" + id, "", 10, TimeUnit.SECONDS);
            return null;
        }
        //如果命中缓存，判断是否过期
        CacheData<Shop> cacheData = JSONUtil.toBean(shopJSON, CacheData.class);
        if (cacheData.getExpireTime() > System.currentTimeMillis()) {
            //缓存未过期，直接返回对象
            return Result.ok(cacheData.getData());
        }
        String lockKey = "lock:shop:" + id;
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 3, TimeUnit.SECONDS);
        //如果获取锁失败，直接返回过期对象
        if (Boolean.FALSE.equals(lock)) {
            return Result.ok(cacheData.getData());
        }
        //获取锁成功，开启独立线程查询数据库，建立缓存，最后释放锁，但是原线程还是直接返回过期数据
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                save(id);
            } finally {
                stringRedisTemplate.delete(lockKey);
            }
        });
        return Result.ok(cacheData.getData());

    }

    public void save(Long id){
        Shop shopInfo = getOne(new QueryWrapper<Shop>().eq("id", id));
        if (shopInfo != null) {
            // 写入redis
            stringRedisTemplate.opsForValue().set("shop:" + id, JSONUtil.toJsonStr(new CacheData<>(shopInfo, System.currentTimeMillis() + 10 *  1000)));
        } else {
            //如果数据库没有数据，redis缓存空对象
            stringRedisTemplate.opsForValue().set("shop:" + id, "", 30, TimeUnit.SECONDS);
        }
    }


    //使用互斥锁解决缓存击穿问题
    @Override
    public Result queryShopById(Long id) throws InterruptedException {
        // 从redis中查询
        String shopJSON = stringRedisTemplate.opsForValue().get("shop:" + id);
        if (shopJSON != null && !shopJSON.isEmpty()) {
            Shop shop = JSONUtil.toBean(shopJSON, Shop.class);
            return Result.ok(shop);
        } else if (shopJSON != null && shopJSON.isEmpty()) {
            return Result.fail("店铺不存在");
        }
        log.info("redis中未查询到数据，从数据库中查询");
        // 从数据库中查询
        String lockKey = "lock:shop:" + id;
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 3, TimeUnit.SECONDS);
        //如果获取锁失败，等待100ms后重试
        if (Boolean.FALSE.equals(lock)) {
            try {
                TimeUnit.MILLISECONDS.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            shopJSON = stringRedisTemplate.opsForValue().get("shop:" + id);
            if (shopJSON != null && !shopJSON.isEmpty()) {
                Shop shop = JSONUtil.toBean(shopJSON, Shop.class);
                return Result.ok(shop);
            } else if (shopJSON != null && shopJSON.isEmpty()) {
                return Result.fail("店铺不存在");
            }
        }
        //获取锁成功，查询数据库，建立缓存，最后释放锁
        try {
            Shop shopInfo = getOne(new QueryWrapper<Shop>().eq("id", id));
            if (shopInfo != null) {
                // 写入redis
                stringRedisTemplate.opsForValue().set("shop:" + id, JSONUtil.toJsonStr(shopInfo),10, TimeUnit.MINUTES);
            }else{
                //如果数据库没有数据，redis缓存空对象
                stringRedisTemplate.opsForValue().set("shop:" + id, "",30, TimeUnit.SECONDS);
                throw new BaseException("商铺不存在");
            }
            return Result.ok(shopInfo);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional
    public Result updateShop(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            throw new BaseException("商铺id不能为空");
        }
        //更新数据库之后再删除redis缓存
        update(shop,new UpdateWrapper<Shop>().eq("id", shop.getId()));
        stringRedisTemplate.delete("shop:" + shop.getId());
        return Result.ok();
    }
}
