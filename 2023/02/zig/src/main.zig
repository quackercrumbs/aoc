const std = @import("std");

pub fn main() !void {
    // Prints to stderr (it's a shortcut based on `std.io.getStdErr()`)
    std.debug.print("All your {s} are belong to us.\n", .{"codebase"});

    // read the program arguments
    var args = std.process.args();
    _ = args.skip();
    const inputFile = args.next().?;

    // try problem1(inputFile, Game{ .redMax = 12, .greenMax = 13, .blueMax = 14, .id = 0 });
    try problem2(inputFile);
}

const Game = struct {
    id: u8,
    redMax: u32,
    blueMax: u32,
    greenMax: u32,
};

const Round = struct {
    red: u32,
    blue: u32,
    green: u32,

    // yuck... using this to tell me where the cursor left off...
    cursor: usize,
};

fn isNumber(byte: u8) bool {
    return (byte >= 48) and (byte <= 57);
}

const RoundColor = struct {
    value: u32,
    cursor: usize,
};

fn parseRoundColor(line: []const u8, fromIndex: usize, roundEndIndex: usize, colorStr: []const u8) !?RoundColor {
    // parse colors
    if (std.mem.indexOfPos(u8, line, fromIndex, colorStr)) |colorIndex| {
        if (colorIndex <= roundEndIndex) {
            // walk backwards to read the numbers
            var index = colorIndex - 1;
            while (index >= fromIndex) {
                if (isNumber(line[index])) {
                    index -= 1;
                } else {
                    // Not a number
                    // Move cursor ahead since we only want to range of bytes that are numbers
                    index += 1;
                    break;
                }
            }
            //std.debug.print("{s}: {any} {any} {any}\n", .{ colorStr, index, colorIndex, fromIndex });
            const value = try std.fmt.parseInt(u32, line[index..colorIndex], 10);
            //std.debug.print("parse {any}\n", .{value});
            return RoundColor{ .value = value, .cursor = roundEndIndex };
        }
    }
    return null;
}

fn parseRoundColors(line: []const u8, index: usize) !?Round {
    if (std.mem.indexOfPos(u8, line, index, ";")) |roundEndIndex| {
        // parse colors
        const redValue = if (try parseRoundColor(line, index, roundEndIndex, " red")) |roundColor| roundColor.value else 0;
        const blueValue = if (try parseRoundColor(line, index, roundEndIndex, " blue")) |roundColor| roundColor.value else 0;
        const greenValue = if (try parseRoundColor(line, index, roundEndIndex, " green")) |roundColor| roundColor.value else 0;
        return Round{ .red = redValue, .blue = blueValue, .green = greenValue, .cursor = roundEndIndex };
    }
    return null;
}

fn parseGame(allocator: std.mem.Allocator, line: []const u8) !Game {
    var index: usize = 0;

    // parse game id
    const gameIdStart = std.mem.indexOf(u8, line, "Game ").? + 5;
    const gameIdEnd = std.mem.indexOf(u8, line, ":").?;
    const gameId = try std.fmt.parseInt(u8, line[gameIdStart..gameIdEnd], 10);
    //std.debug.print("gameId: {d}\n", .{gameIdSlice});

    // move cursor along
    index = gameIdEnd + 1;

    // parsing the rounds
    var redMax: u32 = 0;
    var blueMax: u32 = 0;
    var greenMax: u32 = 0;

    // we don't need this, technically ...
    var rounds = std.ArrayList(Round).init(allocator);
    defer rounds.deinit();

    while (index < line.len) {
        const roundEndIndex = std.mem.indexOfPos(u8, line, index, ";") orelse (line.len - 1);

        // parse colors
        const redValue = if (try parseRoundColor(line, index, roundEndIndex, " red")) |roundColor| roundColor.value else 0;
        const blueValue = if (try parseRoundColor(line, index, roundEndIndex, " blue")) |roundColor| roundColor.value else 0;
        const greenValue = if (try parseRoundColor(line, index, roundEndIndex, " green")) |roundColor| roundColor.value else 0;

        if (redValue > redMax) redMax = redValue;
        if (blueValue > blueMax) blueMax = blueValue;
        if (greenValue > greenMax) greenMax = greenValue;
        try rounds.append(Round{ .red = redValue, .blue = blueValue, .green = greenValue, .cursor = roundEndIndex });
        index = roundEndIndex + 1;
    }

    return Game{
        .id = gameId,
        .redMax = redMax,
        .blueMax = blueMax,
        .greenMax = greenMax,
    };
}

fn problem1(inputFile: []const u8, maxGameConfiguration: Game) !void {
    // damn io
    var file = try std.fs.cwd().openFile(inputFile, .{});
    defer file.close();
    var buf_reader = std.io.bufferedReader(file.reader());
    var in_stream = buf_reader.reader();

    // Create allocator
    var arena = std.heap.ArenaAllocator.init(std.heap.page_allocator);
    defer arena.deinit();
    const allocator = arena.allocator();

    var games = std.ArrayList(Game).init(allocator);
    defer games.deinit();

    var ret: u64 = 0;

    var buf: [4096]u8 = undefined;
    while (try in_stream.readUntilDelimiterOrEof(&buf, '\n')) |line| {
        // parse game
        const game = try parseGame(allocator, line);
        try games.append(game);
        // std.debug.print("parsed game: {any}\n", .{game});

        // answering the problem
        if ((game.redMax <= maxGameConfiguration.redMax) and
            (game.greenMax <= maxGameConfiguration.greenMax) and
            (game.blueMax <= maxGameConfiguration.blueMax))
        {
            // std.debug.print("good game: {any}\n", .{game.id});
            ret += game.id;
        }
    }
    std.debug.print("Result {any}\n", .{ret});
}

fn problem2(inputFile: []const u8) !void {
    // damn io
    var file = try std.fs.cwd().openFile(inputFile, .{});
    defer file.close();
    var buf_reader = std.io.bufferedReader(file.reader());
    var in_stream = buf_reader.reader();

    // Create allocator
    var arena = std.heap.ArenaAllocator.init(std.heap.page_allocator);
    defer arena.deinit();
    const allocator = arena.allocator();

    var games = std.ArrayList(Game).init(allocator);
    defer games.deinit();

    var ret: u64 = 0;

    var buf: [4096]u8 = undefined;
    while (try in_stream.readUntilDelimiterOrEof(&buf, '\n')) |line| {
        // parse game
        const game = try parseGame(allocator, line);
        try games.append(game);
        // std.debug.print("parsed game: {any}\n", .{game});

        // answering the problem
        const gamePower = game.redMax * game.blueMax * game.greenMax;
        ret += gamePower;
    }
    std.debug.print("Result {any}\n", .{ret});
}

test "strings" {
    const testStr = "Hello World World";
    try std.testing.expectEqual(std.mem.indexOf(u8, testStr, "Hello").?, 0);
    try std.testing.expectEqual(std.mem.indexOf(u8, testStr, "World").?, 6);
    try std.testing.expectEqual(std.mem.indexOfPos(u8, testStr, 10, "World").?, 12);
}

test "string int parsing" {
    const testStr = "100";
    const ret = try std.fmt.parseInt(u8, testStr, 10);
    try std.testing.expectEqual(ret, 0);
}

test "isNumber" {
    try std.testing.expectEqual(isNumber(' '), false);
}
