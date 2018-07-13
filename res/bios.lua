--
-- Created by IntelliJ IDEA.
-- User: Aleksander
-- Date: 12/07/2018
-- Time: 19:35
-- To change this template use File | Settings | File Templates.
--

local component_invoke = component.invoke
local inet_addr, pl = component.list("internet")()
local addr, pl = component.list("robot")()

local file = "core/init.lua"

function boot_invoke(address, method, ...)
    local result = table.pack(pcall(component_invoke, address, method, ...))
    if not result[1] then
        return nil, result[2]
    else
        return table.unpack(result, 2, result.n)
    end
end

function request(file)
    local soc, err = boot_invoke(inet_addr, "request",  "http://localhost/" .. file, 80)
    if(not soc) then
        print("couldn't fetch " .. file)
        return nil
    end
    local str = ""
    local line = ""
    while(not soc.finishConnect()) do end
    repeat
        line, err = soc.read()
        if line then
            str = str .. line
        end
    until(not line)
    return str
end

local src = request(file)

f, err =  load(src)

if(err) then
    error(err)
end

func = f()

func()

--handle, err = fs.open("/home/testF", "w")
--for k, v in pairs(tab) do
-- print(v)
--str = request("files" .. v)
--end

