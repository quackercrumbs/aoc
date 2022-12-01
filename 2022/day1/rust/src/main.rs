use std::fs;
use std::io::{self, BufRead};

fn main() -> io::Result<()> {
    // read file
    let file = fs::File::open("/home/calvinq/projects/aoc/2022/day1/input.txt")?;
    let buff_reader = io::BufReader::new(file);
    let lines = buff_reader
        .lines()
        .map(|line| line.unwrap())
        .collect::<Vec<_>>();

    let result = problem_1(lines.clone());
    println!("problem 1: {:?}", result);

    let result = problem_2(lines.clone());
    println!("problem 2: {:?}", result);
    Ok(())
}

fn problem_1(string_calories_list: Vec<String>) -> u64 {
    let max_calories = string_calories_list
        // define chunks by strings/lines that are just white space
        .split(|string_calories| string_calories.is_empty())
        // ignore empty lists, since its the seperators (also if elves don't have any calories,
        // they'll default to 0 calories
        .filter(|elf_string_calories| !elf_string_calories.is_empty())
        // for each chunk (i.e ["109" "10"]) parse string and calculate the sum
        .map(|elf_string_calories| {
            let total_calories = elf_string_calories
                .iter()
                .map(|string_number| string_number.parse::<u64>().unwrap());
            total_calories.sum()
        })
        // largest sum
        .max();
    return max_calories.unwrap_or(0);
}

fn problem_2(string_calories_list: Vec<String>) -> u64 {
    let mut total_calories_per_elf = string_calories_list
        // define chunks by strings/lines that are just white space
        .split(|string_calories| string_calories.is_empty())
        // ignore empty lists, since its the seperators (also if elves don't have any calories,
        // they'll default to 0 calories
        .filter(|elf_string_calories| !elf_string_calories.is_empty())
        // for each chunk (i.e ["109" "10"]) parse string and calculate the sum
        .map(|elf_string_calories| {
            let total_calories = elf_string_calories
                .iter()
                .map(|string_number| string_number.parse::<u64>().unwrap());
            total_calories.sum()
        })
        .collect::<Vec<u64>>();
    // sort decreasing order
    total_calories_per_elf.sort_by(|a, b| b.partial_cmp(a).unwrap());
    // take the first 3 and find the sum
    return total_calories_per_elf[0..3].iter().sum();
}
