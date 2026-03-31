for i,key in ipairs(KEYS) do
    local val = redis.call('GET', key)
    if val ~= ARGV[1] then
        return 0
    end
end

return 1