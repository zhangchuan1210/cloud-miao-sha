local key= KEYS[1]
local all= "all_visit_count"
local visitKey=ARGV[1]
local count=tonumber(redis.call("hget",key,visitKey) or 0)
local all_count=tonumber(redis.call("get",all) or 0)
count=count+1
all_count=all_count+1
redis.call("hset",key,visitKey,count)
redis.call("set",all,all_count)
return all_count



