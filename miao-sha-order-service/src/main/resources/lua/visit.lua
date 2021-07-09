local key= KEYS[1]
local allkey= KEYS[2]
local daycountkey= KEYS[3]
local visitKey=ARGV[1]
local dayVisitKey=ARGV[2]
local count=tonumber(redis.call("hget",key,visitKey) or 0)
local all_count=tonumber(redis.call("get",allkey) or 0)
local day_count=tonumber(redis.call("getbit",daycountkey,dayVisitKey) or 0)
count=count+1
all_count=all_count+1
redis.call("hset",key,visitKey,count)
redis.call("set",allkey,all_count)
if day_count==0 then
    redis.call("setbit",daycountkey,dayVisitKey,1)
end

return all_count



