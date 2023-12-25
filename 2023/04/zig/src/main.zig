const std = @import("std");

pub fn main() !void {
    // read args for the input file
    var args = std.process.args();
    _ = args.skip();
    const inputFile = args.next().?;

    // Create allocator
    var arena = std.heap.ArenaAllocator.init(std.heap.page_allocator);
    defer arena.deinit();
    const allocator = arena.allocator();

    const result1 = try problem1(allocator, inputFile);
    std.debug.print("problem1={d}\n", .{result1});
}

fn problem1(allocator: std.mem.Allocator, inputFile: []const u8) !u32 {
    // damn io
    var file = try std.fs.cwd().openFile(inputFile, .{});
    defer file.close();
    var buf_reader = std.io.bufferedReader(file.reader());
    var in_stream = buf_reader.reader();

    var total: u32 = 0;
    var buf: [4096]u8 = undefined;
    while (try in_stream.readUntilDelimiterOrEof(&buf, '\n')) |line| {
        std.debug.print("line={s}\n", .{line});
        const startIndex = std.mem.indexOf(u8, &buf, ":").?;
        const barIndex = std.mem.indexOf(u8, &buf, "|").?;

        // collecting the winning numbers
        var winningNumbersSet = std.AutoHashMap(u16, void).init(allocator);
        defer winningNumbersSet.deinit();

        const winningNumbersSlice = std.mem.trim(u8, line[startIndex + 1 .. barIndex], " ");
        var winningNumbersSliceIter = std.mem.splitSequence(u8, winningNumbersSlice, " ");
        while (winningNumbersSliceIter.next()) |numberSlice| {
            if (numberSlice.len != 0) {
                const number = try std.fmt.parseInt(u16, std.mem.trim(u8, numberSlice, " "), 10);
                try winningNumbersSet.put(number, {});
            }
        }

        var numberMatches: u8 = 0;
        const myNumbersSlice = std.mem.trim(u8, line[barIndex + 1 ..], " ");
        var myNumbersSliceIter = std.mem.splitSequence(u8, myNumbersSlice, " ");
        while (myNumbersSliceIter.next()) |numberSlice| {
            if (numberSlice.len != 0) {
                const number = try std.fmt.parseInt(u16, std.mem.trim(u8, numberSlice, " "), 10);
                if (winningNumbersSet.contains(number)) {
                    numberMatches += 1;
                }
            }
        }
        total += if (numberMatches == 0) 0 else std.math.pow(u16, 2, (numberMatches - 1));
    }
    return total;
}

fn problem2(allocator: std.mem.Allocator, inputFile: []const u8) !u32 {
    // damn io
    var file = try std.fs.cwd().openFile(inputFile, .{});
    defer file.close();
    var buf_reader = std.io.bufferedReader(file.reader());
    var in_stream = buf_reader.reader();

    var total: u32 = 0;
    var buf: [4096]u8 = undefined;
    while (try in_stream.readUntilDelimiterOrEof(&buf, '\n')) |line| {
        std.debug.print("line={s}\n", .{line});
        const startIndex = std.mem.indexOf(u8, &buf, ":").?;
        const barIndex = std.mem.indexOf(u8, &buf, "|").?;

        // collecting the winning numbers
        var winningNumbersSet = std.AutoHashMap(u16, void).init(allocator);
        defer winningNumbersSet.deinit();

        const winningNumbersSlice = std.mem.trim(u8, line[startIndex + 1 .. barIndex], " ");
        var winningNumbersSliceIter = std.mem.splitSequence(u8, winningNumbersSlice, " ");
        while (winningNumbersSliceIter.next()) |numberSlice| {
            if (numberSlice.len != 0) {
                const number = try std.fmt.parseInt(u16, std.mem.trim(u8, numberSlice, " "), 10);
                try winningNumbersSet.put(number, {});
            }
        }

        var numberMatches: u8 = 0;
        const myNumbersSlice = std.mem.trim(u8, line[barIndex + 1 ..], " ");
        var myNumbersSliceIter = std.mem.splitSequence(u8, myNumbersSlice, " ");
        while (myNumbersSliceIter.next()) |numberSlice| {
            if (numberSlice.len != 0) {
                const number = try std.fmt.parseInt(u16, std.mem.trim(u8, numberSlice, " "), 10);
                if (winningNumbersSet.contains(number)) {
                    numberMatches += 1;
                }
            }
        }
        total += if (numberMatches == 0) 0 else std.math.pow(u16, 2, (numberMatches - 1));
    }

    return total;
}

test "problem1 - sample" {
    // Create allocator
    const allocator = std.testing.allocator;

    const result = try problem1(allocator, "../sample.txt");
    std.debug.print("problem1={any}\n", .{result});

    try std.testing.expectEqual(result, 13);
}

test "bit shifting - result" {
    const expected: u8 = 8;
    try std.testing.expectEqual(expected, 1 << 3);
}
