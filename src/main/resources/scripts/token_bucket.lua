-- 令牌桶限流算法实现
-- KEYS[1] : 限流key
-- ARGV[1] : 桶容量
-- ARGV[2] : 令牌生成速率(每秒)
-- ARGV[3] : 当前请求的令牌数
-- ARGV[4] : 当前时间戳(秒)

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local requested = tonumber(ARGV[3])
local now = tonumber(ARGV[4])

local bucket_key = "rate_limit:" .. key
local last_refill_key = "rate_limit:" .. key .. ":last_refill"

-- 获取当前桶中的令牌数和上次填充时间
local bucket = redis.call('GET', bucket_key)
local last_refill = redis.call('GET', last_refill_key)

-- 如果是第一次访问，初始化桶
if bucket == false then
    bucket = capacity
    last_refill = now
    redis.call('SET', bucket_key, bucket)
    redis.call('SET', last_refill_key, last_refill)
    redis.call('EXPIRE', bucket_key, 86400) -- 24小时过期
    redis.call('EXPIRE', last_refill_key, 86400)
else
    bucket = tonumber(bucket)
    last_refill = tonumber(last_refill)

    -- 计算距离上次填充经过的时间
    local elapsed = now - last_refill

    -- 根据时间差补充令牌
    if elapsed > 0 then
        local new_tokens = math.floor(elapsed * rate)
        bucket = math.min(capacity, bucket + new_tokens)
        last_refill = now

        -- 更新桶和时间戳
        redis.call('SET', bucket_key, bucket)
        redis.call('SET', last_refill_key, last_refill)
    end
end

-- 判断是否有足够的令牌
if bucket >= requested then
    -- 消费令牌
    bucket = bucket - requested
    redis.call('SET', bucket_key, bucket)
    return 1  -- 允许访问
else
    return 0  -- 拒绝访问
end
