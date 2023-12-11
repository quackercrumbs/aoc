const std = @import("std");

pub fn main() !void {
    // Prints to stderr (it's a shortcut based on `std.io.getStdErr()`)
    std.debug.print("All your {s} are belong to us.\n", .{"codebase"});

    var args = std.process.args();
    _ = args.skip();
    const inputFile = args.next().?;

    // Create allocator
    var arena = std.heap.ArenaAllocator.init(std.heap.page_allocator);
    defer arena.deinit();
    const allocator = arena.allocator();

    const result = try problem1(allocator, inputFile);
    std.debug.print("problem1={any}\n", .{result});
}

fn isDigit(byte: u8) bool {
    return (byte >= '0') and (byte <= '9');
}

const Location = struct {
    lineIndex: usize,
    index: usize,
};

fn extractNumberSlice(slice: []const u8) []const u8 {
    for (slice, 0..) |character, index| {
        if (!isDigit(character)) {
            return slice[0..index];
        }
    }
    return slice;
}

fn problem1(allocator: std.mem.Allocator, inputFile: []const u8) !u16 {
    // damn io
    var file = try std.fs.cwd().openFile(inputFile, .{});
    defer file.close();
    var buf_reader = std.io.bufferedReader(file.reader());
    var in_stream = buf_reader.reader();

    // Similar to how we do island labeling, each island is its own part.
    // The problem doesn't say it explicitly, but maybe there can be
    // duplicate duplicate part numbers.
    var partIdCounter: u16 = 0;
    var locationToPartId = std.AutoHashMap(Location, u16).init(allocator);
    defer locationToPartId.deinit();

    // Each part we find has a number
    var partIdToPartNumber = std.AutoHashMap(u16, u16).init(allocator);
    defer partIdToPartNumber.deinit();

    // used to identify if a part id belongs to the engine.
    var locationToSymbol = std.AutoHashMap(Location, u8).init(allocator);
    defer locationToSymbol.deinit();

    // holds all the data from input
    var linesData = std.ArrayList([]u8).init(allocator);
    defer deinitStringArray(allocator, linesData);

    var lineIndex: usize = 0;
    var lineLength: usize = 0;
    var buf: [4096]u8 = undefined;
    while (try in_stream.readUntilDelimiterOrEof(&buf, '\n')) |line| {
        // We don't need to do this, but curious how we can copy strings!
        try linesData.append(try allocator.dupe(u8, line));
        lineLength = line.len;

        var index: usize = 0;
        while (index < line.len) {
            if (isDigit(line[index])) {
                // find the index of the end of the number
                var endNumberIndex = index + 1;
                while (endNumberIndex < line.len) {
                    if (!isDigit(line[endNumberIndex])) {
                        break;
                    }
                    endNumberIndex += 1;
                }

                const partNumberSlice = line[index..endNumberIndex];
                const partNumber = try std.fmt.parseInt(u16, partNumberSlice, 10);
                try partIdToPartNumber.put(partIdCounter, partNumber);
                for (0..partNumberSlice.len) |sliceOffset| {
                    // This makes it easy to look up neighboring part numbers
                    // near a symbol
                    //std.debug.print("parsed [{d} {d}]{d}\n", .{ lineIndex, index + sliceOffset, partNumber });
                    try locationToPartId.put(Location{ .lineIndex = lineIndex, .index = index + sliceOffset }, partIdCounter);
                }
                partIdCounter += 1;

                index = endNumberIndex;
            } else if (line[index] != '.') {
                // it is a symbol
                //std.debug.print("parsed symbol {c}\n", .{line[index]});
                try locationToSymbol.put(Location{ .lineIndex = lineIndex, .index = index }, line[index]);
                index += 1;
            } else {
                index += 1;
            }
        }
        lineIndex += 1;
    }
    //debugArrayList(linesData);
    //debugLocationToIntMap(locationToPartId);
    //debugIntToIntMap(partIdToPartNumber);
    //debugLocationToCharMap(locationToSymbol);

    const maxLineIndex = lineIndex - 1;
    const maxIndex = lineLength - 1;
    var enginePartIds = std.AutoHashMap(u16, bool).init(allocator);
    defer enginePartIds.deinit();
    // identify all the engine parts
    var symbolIter = locationToSymbol.keyIterator();
    while (symbolIter.next()) |symbolLocation| {
        // gotta find all the neighoring part ids
        const symLineIndex = symbolLocation.lineIndex;
        const index = symbolLocation.index;
        const aboveLineIndex = if (symLineIndex > 0) symLineIndex - 1 else symLineIndex;
        const belowLineIndex = if (symLineIndex < maxLineIndex) symLineIndex + 1 else symLineIndex;
        const leftIndex = if (index > 0) index - 1 else index;
        const rightIndex = if (index < maxIndex) index + 1 else index;

        if (locationToPartId.get(Location{ .lineIndex = aboveLineIndex, .index = leftIndex })) |partId| {
            try enginePartIds.put(partId, true);
        }
        if (locationToPartId.get(Location{ .lineIndex = aboveLineIndex, .index = index })) |partId| {
            try enginePartIds.put(partId, true);
        }
        if (locationToPartId.get(Location{ .lineIndex = aboveLineIndex, .index = rightIndex })) |partId| {
            try enginePartIds.put(partId, true);
        }
        if (locationToPartId.get(Location{ .lineIndex = symLineIndex, .index = leftIndex })) |partId| {
            try enginePartIds.put(partId, true);
        }
        if (locationToPartId.get(Location{ .lineIndex = symLineIndex, .index = rightIndex })) |partId| {
            try enginePartIds.put(partId, true);
        }
        if (locationToPartId.get(Location{ .lineIndex = belowLineIndex, .index = leftIndex })) |partId| {
            try enginePartIds.put(partId, true);
        }
        if (locationToPartId.get(Location{ .lineIndex = belowLineIndex, .index = index })) |partId| {
            try enginePartIds.put(partId, true);
        }
        if (locationToPartId.get(Location{ .lineIndex = belowLineIndex, .index = rightIndex })) |partId| {
            try enginePartIds.put(partId, true);
        }
    }
    //debugIntToBoolMap(enginePartIds);

    // sum all the engine part ids
    var total: u16 = 0;
    var enginePartIdsIter = enginePartIds.keyIterator();
    while (enginePartIdsIter.next()) |partId| {
        const partNumber = partIdToPartNumber.get(partId.*).?;
        total += partNumber;
    }
    // std.debug.print("Total: {d}\n", .{total});
    return total;
}

