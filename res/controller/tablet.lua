--
-- Created by IntelliJ IDEA.
-- User: Aleksander
-- Date: 12/07/2018
-- Time: 19:50
-- To change this template use File | Settings | File Templates.
--
return function(socket)

    local fs = load_api("filesystem")
    local term = load_api("term")

    local list = fs.list("/home/functions")
    local array = {}
    for k in list do
        table.insert(array, k)
    end

    term.clear()
    for i,v in ipairs(array) do
        term.write("[" .. i .. "] -> " .. v .. "\n")
    end
    term.write("Selection: ")
    local s = tonumber(io.read())
    term.write(array[s])
    loadfile("/home/functions/" .. array[s])()(socket)

end