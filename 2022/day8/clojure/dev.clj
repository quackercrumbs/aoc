(ns dev
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :as test :refer [deftest is]])
  (:import [java.lang Character]))

(defn inspect
  [x]
  (println x)
  x)

(def sample
  ["30373"
   "25512"
   "65332"
   "33549"
   "35390"])

(defn parse-input
  "Creates a 2d vector"
  [input]
  (->> input
       ;; split into individual digit strings
       (mapv #(->> (str/split % #"")
                   ;; maps string to int
                   (mapv edn/read-string)))))

(defn check-row
  [grid row-index col-range e]
  (->>
   (map (fn [col-index]
          (let [row (get grid row-index)
                curr (get row col-index)]
            curr))
        col-range)
   (some #(<= e %))))

(defn check-col
  [grid row-range col-index e]
  (->>
   (map (fn [row-index]
          (let [row (get grid row-index)
                curr (get row col-index)]
            curr))
        row-range)
   (some #(<= e %))))

(defn find-num-visible-trees*
  [grid]
  (->>
   (let [num-cols (count (first grid))
         num-rows (count grid)]
     (for [row-index (range 1 (dec num-rows))
           col-index (range 1 (dec num-cols))]
       (let [row (get grid row-index)
             e (get row col-index)]
         (and
       ;; check right to left
          (check-row grid row-index (reverse (range (inc col-index) num-cols)) e)
       ;; check left to right
          (check-row grid row-index (range 0 col-index) e)
       ;; check down to top
          (check-col grid (reverse (range (inc row-index) num-rows)) col-index e)
       ;; check up to down
          (check-col grid (range 0 row-index) col-index e)))))
   (filter nil?)
   count))

(defn find-num-visible-trees
  [grid]
  (let [num-cols (count (first grid))
        num-rows (count grid)]
    (+ num-rows num-rows num-cols num-cols -4
       (find-num-visible-trees* grid))))

(deftest test-sample-p1
  (is (= 21 (-> (parse-input sample)
      find-num-visible-trees))))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day8/input.txt")]
                (doall (line-seq file)))]
    (->> lines
         parse-input
         find-num-visible-trees)))
(time (problem-1)) ; 1827
; 118 ms


