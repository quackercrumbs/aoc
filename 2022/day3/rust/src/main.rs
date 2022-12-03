use std::collections::HashSet;
use std::fs;
use std::io::{self, BufRead};

fn main() -> io::Result<()> {
    // read file
    let file = fs::File::open("/home/calvinq/projects/aoc/2022/day3/input.txt")?;
    let buff_reader = io::BufReader::new(file);
    let lines = buff_reader
        .lines()
        .map(|line| line.unwrap())
        .collect::<Vec<_>>();

    let result = problem_1(lines.clone());
    println!("problem 1: {:?}", result);

    Ok(())
}

fn calculate_priority(c: &char) -> u64 {
    if c.is_ascii_uppercase() {
        (*c as u8 - b'A' + 27).into()
    } else {
        (*c as u8 - b'a' + 1).into()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn test_priority_math() {
        let c = 'A';
        assert_eq!(c as u8 - b'A', 0);

        let p = calculate_priority(&'A');
        assert_eq!(p, 27);
        assert_eq!(calculate_priority(&'B'), 28);
        assert_eq!(calculate_priority(&'Z'), 52);

        assert_eq!(calculate_priority(&'a'), 1);
        assert_eq!(calculate_priority(&'b'), 2);
        assert_eq!(calculate_priority(&'z'), 26);
    }
}

fn problem_1(mut lines: Vec<String>) -> u64 {
    let answer = lines.iter_mut().map(|line| {
        // idenify common character
        let line_len = line.len();
        let (left, right) = line.split_at(line_len / 2);
        let left = left.chars().collect::<HashSet<char>>();
        let right = right.chars().collect::<HashSet<char>>();
        let common_chars = right
            .intersection(&left)
            .map(|c| c.clone())
            .collect::<Vec<char>>();
        let common_char = common_chars.get(0).unwrap().clone();

        // calculate priority
        let priority = calculate_priority(&common_char);
        priority
    }).sum::<u64>();

    return answer
}
