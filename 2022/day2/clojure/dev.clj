(ns dev
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn inspect
  [x]
  (do (println x)
      x))

(def mapping
  {"A" :rock
   "X" :rock
   "B" :paper
   "Y" :paper
   "C" :scissor
   "Z" :scissor})

(def hand-shape-score
  {:rock 1
   :paper 2
   :scissor 3})

(defn round-score
  [you me]
  (let [round [you me]]
    (condp = round
      [you you] 3 ;; you == me (draw)
      [:rock :paper] 6
      [:paper :scissor] 6
      [:scissor :rock] 6
      [:rock :scissor] 0
      [:paper :rock] 0
      [:scissor :paper] 0)))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day2/input.txt")]
                (doall (line-seq file)))]
    (->> lines
         ;; parse each round to ["A" "X"]
         (mapv #(str/split % #" "))
         ;; serialize each round
         (mapv (fn [[you me]] (vector (get mapping you) (get mapping me))))
         ;; calculate score for round
         (mapv (fn [[you me]] (+ (hand-shape-score me) (round-score you me))))
         ;; calculate total score
         (apply +))))
(problem-1) ; 11767

