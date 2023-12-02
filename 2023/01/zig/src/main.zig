const std = @import("std");

fn isNumber(byte: u8) bool {
    return (byte >= 48) and (byte <= 57);
}

fn makeNumber(digits: [2]u8, index: u8) u8 {
    if (index >= 2) {
        var ret = digits[1] + digits[0] * 10;
        return ret;
    } else {
        return digits[0] * 10 + digits[0];
    }
}

fn problem1() !void {
    var file = try std.fs.cwd().openFile("../input.txt", .{});
    defer file.close();

    var buf_reader = std.io.bufferedReader(file.reader());
    var in_stream = buf_reader.reader();

    var arena = std.heap.ArenaAllocator.init(std.heap.page_allocator);
    defer arena.deinit();
    const allocator = arena.allocator();

    var numbers = std.ArrayList(u8).init(allocator);
    defer numbers.deinit();

    var index: u8 = 0;
    var digits = [2]u8{ 0, 0 };
    var buf: [1024]u8 = undefined;
    while (try in_stream.readUntilDelimiterOrEof(&buf, '\n')) |line| {
        for (line) |character| {
            if (isNumber(character)) {
                // determine which digit to write to
                const i: u8 = if (index > 0) 1 else 0;

                // ascii math
                digits[i] = character - 48;

                // 2 signals that digit[1] was written
                if (index < 2) {
                    index += 1;
                }
            }
        }
        var number = makeNumber(digits, index);
        try numbers.append(number);

        // reset index
        index = 0;
    }

    // sum all the numbers together
    var ret: u64 = 0;
    for (numbers.items) |number| {
        ret = ret + number;
    }
    std.debug.print("{d}\n", .{ret});
}

fn problem2() !void {
    var file = try std.fs.cwd().openFile("../input.txt", .{});
    defer file.close();

    var buf_reader = std.io.bufferedReader(file.reader());
    var in_stream = buf_reader.reader();

    var arena = std.heap.ArenaAllocator.init(std.heap.page_allocator);
    defer arena.deinit();
    const allocator = arena.allocator();

    var numbers = std.ArrayList(u8).init(allocator);
    defer numbers.deinit();

    var spelledDigits = std.StringHashMap(u8).init(allocator);
    defer spelledDigits.deinit();
    try spelledDigits.put("one", 1);
    try spelledDigits.put("two", 2);
    try spelledDigits.put("three", 3);
    try spelledDigits.put("four", 4);
    try spelledDigits.put("five", 5);
    try spelledDigits.put("six", 6);
    try spelledDigits.put("seven", 7);
    try spelledDigits.put("eight", 8);
    try spelledDigits.put("nine", 9);

    var index: u8 = 0;
    var digits = [2]u8{ 0, 0 };
    var buf: [1024]u8 = undefined;
    while (try in_stream.readUntilDelimiterOrEof(&buf, '\n')) |line| {
        for (line, 0..) |character, lineIndex| {
            if (isNumber(character)) {
                // determine which digit to write to
                const i: u8 = if (index > 0) 1 else 0;

                // ascii math
                digits[i] = character - 48;

                // 2 signals that digit[1] was written
                if (index < 2) {
                    index += 1;
                }
            } else {
                const lineLen = line.len;
                var iterator = spelledDigits.keyIterator();
                while (iterator.next()) |digitWordIt| {
                    const digitWord = digitWordIt.*;
                    const digitWordLen = digitWord.len;
                    if ((lineIndex + digitWordLen) <= lineLen) {
                        // we can create the substring
                        const maybeDigitWord = line[lineIndex .. lineIndex + digitWordLen];
                        //std.debug.print("{s} {s}\n", .{ maybeDigitWord, digitWord });
                        if (std.mem.eql(u8, maybeDigitWord, digitWord)) {
                            //std.debug.print("{s} {s} {any}\n", .{ maybeDigitWord, digitWord, spelledDigits.get(digitWord) });
                            const i: u8 = if (index > 0) 1 else 0;
                            digits[i] = spelledDigits.get(digitWord).?;
                            // 2 signals that digit[1] was written
                            if (index < 2) {
                                index += 1;
                            }
                            break;
                        }
                    }
                }
            }
        }
        var number = makeNumber(digits, index);
        try numbers.append(number);

        // reset index
        index = 0;
    }

    // sum all the numbers together
    var ret: u64 = 0;
    for (numbers.items) |number| {
        ret = ret + number;
    }
    std.debug.print("{d}\n", .{ret});
}

pub fn main() !void {
    // Prints to stderr (it's a shortcut based on `std.io.getStdErr()`)
    try problem1();
    try problem2();
}

test "isNumber" {
    try std.testing.expectEqual(isNumber('a'), false);
    try std.testing.expectEqual(isNumber('z'), false);
    try std.testing.expectEqual(isNumber('A'), false);
    try std.testing.expectEqual(isNumber('Z'), false);
    try std.testing.expectEqual(isNumber('0'), true);
    try std.testing.expectEqual(isNumber('1'), true);
    try std.testing.expectEqual(isNumber('2'), true);
    try std.testing.expectEqual(isNumber('3'), true);
    try std.testing.expectEqual(isNumber('4'), true);
    try std.testing.expectEqual(isNumber('5'), true);
    try std.testing.expectEqual(isNumber('6'), true);
    try std.testing.expectEqual(isNumber('7'), true);
    try std.testing.expectEqual(isNumber('9'), true);
}

test "maps" {
    const allocator = std.testing.allocator;
    var maps = std.StringHashMap(u8).init(allocator);
    defer maps.deinit();
    try maps.put("test1", 1);
    try maps.put("test2", 1);
    var iterator = maps.keyIterator();
    while (iterator.next()) |k| {
        const v = maps.get(k.*);
        std.debug.print("{s} {any}\n", .{ k, v });
    }
}
