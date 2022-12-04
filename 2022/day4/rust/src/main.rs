use std::collections::HashSet;
use std::fs;
use std::io::{self, BufRead};
use std::ops;

fn main() -> io::Result<()> {
    // read file
    let file = fs::File::open("/home/calvinq/projects/aoc/2022/day4/input.txt")?;
    let buff_reader = io::BufReader::new(file);
    let lines = buff_reader
        .lines()
        .map(|line| line.unwrap())
        .collect::<Vec<_>>();

    let result = problem_1(lines.clone());
    println!("problem 1: {:?}", result);

    Ok(())
}

fn range_fully_contains(
    range_1: &ops::RangeInclusive<u64>,
    range_2: &ops::RangeInclusive<u64>,
) -> bool {
    return (range_1.contains(&range_2.start()) && range_1.contains(&range_2.end()))
        || (range_2.contains(&range_1.start()) && range_2.contains(&range_1.end()));
}

fn problem_1(lines: Vec<String>) -> u64 {
    let answer = lines
        .into_iter()
        .map(|team_string| {
            let team_ranges = team_string
                .split(",")
                .map(|range| {
                    let range = range
                        .split("-")
                        .map(|n| n.parse::<u64>().unwrap())
                        .collect::<Vec<u64>>();
                    let low = range.get(0).unwrap().to_owned();
                    let high = range.get(1).unwrap().to_owned();
                    ops::RangeInclusive::new(low, high)
                })
                .collect::<Vec<ops::RangeInclusive<u64>>>();

            let range_1 = team_ranges.get(0).unwrap();
            let range_2 = team_ranges.get(1).unwrap();
            range_fully_contains(range_1, range_2)
        })
        .filter(|result| result == &true)
        .count();

    return answer as u64;
}

#[cfg(test)]
mod tests {

    use super::*;

    #[test]
    fn test_sample() {
        let input: Vec<String> = vec![
            String::from("2-4,6-8"),
            String::from("2-3,4-5"),
            String::from("5-7,7-9"),
            String::from("2-8,3-7"),
            String::from("6-6,4-6"),
            String::from("2-6,4-8"),
        ]
        .into();
        assert_eq!(2, problem_1(input));
    }
}
