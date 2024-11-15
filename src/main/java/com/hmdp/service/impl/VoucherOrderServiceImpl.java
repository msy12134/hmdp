package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisUniqueIdCreater;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.ZoneOffset;


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
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisUniqueIdCreater RedisUniqueIdCreater;

    @Resource
    private ApplicationContext applicationContext; //从IOC容器中获取bean,用于解决事务失效问题,不要直接注入bean，会导致循环依赖
    @Override
    public Result seckillVoucher(Long voucherId) throws InterruptedException {
        // 获取秒杀券信息
        SeckillVoucher seckillVoucher = seckillVoucherService.getOne(new QueryWrapper<SeckillVoucher>().eq("voucher_id", voucherId));
        if (seckillVoucher == null) {
            return Result.fail("秒杀券不存在");
        }
        // 判断库存
        if (seckillVoucher.getStock() <= 0) {
            return Result.fail("库存不足");
        }
        // 判断秒杀时间
        long now = System.currentTimeMillis();
        if (seckillVoucher.getBeginTime().toInstant(ZoneOffset.UTC).toEpochMilli() > now || seckillVoucher.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli() < now) {
            return Result.fail("不在秒杀时间内");
        }
        //判断是否已经秒杀过
        Long userId = UserHolder.getUser().getId();
        long count = count(new QueryWrapper<VoucherOrder>().eq("user_id", userId).eq("voucher_id", voucherId));
        if (count > 0) {
            return Result.fail("每人只能秒杀一次");
        }
        String lockKey = "lock:order:" + userId + ":" + voucherId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            return Result.fail("每人只能秒杀一次");
        }
        try {
            // 判断是否已经秒杀过
            long count1 = count(new QueryWrapper<VoucherOrder>().eq("user_id", userId).eq("voucher_id", voucherId));
            if (count1 > 0) {
                return Result.fail("每人只能秒杀一次");
            }
            //防止事务失效，所以使用代理对象调用创建订单的方法createVoucherOrder
            IVoucherOrderService proxy = applicationContext.getBean(IVoucherOrderService.class);
            return proxy.createVoucherOrder(voucherId, userId);
        }
        finally {
            lock.unlock();
        }
    }
    // 删除一张秒杀券，然后创建相应订单
    @Transactional
    public Result createVoucherOrder(Long voucherId, Long userId) {
        // 扣减库存，使用乐观锁
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if (!success) {
            return Result.fail("库存不足");
        }
        // 创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setUserId(userId);
        voucherOrder.setId(RedisUniqueIdCreater.nextId("voucher_order"));
        save(voucherOrder);
        return Result.ok("秒杀成功");
    }
}