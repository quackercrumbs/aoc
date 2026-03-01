ParseInput ← (
  ⊜□⊸≠ @\n
  ⊃(⊜⊢ ⊸≠@ °□⊣
  | ↘¯1
    ≡(
      ⊸≠@ °□
      ⊜⋕
    )
    ⍉
  )
)

SolveA ← (
  ⨬(/×|/+)⨂"*+"
  /+
)

SolveA ParseInput &fras "../sample.txt" ## 4277556 
SolveA ParseInput &fras "../input.txt"  ## 4449991244405 
