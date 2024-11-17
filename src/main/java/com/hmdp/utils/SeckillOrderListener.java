package com.hmdp.utils;
import com.hmdp.config.RabbitMQConfig;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.service.impl.VoucherOrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class SeckillOrderListener {

    @Autowired
    private IVoucherOrderService voucherOrderService;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void handleSeckillOrder(VoucherOrderServiceImpl.SeckillOrderMessage message) {
        try {
            voucherOrderService.createVoucherOrder(message.getVoucherId(), message.getUserId());
        } catch (Exception e) {
            // 处理异常，例如记录日志或重试
            log.error("处理秒杀订单失败, voucherId: {}, userId: {}", message.getVoucherId(), message.getUserId(), e);
            // 可以考虑将失败的消息重新发送到队列或存储到数据库以便后续处理
        }
    }
}
