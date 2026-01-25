ParseInput ← (
  ⊜∘ ⊸≠@\n
  =@@
)

PadTopBottom ← (
  ˜↯0⊸(⊏1△) # gen 0s array based on num rows
  ˜⊂⟜⊂      # Pad top and bottom
)
PadZeros ← (
  ⍉PadTopBottom⍉PadTopBottom
)

SolveA ← (
  ⊸PadZeros
  ⧈(
    °⊸(⊡1_1)0 # Set middle to 0
    /+/+      # Number of surrounding rolls
    <4        # Which cells have less than 4 rolls
  ) 3_3
  /+/+× # Count for cell < 4 AND there is a roll present
)

# SolveA [1_2_3 4_5_6 7_8_9]
SolveA ParseInput &fras "../sample.txt" # 13
SolveA ParseInput &fras "../input.txt"  # 1351
