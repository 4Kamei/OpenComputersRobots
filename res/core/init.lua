--
-- Created by IntelliJ IDEA.
-- User: Aleksander
-- Date: 12/07/2018
-- Time: 19:33
-- To change this template use File | Settings | File Templates.
--
-- TODO: DOWNLADING API FILES AUTOMATICALLY

if not print then
    local d = component.list("debug")()
    if d then
        local c = component.proxy(d)
        _G.print = function(message) c.runCommand("tell @p " .. tostring(message)) end
    else
        local l = component.list("screen")()
        if l then
            local g = component.list("gpu")()
            if not g then
                error("Screen but no gpu???")
            end
            g = component.proxy(g)
            g.bind(l)
            local wid, hei = g.getResolution()
            _G.print = function(str)
                g.copy(1, 1, wid, hei, 0, -1)
                g.fill(1, hei, wid, 1, ' ')
                g.set(1, hei , str)
            end

        else
            _G.print = function() end
        end
    end
end

print("In init")

local list = component.list("filesystem")
local fs_addr
for addr in list do
    local label = component.proxy(addr).getLabel()
    if(type(label) == "nil") then
        fs_addr = addr
        break
    end
end

_G.fs_comp = component.proxy(fs_addr)

function _G.load_library(name, needed)
    print("Loading " .. name)
    local handle = _G.fs_comp.open("/api/" .. name .. ".lua")
    if(not handle) then
        if needed then
            error("Could not find lib " .. name)
        else
            print("Could not load " .. name)
            return nil, "could not find"
            end
    end

    local tab_str = {}
    local str
    while true do
        local line = _G.fs_comp.read(handle, 20)
        if line then
            tab_str[#tab_str + 1] = line
        else
            str = table.concat(tab_str)
            break
        end
    end

    print("Loaded file " .. name)

    local func, error = load(str)
    if not func then
        if needed then
            error("Error in library " .. name)
        else
            print("Error in library " .. name)
            return nil, "error in library"
        end
    end

    local result, api = pcall(func)

    if(result) then
        print("Loaded api " .. name)
        return api
    end
    if (needed) then
        error("Could not load api " .. api)
    else
        return nil, api;
    end
end

local function get_net()
    local net_addr = component.list("internet")()
    if(not net_addr) then
        error("REQUIRES INTERNET CARD")
    end
    return component.proxy(net_addr)
end

local net = get_net()

local function request_file(file)
    local soc, err = net.request("http://localhost/" ..  file, 80)
    if(not soc) then
        print("couldn't fetch " .. file)
        return nil
    end
    local str = ""
    local line = ""
    print("Connecting for file " .. file)
    while(not soc.finishConnect()) do end
    print("Connected! for file " .. file)
    repeat
        line, err = soc.read()
        if line then
            str = str .. line
        end
    until(not line)
    return str
end


local function is_controller()
    return not(not tablet)
end

--TODO YOU WROTE THIS BUT DIDN'T TEST IT. SO TEST IT

local fs, error = load_library("filesystem", false)

if(not fs) then
    print("Fallback fs")
    local str = request_file("core/api/filesystem.lua")
    print("Loaded file from server")
    local dir = fs_comp.isDirectory("api")
    local handle
    if(not dir) then
        fs_comp.makeDirectory("api")
        handle = fs_comp.open("/api/filesystem.lua", 'w')
        print("Opened handle to file /api/filesystem.lua");
    else
        if(fs_comp.exists("api/filesystem.lua")) then
            fs_comp.remove("api/filesystem.lua")
            print("Removing old file")
        end
        handle = fs_comp.open("/api/filesystem.lua")
        print("Opened handle to file /api/filesystem.lua");
    end


    print("Handle = " .. tostring(handle));

    fs_comp.write(handle, str)
    fs_comp.close(handle)

    fs, error = load_library("filesystem", true)

end

local function download_write(name, req_name)
    print("Downloading " .. req_name .. " and saving as " .. name)
    local handle, err = fs.open(name, "w")
    if not handle then
        error(err)
    end
    local src = request_file(req_name)
    local suc, err = handle:write(src)
    if not suc then
        error(err)
    end
    handle:close()
end

print("GOT PAST ALL THE SHIT")

for k, v in pairs(fs) do
    print(k)
end


local file

local core = {}
table.insert(core, "api/filesystem.lua")
table.insert(core, "api/json.lua")
table.insert(core, "api/sides.lua")

local robot = {}
table.insert(robot, "api/a-star.lua")

local controller = {}
table.insert(controller, "functions/mine_project.lua")
table.insert(controller, "tablet.lua")

local todl

if(is_controller()) then
    file = "controller/"
    todl = controller
    local func_exists = fs.isDirectory("functions")
    if not func_exists then
            fs.makeDirectory("functions")
    end
else
    file = "robot/"
    todl = robot
end

print("file prefix = " .. file)

local api_exists = fs.isDirectory("api")
if not api_exists then
        fs.makeDirectory("api")
end

for _,v in pairs(core) do
    download_write(v, "core/" .. v)
end

for _,v in pairs(todl) do
    download_write(v, file .. v)
end

local src = request_file(file .. "rom.lua")
return loadfile(src)