fn deinitStringArray(allocator: std.mem.Allocator, a: std.ArrayList([]u8)) void {
    // used to defer deinitialization of nested array lists (only 1 level)
    for (a.items) |str| {
        allocator.free(str);
    }
    a.deinit();
}

fn deinitNestedArray(a: std.ArrayList(std.ArrayList(u8))) void {
    // used to defer deinitialization of nested array lists (only 1 level)
    for (a.items) |inner_a| {
        inner_a.deinit();
    }
    a.deinit();
}

fn debugLocationToIntMap(m: std.AutoHashMap(Location, u16)) void {
    std.debug.print("LocationToInt-\n", .{});
    var it = m.keyIterator();
    while (it.next()) |location| {
        std.debug.print("{any}={any}\n", .{ location, m.get(location.*) });
    }
}

fn debugIntToIntMap(m: std.AutoHashMap(u16, u16)) void {
    std.debug.print("IntToInt-\n", .{});
    var it = m.keyIterator();
    while (it.next()) |n| {
        std.debug.print("{any}={any}\n", .{ n.*, m.get(n.*) });
    }
}

fn debugIntToBoolMap(m: std.AutoHashMap(u16, bool)) void {
    std.debug.print("IntToBool-\n", .{});
    var it = m.keyIterator();
    while (it.next()) |n| {
        std.debug.print("{any}={any}\n", .{ n.*, m.get(n.*) });
    }
}

fn debugLocationToCharMap(m: std.AutoHashMap(Location, u8)) void {
    std.debug.print("LocationToStr-\n", .{});
    var it = m.keyIterator();
    while (it.next()) |location| {
        std.debug.print("{any}={c}\n", .{ location, m.get(location.*).? });
    }
}

