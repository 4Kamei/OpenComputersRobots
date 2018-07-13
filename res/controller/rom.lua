local tab = loadfile("/lib/tablet_connect.lua")()

if(not tab) then
    print("Cannot")
    os.sleep(1)
    os.exit(1)
end

local fs = load_api("filesystem")

local function run()
    local f, d = loadfile("/home/tablet.lua")()(tab)
end

local h, err = pcall(run)

if(not h) then
    tab.send_packet("S_MESSAGE", err)
end

tab.send_packet("S_CLOSE")
tab.close()