# Experimental!
ParseInput ← (
  ▽⊸≠@\n        # Remove the new lines
  ⊜□⊸≠@,        # split by ,
  ≡◇(≡◇⋕⊜□⊸≠@-) # split by -, then parse
)

IsInvalidID ← (
  ≡(
    °⋕ # to string
    ⊸⧻ # pull out the length of each string
    ⊙□ # box the string, since len different (f.e "99" "100")
  )
  ⊸≡(¬◿ 2 # Find all even length strings
     ⨬(0|(
        # Process even length strings
        ÷2⊸(⧻°□) # find half way mark
        ⟜⊸⍚↙     # take first half
        ⊃⊙⋅∘⋅∘   # Reorder: a c b (len, full, first_half)
        ⊸⍚↘      # take second half
        ⊙⋅∘      # Drop the full string
        /×/=⊟    # string equality by char
      )
    )
  )
  ▽   # only keep strings that pass the switch statement
  /+⋕ # parse those strings and compute total
)
# ≡◇IsInvalidID {1_2_3 4_5_6_7 10_11_12 1000_1010_1111}

ProblemA ← (
  +1 ⊸≡/-       # length + 1 to include right
  ≡(□ ⇡ ⊢)      # generate ranges 
  ≡(+⊙⊢)        # add left as offset
  ≡◇IsInvalidID # for each row, find sum of all invalid ids
  /+            # sum all results
)
# ProblemA¤11_22                 # 33 (11, 22)
# ProblemA¤95_115                # 99
# ProblemA¤998_1012              # 1010
# ProblemA¤1188511880_1188511890 # 1188511885
# ProblemA¤222220_222224         # 222222
# ProblemA¤1698522_1698528       # []
# ProblemA¤446443_446449         # 446446
# ProblemA¤38593856_38593862     # 38593859
# ProblemA [11_22 11_23]

# ProblemA ParseInput &fras "../sample.txt" # 1227775554
# ProblemA ParseInput &fras "../input.txt" ## 32976912643
