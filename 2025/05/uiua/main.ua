# ⊜□⊸≠@\n &fras "../sample.txt"
SplitInput ← (
  ˜⊂0⊸=@\n
  ⧈∘2           # Windows of 2
  ≡/(×)         # Find consective new lines
  ˜⊟⊸+1⊢⊚       # Find index of the consective new lines
  ¬⬚0˜↯⊸⊙(¤⧻)°⊚ # Put those indexes back as a mask
  ⊜□            # Partition by those new lines
  # unbox the results
  ⊸(°□⊢)
  ⊙(°□⊣)
)
ParseRanges ← (
  ⊜(⊜⋕ ⊸≠@-)⊸≠@\n
)

ParseNumbers ← (
  ⊜⋕ ⊸≠@\n
)

SolveA ← (
  # ⊃(≡(>0/+)?⊞≤?⊙(⊢⍉)|≡(>0/+)?⊞≥?⊙(⊣ ⍉))
  ⊃(⊞≤⊙(⊢⍉)|⊞≥⊙(⊣ ⍉))
  /+⍉×
  /+≥1
)

Parse ← (
  ParseRanges SplitInput
  ⊙ParseNumbers
  ⊃(⋅∘|∘)
)

SolveA Parse &fras "../sample.txt" ## 3 
SolveA Parse &fras "../input.txt"  ## 896 
