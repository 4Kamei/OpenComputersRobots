return function(socket)

  local navi = require("component").navigation

  local s = require("json")

  local nav = {}

  nav["getPosition"] = function()
    local x, y, z = navi.getPosition()
    return math.floor(x), math.floor(y), math.floor(z)
  end

  local term = require("term")
  
  local pos_offset = {}

  local zero_pos_waypoint = function(waypoint_name)

        local wp = navi.findWaypoints(1000)
        local waypoint
        local ind = wp["n"]
        for i = 1,ind do
            local v = wp[i]
            if v["label"] == waypoint_name then
                waypoint = v["position"]
            end
        end

        if not waypoint then
            write_local("could not find waypoint " .. waypoint_name)
        end

        local x, y, z = nav.getPosition()
        x = x + waypoint[1]
        y = y + waypoint[2]
        z = z + waypoint[3]

        pos_offset["x"] = -x
        pos_offset["y"] = -y
        pos_offset["z"] = -z

    end

    local get_pos = function()
        local p = {}
        local x, y, z = nav.getPosition()
        p["x"] = pos_offset.x + x
        p["y"] = pos_offset.y + y
        p["z"] = pos_offset.z + z
        return p;
    end

  term.clear()
  term.write("Zero to waypoint: ")
  local wp = io.read()
  zero_pos_waypoint(wp)
  term.write("FirstPos?")
  io.read()
  pos = get_pos()
  term.write("Second pos?")
  io.read()
  pos2 = get_pos()

  cont = {}
  cont["x"] = math.min(pos["x"], pos2["x"])
  cont["y"] = math.max(pos["y"], pos2["y"])
  cont["z"] = math.min(pos["z"], pos2["z"])

  cont["sizeX"] = math.abs(pos["x"] - pos2["x"]) + 1
  cont["sizeY"] = math.abs(pos["y"] - pos2["y"]) + 1
  cont["sizeZ"] = math.abs(pos["z"] - pos2["z"]) + 1

  for k,v in pairs(cont) do
    term.write("[" .. k .. "] -> " .. tostring(v) .. "\n")
  end
  term.write("\nConfirm?")
  io.read()

  local packet = {}
  packet["type"] = "MINE_PROJECT"
  packet["data"] = s.encode(cont)
  socket.send_packet("S_NEW_PROJECT", packet)

end