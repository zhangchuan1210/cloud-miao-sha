local key= KEYS[1]
local arg=ARGV[1]

local count=tonumber(redis.call("get",key) or 0)
if count >0 then
    count=count-1
    redis.call("set",key,count)
end

return count



