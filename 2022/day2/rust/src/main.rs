use std::fs;
use std::io::{self, BufRead};

fn main() -> io::Result<()> {
    // read file
    let file = fs::File::open("/home/calvinq/projects/aoc/2022/day2/input.txt")?;
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

#[derive(Clone, PartialEq, Eq)]
enum Hand {
    Rock,
    Paper,
    Scissor,
}

#[derive(Clone, PartialEq, Eq)]
enum RoundResult {
    Lose,
    Draw,
    Win,
}

fn run_round(you: &Hand, me: &Hand) -> RoundResult {
    if you == me {
        return RoundResult::Draw;
    }
    let result = match (you, me) {
        (Hand::Rock, Hand::Paper) => RoundResult::Win,
        (Hand::Rock, Hand::Scissor) => RoundResult::Lose,
        (Hand::Paper, Hand::Scissor) => RoundResult::Win,
        (Hand::Paper, Hand::Rock) => RoundResult::Lose,
        (Hand::Scissor, Hand::Rock) => RoundResult::Win,
        (Hand::Scissor, Hand::Paper) => RoundResult::Lose,
        _ => panic!(""),
    };
    result
}

fn get_result_score(result: &RoundResult) -> u64 {
    match result {
        RoundResult::Lose => 0,
        RoundResult::Draw => 3,
        RoundResult::Win => 6,
    }
}

fn calculate_round_score(you: &Hand, me: &Hand) -> u64 {
    let result = run_round(&you, &me);
    get_result_score(&result)
}

fn get_hand_score(hand: &Hand) -> u64 {
    match hand {
        Hand::Rock => 1,
        Hand::Paper => 2,
        Hand::Scissor => 3,
    }
}

fn problem_1(lines: Vec<String>) -> u64 {
    let total_points: u64 = lines
        .iter()
        .map(|round_string| {
            // parse the round string
            let round = round_string
                .split(" ")
                .map(|letter| match letter {
                    "A" | "X" => Hand::Rock,
                    "B" | "Y" => Hand::Paper,
                    "C" | "Z" => Hand::Scissor,
                    _ => panic!(""),
                })
                .collect::<Vec<_>>();

            let you = round.get(0).unwrap();
            let me = round.get(1).unwrap();

            // determine score
            let round_score = calculate_round_score(&you, &me);
            let hand_score = get_hand_score(me);

            round_score + hand_score
        })
        .sum();
    total_points
}

fn determine_my_hand(you: &Hand, result: &RoundResult) -> Hand {
    if result == &RoundResult::Draw {
        return you.clone()
    }
    match (you, result) {
        (Hand::Rock, RoundResult::Win) => Hand::Paper,
        (Hand::Rock, RoundResult::Lose) => Hand::Scissor,
        (Hand::Paper, RoundResult::Win) => Hand::Scissor,
        (Hand::Paper, RoundResult::Lose) => Hand::Rock,
        (Hand::Scissor, RoundResult::Win) => Hand::Rock,
        (Hand::Scissor, RoundResult::Lose) => Hand::Paper,
        _ => panic!("")
    }
}

fn problem_2(lines: Vec<String>) -> u64 {
    let total_points: u64 = lines
        .iter()
        .map(|round_string| {
            // parse the round string
            let round_raw: Vec<&str> = round_string.split(" ").collect();
            let you = match *round_raw.get(0).unwrap() {
                "A" => Hand::Rock,
                "B" => Hand::Paper,
                "C" => Hand::Scissor,
                _ => panic!(""),
            };
            let round_result = match *round_raw.get(1).unwrap() {
                "X" => RoundResult::Lose,
                "Y" => RoundResult::Draw,
                "Z" => RoundResult::Win,
                _ => panic!(""),
            };

            // determine score
            let round_score = get_result_score(&round_result);
            let me = determine_my_hand(&you, &round_result);
            let hand_score = get_hand_score(&me);

            round_score + hand_score
        })
        .sum();
    total_points
}
