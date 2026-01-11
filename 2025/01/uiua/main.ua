ParseInput ← (
  # Split by new line
  (⊜□⊸≠@\n)
  # More string processing
  # Pushes "LRLR ..." [1 2 3 4 ...]
  ≡⊃(◇⊢)(◇(⋕↘1))
  # Process the direction to -/+ 1
  (≡⍣(¯ 1 °$"L"|1 °$"R"))
  × # returns [+/- spin-number, ...]
)

# Part 1
ProblemA ← (
  ParseInput
  # Scan to compute position of dial after each move
  (\((◿100)+)⊂ 50) # Debug: Add by to Left of X 
  # Count number of positions of 0
  /+(= 0)
)

# ProblemA &fras "../input.txt" # 1118
# ProblemA &fras "../sample.txt" # 3

ExcludeFirst ← °⊸⊢⊸(⨬(-1⊢|+1⊢))⊸/>⊟
NumZeroCrosses ← (
  ExcludeFirst
  ⇡+1⊸/-⍆
  +(⊙⊢)
  ◿100
  /+=0
)

# 0
# NumZeroCrosses 0 1
# 2
# NumZeroCrosses 50 ¯101
# 1
# NumZeroCrosses 50 100
# 2
# NumZeroCrosses 50 200
# 3
# NumZeroCrosses 101 400
# 1
# NumZeroCrosses 5 ¯5

# 1
# NumZeroCrosses 99 101

# 0
# NumZeroCrosses 0 ¯5

# [1 0 0 1 2 0 1 ]
# ⧈(NumZeroCrosses ⊢⟜⊣) 2 [50 100 101 199 200 0 5 ¯10]

# Part 2
ProblemB ← (
  ParseInput
  # Scan to compute position of dial after each move
  (\+⊂ 50)
  ⧈(NumZeroCrosses⊢⟜⊣) 2
  /+
)
# ProblemB &fras "../sample.txt" # 6
# ProblemB &fras "../input.txt" # 6289
