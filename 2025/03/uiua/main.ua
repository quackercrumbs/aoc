ParseInput ← (
  ⊜∘⊸≠@\n
  ≡(
    /⊂⍉˜⊟ ˜↯ @ ⊸⧻ # ["0 1 2 3 " "4 5 6 7 "]
    ⊜⋕⊸≠@         # [0_1_2_3 4_5_6_7]
  )
)

# ParseInput &fras "../sample.txt"

# Pointless ...
MaxTwo ← ≡(
  0⊸˜⨂⊸/↥

  ⊃(⋅∘|∘) # move 0 back 1 position on stack
  ⟜⊸⌝⊸⊡
  ⊃(⋅∘|∘) # move 0 back 1 position on stack
  ˜⨂⊸/↥
)

SolveA ← (
  ≡(
    ⟜⍜⇌(↘¯1\↥)
    # find the max at each position
    # (dropping and then under reverse, is the same as (under reverse, rotate and drop first))
    # This effectively like an offset the max array by 1 (use question mark to debug this if needed or look at previous commit)
    ↘¯1    # Should not look at the end of original array (also to match size of the max scan array)
    /↥+×10 # just dealing with 2 digit numbers
  )
  /+ # sum results each row
)
# SolveA [1_2_3 3_2_1]
# SolveA [8_1_1_9]
# SolveA [2_3_4_2_3_4_7_8]
# SolveA [8_1_8_1_9_1_2_1]

# SolveA ParseInput &fras "../sample.txt" # 357
# SolveA ParseInput &fras "../input.txt"  # 17158
