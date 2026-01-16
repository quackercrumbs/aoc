ParseInput ← (
  ⊜∘⊸≠@\n
  ≡(
    /⊂⍉˜⊟ ˜↯ @ ⊸⧻ # ["0 1 2 3 " "4 5 6 7 "]
    ⊜⋕⊸≠@         # [0_1_2_3 4_5_6_7]
  )
)

ParseInput &fras "../sample.txt"