fn debugArrayList(a: std.ArrayList([]u8)) void {
    std.debug.print("ArrayList-\n", .{});
    for (a.items) |item| {
        std.debug.print("{c}\n", .{item});
    }
}

test "isDigit" {
    try std.testing.expect(isDigit('0'));
    try std.testing.expect(isDigit('1'));
    try std.testing.expect(isDigit('2'));
    try std.testing.expect(isDigit('3'));
    try std.testing.expect(isDigit('4'));
    try std.testing.expect(isDigit('5'));
    try std.testing.expect(isDigit('6'));
    try std.testing.expect(isDigit('7'));
    try std.testing.expect(isDigit('8'));
    try std.testing.expect(isDigit('9'));
}

test "split sequence" {
    var iter = std.mem.splitSequence(u8, "1...123....567...1.2.3", ".");

    while (iter.peek()) |numStr| {
        const index = iter.index;
        std.debug.print("\ns={s} len={d} index={any} \n", .{
            numStr,
            numStr.len,
            index,
        });

        // advance iterator
        _ = iter.next();
    }
}

test "extractNumberSlice" {
    try std.testing.expectEqualSlices(u8, extractNumberSlice("10*"), "10");
    try std.testing.expectEqualSlices(u8, extractNumberSlice("10"), "10");
    try std.testing.expectEqualSlices(u8, extractNumberSlice("0"), "0");
    try std.testing.expectEqualSlices(u8, extractNumberSlice("1000-0"), "1000");
}

test "hashMap printing" {
    const allocator = std.testing.allocator;
    var m = std.AutoHashMap(Location, u8).init(allocator);
    defer m.deinit();

    try m.put(Location{ .lineIndex = 0, .index = 1 }, 'a');
    try m.put(Location{ .lineIndex = 0, .index = 2 }, 'b');
    try m.put(Location{ .lineIndex = 0, .index = 3 }, 'c');
    try m.put(Location{ .lineIndex = 0, .index = 4 }, '$');
    debugLocationToCharMap(m);

    std.debug.print("\n\n", .{});
}

test "multi-dimensional array list - deinit" {
    const allocator = std.testing.allocator;
    var a = std.ArrayList(std.ArrayList(u8)).init(allocator);
    // this should clear us from memory leaks
    defer deinitNestedArray(a);

    var inner_a = std.ArrayList(u8).init(allocator);
    try inner_a.append(1);
    try a.append(inner_a);
}

test "string array list - deinit" {
    const allocator = std.testing.allocator;
    var a = std.ArrayList([]u8).init(allocator);
    // this should clear us from memory leaks
    defer deinitStringArray(allocator, a);

    try a.append(try allocator.dupe(u8, "test"));
}

test "copying sentinel-terminated-array" {
    const allocator = std.testing.allocator;
    const s1 = "test";
    const s2 = try std.mem.Allocator.dupe(allocator, u8, s1);

    // we need to clean up to prevent leaks!
    defer allocator.free(s2);

    std.debug.print("s1={s} s2={s}", .{ s1, s2 });
}

test "sample problem1" {
    // Create allocator
    const allocator = std.testing.allocator;

    const result = try problem1(allocator, "../sample.txt");
    std.debug.print("problem1={any}\n", .{result});

    try std.testing.expectEqual(result, 4361);
}

test "parse sample line" {
    const line = "10...20*....*300....*400*....500....600";
    var index: usize = 0;
    while (index < line.len) {
        if (isDigit(line[index])) {
            // find the index of the end of the number
            var endNumberIndex = index + 1;
            while (endNumberIndex < line.len) {
                if (!isDigit(line[endNumberIndex])) {
                    break;
                }
                endNumberIndex += 1;
            }

            const numberSlice = line[index..endNumberIndex];
            const number = try std.fmt.parseInt(u16, numberSlice, 10);
            std.debug.print("parsed {d}\n", .{number});

            index = endNumberIndex;
        } else if (line[index] != '.') {
            // it is a symbol
            std.debug.print("parsed symbol {c}\n", .{line[index]});
            index += 1;
        } else {
            index += 1;
        }
    }
}

test "range" {
    var total: usize = 0;
    for (0..3) |n| {
        total += n;
    }

    try std.testing.expectEqual(total, 3);
}
