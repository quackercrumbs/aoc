(ns dev
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

;; -- Problem 1
(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day1/input.txt")]
                (doall (line-seq file)))]
    ;; form calorie groups per elf
    (->> (partition-by #(= "" %) lines)
         ;; ignore the paritions
         (filter #(not= [""] %))
         ;; calculate total calories per elf
         (mapv (fn [string-calories-list]
                 (apply + (mapv #(edn/read-string %) string-calories-list))))
         ;; find the max
         (apply max)))) 
(problem-1) ; 72602

(defn problem-2
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day1/input.txt")]
                (doall (line-seq file)))]
    ;; form calorie groups per elf
    (->> (partition-by #(= "" %) lines)
         ;; ignore the paritions
         (filter #(not= [""] %))
         ;; calculate total calories per elf
         (mapv (fn [string-calories-list]
                 (apply + (mapv #(edn/read-string %) string-calories-list))))
         ;; sort in decreasing order
         (sort >)
         ;; take the top 3 and add
         (take 3)
         (apply +))))

(problem-2)  ; 207410

