--
-- Created by IntelliJ IDEA.
-- User: Aleksander
-- Date: 11/07/2018
-- Time: 14:51
-- To change this template use File | Settings | File Templates.
--

local function load_library(name, needed)
    local api, error = dofile("/api/" .. name .. ".lua")
    if(api) then
        return api
    end
    if (needed) then
        error("Could not load api " .. name)
    else
        return nil, error
    end
end

return function()

    seri = load_library("json", true)

    sides = load_library("sides", true)

    dbg = component.proxy(component.list("debug")())

    geo = component.proxy(component.list("geolyzer")())

    robot = component.proxy(component.list("robot")())

    send_packet = function(type, content, ...)
        local head  = { ... }
        local packet = {}
        packet["header"] = head
        packet["type"] = type
        local cont = seri.encode(content)
        packet["content"] = cont;
        packet["content-size"] = #cont
        soc.write(seri.encode(packet) .. "\n")
    end

    write_local = function(message)
        dbg.runCommand("tell theinfamouspig " .. message)
    end

    busy_wait = function(count)
        local ntime = os.clock() + count
        repeat until os.clock() > ntime
    end

    recieve_packet = function()
        local line = ""
        local str = ""
        while (true) do
            line = soc.read()

            if(#line > 0 and line) then
                str = str .. line
            elseif (#str > 0) then
                local result, content = pcall(seri.decode, str)

                if(not result or not (type(content) == "table")) then
                    return nil, "couldn't decode packet"
                end
                return content;
            end
        end
    end

    init_with_server = function()

        local pos = {}

        pos["address"] = computer.address()
        pos["type"] = "server"

        soc.read()

        send_packet("S_INIT", pos)
        write_local("Sent init")

        local data = recieve_packet()
        write_local("Recieved Packet")

        if(data and data.type == "C_ACCEPT_INIT") then
            write_local("Finished Init")
            send_packet("S_ACCEPT_INIT", nil)
            return true
        else
            write_local("Rejecting init")
            send_packet("S_DENY_INIT", nil)
        end
        return false
    end

    write_local("Controller identified as " .. robot.name())

    init_with_server()

    soc.close()
end

