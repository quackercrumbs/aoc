{:ok, body} = File.read("/home/calvinq/projects/aoc/2022/day1/input.txt")

lines = String.split(body, "\n")
result = Stream.chunk_by(lines, &(&1 === ""))
  |> Stream.filter(&(&1 != [""]))
  |> Stream.map(fn string_list ->
    Stream.map(string_list, &(String.to_integer(&1)))
      |> Enum.to_list()
      |> List.foldl(0, fn current_element, running_sum -> current_element + running_sum end)
    end)
  |> Enum.sort(&(&1 >= &2))
  |> Enum.take(3) # change this into 1 (to get the elf with the largest total calories)
  |> List.foldl(0, fn current_element, running_sum -> current_element + running_sum end)

IO.puts("---")
IO.puts(result)

