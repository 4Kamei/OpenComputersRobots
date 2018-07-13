--
-- Created by IntelliJ IDEA.
-- User: Aleksander
-- Date: 13/07/2018
-- Time: 14:59
-- To change this template use File | Settings | File Templates.
--

-- Created by IntelliJ IDEA.
-- User: Aleksander
-- Date: 11/07/2018
-- Time: 20:38
-- To change this template use File | Settings | File Templates.
--



local json = require("json")
local net = require("component").internet
local component_invoke = require("component").invoke
local computer = require("computer")

local connection = {}

local function boot_invoke(address, method, ...)
    local result = table.pack(pcall(component_invoke, address, method, ...))
    if not result[1] then
        return nil, result[2]
    else
        return table.unpack(result, 2, result.n)
    end
end

local soc, reason = boot_invoke(net.address, "connect", "localhost", 12345)

if(not soc) then
    return nil, "Could not connect " .. reason
end

function connection.send_packet(type, content, ...)
    local head  = { ... }
    local packet = {}
    packet["header"] = head
    packet["type"] = type
    local cont = json.encode(content)
    packet["content"] = cont;
    packet["content-size"] = #cont
    soc.write(json.encode(packet) .. "\n")
end

function connection.recieve_packet()
    local str = ""
    while (true) do
        line = soc.read()

        if(#line > 0 and line) then
            str = str .. line
        elseif (#str > 0) then
            local result, content = pcall(json.decode, str)

            if(not result or not (type(content) == "table")) then
                return nil, "couldn't decode packet"
            end
            return content;
        end
    end
end

function connection.close()
    soc.close()
end

local init_with_server = function()

    local pos = {}

    pos["address"] = computer.address()
    pos["type"] = "server"

    soc.read()

    connection.send_packet("S_INIT", pos)

    local data = connection.recieve_packet()

    if(data and data.type == "C_ACCEPT_INIT") then
        connection.send_packet("S_ACCEPT_INIT", nil)
        return true
    else
        connection.send_packet("S_DENY_INIT", nil)
    end
    return false
end
local r = init_with_server()
if(not r) then
    return nil, "Init refused"
else
    return connection
end

