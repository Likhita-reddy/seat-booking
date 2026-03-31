-- input keys, args: userId, expiry

for i, key in ipairs(KEYS) do
    if redis.call('EXISTS',key) == 1 then
        for j = 1, i-1 do
            redis.call('DEL', keys[j])
        end
        return 0
    end
end

for i, key in ipairs(KEYS) do
    redis.call('SET',key,ARGV[1],'EX',ARGV[2])
end
return 1