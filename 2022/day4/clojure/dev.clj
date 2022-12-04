(ns dev
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.edn :as edn]
            [clojure.string :as str])
  (:import [java.lang Character]))

(defn inspect
  [x]
  (do (println x) x))

(defn range-fully-contained* 
  [[range1-low range1-high] [range2-low range2-high]]
  (and
        ;; range1-low < range2-low < range1-high
   (and (<= range2-low range1-high) (<= range1-low range2-low))
        ;; range2-low < range2-high < range2-high
   (and (<= range2-high range1-high) (<= range1-low range2-low))))

;; brute force ish, maybe we can do something with sorting the mins and maxes of the ranges?
(defn range-fully-contained
  "returns true if one of the ranges fully contains the other"
  [range-1 range-2]
  (or (range-fully-contained* range-1 range-2)
      (range-fully-contained* range-2 range-1)))

(do
  (let [test-str ["2-4,6-8"
                  "2-3,4-5"
                  "5-7,7-9"
                  "2-8,3-7"
                  "6-6,4-6"
                  "2-6,4-8"]]
    (->> test-str
         ;; for each line, create team pairs [[pair-1 pair-1]
         ;;                                   [pair-2 pair-2]]
         (mapv #(str/split % #","))
         ;; each team pair, create ranges
         ;;  team = [pair-a pair-b]
         (mapv (fn [team] (->> team
                               ;; team = [[left-a right-a] [left-b, right-b]] 
                               (mapv #(str/split % #"-"))
                               ;; convert from string to int ranges
                               (mapv (fn [[left-section right-section]]
                                       (vector (edn/read-string left-section) (edn/read-string right-section)))))))
         ;; for each team, identify if one of the elves' ranges fully contain the other
         (reduce (fn [acc team]
                   (let [[elf1-range elf2-range] team]
                     (if (range-fully-contained elf1-range elf2-range)
                       (inc acc)
                       acc)))

                 0))))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day4/input.txt")]
                (doall (line-seq file)))]
    (->> lines
         ;; for each line, create team pairs [[pair-1 pair-1]
         ;;                                   [pair-2 pair-2]]
         (mapv #(str/split % #","))
         ;; each team pair, create ranges
         ;;  team = [pair-a pair-b]
         (mapv (fn [team] (->> team
                               ;; team = [[left-a right-a] [left-b, right-b]] 
                               (mapv #(str/split % #"-"))
                               ;; convert from string to int ranges
                               (mapv (fn [[left-section right-section]]
                                       (vector (edn/read-string left-section) (edn/read-string right-section)))))))
         ;; for each team, identify if they overlap
         (reduce (fn [acc team]
                   (let [[elf1-range elf2-range] team]
                     (if (range-fully-contained elf1-range elf2-range)
                       (inc acc)
                       acc)))

                 0))))
(problem-1) ; 569
