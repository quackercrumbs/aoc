use std::collections::HashSet;
use std::fs;
use std::io::{self, BufRead};

fn main() -> io::Result<()> {
    // read file
    let file = fs::File::open("/home/calvinq/projects/aoc/2022/day6/input.txt")?;
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
fn find_signal(line: &str, size: usize) -> usize {
    // https://users.rust-lang.org/t/windows-method-for-str-is-str-a-slice/37512/4
    let chars = line.chars().collect::<Vec<char>>();
    let result = chars
        .windows(size)
        .enumerate()
        .find(|(_start_index, window)| {
            let uniq_chars = window.iter().collect::<HashSet<&char>>();
            uniq_chars.len() == size
        });

    let start_index = result.unwrap().0;
    start_index + size
}

fn problem_1(lines: Vec<String>) -> usize {
    find_signal(lines.get(0).unwrap(), 4)
}

fn problem_2(lines: Vec<String>) -> usize {
    find_signal(lines.get(0).unwrap(), 14)
}

#[cfg(test)]
mod tests_sample {

    use super::*;

    #[test]
    fn test_p1() {
        let s = vec![String::from("mjqjpqmgbljsphdztnvjfqwrcgsmlb")];
        assert_eq!(problem_1(s), 7);

        let s = vec![String::from("bvwbjplbgvbhsrlpgdmjqwftvncz")];
        assert_eq!(problem_1(s), 5);

        let s = vec![String::from("nppdvjthqldpwncqszvftbrmjlhg")];
        assert_eq!(problem_1(s), 6);

        let s = vec![String::from("nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg")];
        assert_eq!(problem_1(s), 10);

        let s = vec![String::from("zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw")];
        assert_eq!(problem_1(s), 11);
    }

    #[test]
    fn test_p2() {
        let s = vec![String::from("mjqjpqmgbljsphdztnvjfqwrcgsmlb")];
        assert_eq!(problem_2(s), 19);

        let s = vec![String::from("bvwbjplbgvbhsrlpgdmjqwftvncz")];
        assert_eq!(problem_2(s), 23);

        let s = vec![String::from("nppdvjthqldpwncqszvftbrmjlhg")];
        assert_eq!(problem_2(s), 23);

        let s = vec![String::from("nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg")];
        assert_eq!(problem_2(s), 29);

        let s = vec![String::from("zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw")];
        assert_eq!(problem_2(s), 26);
    }
}
