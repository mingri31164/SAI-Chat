-- Redis 分布式 Snowflake ID 生成器初始化脚本
-- 使用 Redis INCR 命令实现原子性递增，避免多实例冲突

-- Lua 脚本用于原子性地获取和更新 workerId 和 datacenterId
local key_prefix = "snowflake:id:"
local worker_key = key_prefix .. "workerId"
local datacenter_key = key_prefix .. "datacenterId"

-- 获取或初始化 workerId (0-31)
local workerId = redis.call('GET', worker_key)
if not workerId then
    workerId = 0
else
    workerId = tonumber(workerId)
end

-- 获取或初始化 datacenterId (0-31)
local datacenterId = redis.call('GET', datacenter_key)
if not datacenterId then
    datacenterId = math.floor(math.random() * 32)  -- 随机选择 0-31
    redis.call('SET', datacenter_key, datacenterId)
else
    datacenterId = tonumber(datacenterId)
end

return {workerId, datacenterId}
