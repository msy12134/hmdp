--判断库存，用户是否已经下单并执行相应操作
--KEYS[1] 秒杀券的key
--KEYS[2] 用户已经下单的集合键
--ARGV[1] 用户id
local stock=tonumber(redis.call('get',KEYS[1]))
if stock<=0 then
    --库存小于0，返回1
    return 1
end
local isOrdered=redis.call('sismember',KEYS[2],ARGV[1])
if isOrdered==1 then
    --用户已经下单，返回2
    return 2
end
redis.call('decr',KEYS[1])
redis.call('sadd',KEYS[2],ARGV[1])

--秒杀成功，返回0
return 0