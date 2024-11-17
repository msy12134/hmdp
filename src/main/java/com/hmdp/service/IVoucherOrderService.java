package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;

import java.io.IOException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucherVersion1(Long voucherId) throws InterruptedException;

    Result seckillVoucherVersion2(Long voucherId) throws InterruptedException, IOException;


    Result createVoucherOrder(Long voucherId, Long userId);
}
