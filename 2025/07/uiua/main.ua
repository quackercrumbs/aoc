Parse ← (
  ⊜∘ ⊸≠ @\n
)

# Len, index, matrix
Simulate ← |3.3 (
  # Start row 1
  # Look at previous row
  # - IF S in previous, draw | in current row IF not a splitter
  # - IF | in previous, draw | in current row IF not a splitter
  #
  # Look at current row
  # - IF splitter, draw | in current row to left and right IF they're not splitters

  # Find all splitters
  # For each splitter, look at prev row and generate curr row with new beams

  ⊙⟜⊸(
    # Find the beam in previous row and Find the splitter
    /+⊟₃⊃(=@S⊏ -1|=@|⊏ -1|×2=@^⊏|⊏) # Multiply by 2 to distinguish that it is a splitter

    ×⊸≠ 2 # Keep beams the move down and splitters that need splittin
    ⊃(    # Splitting the beam
      ⊃(¤ ⧻
      | ⊚⌕3
        ◴⊂≡⊃(+1|-1)
        °⊚
      )
      ⬚0 ↯
    | # Beams
      = 1
    )
    # Indexes of beams to set
    ⊚≥1 +

    # Uiuaism
    # Set values at indices within a target matrix.
    # It takes three arguments:
    # 1) an array of points for the indices to replace;
    # 2) the original matrix;
    # 3) the value to set the given indices to.
    ⊃(∘|⋅∘|@|) # Adds the replacement value
    ⊙◌∧(⊸⍜⊡◌⊙°□)⊙□
  )
  ⊙⟜⌝⊸⊡
  ⊙(+1)
  ⊃(=|⊙∘)
  ⨬(Simulate|∘)

  # TODO: recurse
)

Init ← (
  ⊃(⧻|1 ∘)
)

# Simulate Init ["..|.." "..^.."]
# ⊸Simulate Init ["..|.." "..^.^"]
# ⊸Simulate Init ["..|.|" "..^.."]
# ⊸Simulate Init ["..||." "..^.."]
# Simulate Init ["..|.." "..^^."] # Not present in input puzzle
# Simulate Init [".|^|." "....."]

CountSplits ← (
  ⊢/+/+ ⧈(/× = [¤ @| ¤ @^]) 2_1
)
# CountSplits ?⋅⋅∘ Simulate Init Parse &fras "../sample.txt" ## 21

# Make sure to set this in your env
# set UIUA_RECURSION_LIMIT 1000
CountSplits ?⋅⋅∘ Simulate Init Parse &fras "../input.txt" ## 1585 
