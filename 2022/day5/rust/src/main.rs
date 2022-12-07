use std::collections::HashMap;
use std::fs;
use std::io::{self, BufRead};

fn main() -> io::Result<()> {
    // read file
    let file = fs::File::open("/home/calvinq/projects/aoc/2022/day5/input.txt")?;
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

fn parse_state_line(s: &str) -> Vec<Option<char>> {
    s.chars()
        .collect::<Vec<char>>()
        .chunks(4)
        .map(|letter_string| {
            let char = letter_string.get(1).unwrap().to_owned();
            if char.is_whitespace() {
                None
            } else {
                Some(char)
            }
        })
        .collect::<Vec<Option<char>>>()
}

fn parse_state_lines(lines: &[String]) -> HashMap<u64, Vec<char>> {
    // NOTE! assumes that the last line of lines doesn't contain the indexs! (i.e 1 2 3 4 5 ...)
    // from the input
    let mut state: HashMap<u64, Vec<char>> = HashMap::new();
    lines
        .iter()
        .map(|line| parse_state_line(line))
        // add each row/line to the state
        .for_each(|row| {
            // insert letters in the row into state
            row.into_iter()
                .enumerate()
                .for_each(|(index, maybe_letter)| {
                    let index = (index + 1) as u64;
                    if !state.contains_key(&index) {
                        state.insert(index, Vec::new());
                    }

                    let letters = state.get_mut(&index);
                    if let Some(letters) = letters {
                        if let Some(letter) = maybe_letter {
                            letters.push(letter);
                        }
                    }
                });
        });
    state
}

#[cfg(test)]
mod parsing_tests {

    use super::*;

    #[test]
    fn test_parse_state_line() {
        let s = String::from("    [D]    ");
        let ret = parse_state_line(&s);
        assert_eq!(vec![None, Some('D'), None], ret);
    }

    #[test]
    fn test_parse_state_lines() {
        let input: Vec<String> = vec![
            String::from("    [D]    "),
            String::from("[N] [C]    "),
            String::from("[Z] [M] [P]"),
        ];

        let ret = parse_state_lines(&input);

        assert_eq!(
            HashMap::from([
                (1, vec!['N', 'Z']),
                (2, vec!['D', 'C', 'M']),
                (3, vec!['P'])
            ]),
            ret
        )
    }

    #[test]
    fn test_parse_actions() {
        let input: Vec<String> = vec![
            String::from("move 1 from 2 to 1"),
            String::from("move 3 from 1 to 3"),
            String::from("move 2 from 2 to 1"),
            String::from("move 1 from 1 to 2"),
        ];

        let ret = parse_actions(&input);

        assert_eq!(
            vec![
                Action::new(1, 2, 1),
                Action::new(3, 1, 3),
                Action::new(2, 2, 1),
                Action::new(1, 1, 2)
            ],
            ret
        )
    }
}

#[derive(Debug, PartialEq)]
struct Action {
    amount: u64,
    src: u64,
    dest: u64,
}
impl Action {
    pub fn new(amount: u64, src: u64, dest: u64) -> Action {
        Action { amount, src, dest }
    }
}
fn parse_actions(lines: &[String]) -> Vec<Action> {
    lines
        .iter()
        .map(|line| {
            let parts = line.split(" ").collect::<Vec<&str>>();
            Action {
                amount: parts.get(1).unwrap().parse::<u64>().unwrap(),
                src: parts.get(3).unwrap().parse::<u64>().unwrap(),
                dest: parts.get(5).unwrap().parse::<u64>().unwrap(),
            }
        })
        .collect::<Vec<Action>>()
}

#[cfg(test)]
mod test_parse_input {

    use super::*;

    #[test]
    fn test_parse_input() {
        let input: Vec<String> = vec![
            String::from("    [D]    "),
            String::from("[N] [C]    "),
            String::from("[Z] [M] [P]"),
            String::from(" 1   2   3 "),
            String::from(""),
            String::from("move 1 from 2 to 1"),
            String::from("move 3 from 1 to 3"),
            String::from("move 2 from 2 to 1"),
            String::from("move 1 from 1 to 2"),
        ];
        let (state, actions) = parse_input(input);
        println!("{:?}", state);
        println!("{:?}", actions);

        assert_eq!(
            HashMap::from([
                (1, vec!['N', 'Z']),
                (2, vec!['D', 'C', 'M']),
                (3, vec!['P'])
            ]),
            state
        );

        assert_eq!(
            vec![
                Action::new(1, 2, 1),
                Action::new(3, 1, 3),
                Action::new(2, 2, 1),
                Action::new(1, 1, 2)
            ],
            actions
        )
    }
}

fn parse_input(lines: Vec<String>) -> (HashMap<u64, Vec<char>>, Vec<Action>) {
    let lines_split = lines
        .split(|line| line == "")
        .map(|s| s.to_vec())
        .collect::<Vec<Vec<String>>>();
    let raw_state_lines = lines_split.get(0).unwrap();
    let state = parse_state_lines(&raw_state_lines[0..raw_state_lines.len() - 1]);
    let raw_action_lines = lines_split.get(1).unwrap();
    let actions = parse_actions(&raw_action_lines);

    (state, actions)
}

fn problem_1(lines: Vec<String>) -> String {
    let (mut state, actions) = parse_input(lines);

    actions.iter().for_each(|action| {
        //println!("{:?}", state);
        //println!("{:?}", action);
        //println!("------");
        let src_stack = state.get_mut(&action.src).unwrap();
        let mut elements_to_move: Vec<char> =
            src_stack.drain(0..action.amount as usize).rev().collect();

        let dest_stack = state.remove(&action.dest).unwrap();
        elements_to_move.extend(dest_stack); // new dest_stack

        state.insert(action.dest, elements_to_move);
    });

    // println!("{:?}", state);
    let num_stacks = state.len() as u64 + 1;
    let result = (1..num_stacks)
        .into_iter()
        .map(|i| {
            let stack = state.get(&i).unwrap();
            let c = stack.get(0).unwrap();
            c.to_owned()
        })
        .collect::<String>();
    result
}

#[cfg(test)]
mod tests_sample_p1 {

    use super::*;

    #[test]
    fn test_sample() {
        let input: Vec<String> = vec![
            String::from("    [D]    "),
            String::from("[N] [C]    "),
            String::from("[Z] [M] [P]"),
            String::from(" 1   2   3 "),
            String::from(""),
            String::from("move 1 from 2 to 1"),
            String::from("move 3 from 1 to 3"),
            String::from("move 2 from 2 to 1"),
            String::from("move 1 from 1 to 2"),
        ]
        .into();
        assert_eq!("CMZ", problem_1(input));
    }
}

fn problem_2(lines: Vec<String>) -> String {
    let (mut state, actions) = parse_input(lines);

    actions.iter().for_each(|action| {
        let src_stack = state.get_mut(&action.src).unwrap();
        let mut elements_to_move: Vec<char> =
            // difference compared to p1, is that we don't do .rev()
            src_stack.drain(0..action.amount as usize).collect();

        let dest_stack = state.remove(&action.dest).unwrap();
        elements_to_move.extend(dest_stack); // new dest_stack

        state.insert(action.dest, elements_to_move);
    });

    // println!("{:?}", state);
    let num_stacks = state.len() as u64 + 1;
    let result = (1..num_stacks)
        .into_iter()
        .map(|i| {
            let stack = state.get(&i).unwrap();
            let c = stack.get(0).unwrap();
            c.to_owned()
        })
        .collect::<String>();
    result
}

#[cfg(test)]
mod tests_sample_p2 {

    use super::*;

    #[test]
    fn test_sample() {
        let input: Vec<String> = vec![
            String::from("    [D]    "),
            String::from("[N] [C]    "),
            String::from("[Z] [M] [P]"),
            String::from(" 1   2   3 "),
            String::from(""),
            String::from("move 1 from 2 to 1"),
            String::from("move 3 from 1 to 3"),
            String::from("move 2 from 2 to 1"),
            String::from("move 1 from 1 to 2"),
        ]
        .into();
        assert_eq!("MCD", problem_2(input));
    }
}


