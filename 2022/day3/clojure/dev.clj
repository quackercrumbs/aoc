(ns dev
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str])
  (:import [java.lang Character]))

(defn inspect
  [x]
  (do (println x)
      x))

#_(do
    (clojure.repl/apropos "split")
    (clojure.repl/doc clojure.core/split-at)
    (->> (clojure.core/split-at (quot (count "zz") 2) "zaa")
         (mapv set)
         (apply clojure.set/intersection)
         (mapv (fn [c] (if (Character/isUpperCase c)
                         (+ 27 (- (int c) (int \A)))
                         (inc (- (int c) (int \a)))))))
    (int \a)
    (Character/isUpperCase \A))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day3/input.txt")]
                (doall (line-seq file)))]
    (->> lines
         (mapv (fn [line]
                 (->> (split-at (quot (count line) 2) line)
                      (mapv set)
                      (apply set/intersection)
                      (mapv (fn [c] (if (Character/isUpperCase c)
                                      (+ 27 (- (int c) (int \A)))
                                      (+ 1 (- (int c) (int \a))))))
                      (apply +))))

         (apply +))))
(problem-1)
; 8053

#_(do
  (clojure.repl/apropos "partition")
  (clojure.repl/doc partition)
  (->> ["AA" "Ab" "Ac" "BB" "Bb" "Bc"]
       (partition 3)
       (mapv (fn [group] (->>
                          (mapv set group)
                          (apply set/intersection))))
       (apply concat)
       (mapv (fn [c] (if (Character/isUpperCase c)
                       (+ 27 (- (int c) (int \A)))
                       (+ 1 (- (int c) (int \a))))))
       (apply +)))

(defn problem-2
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day3/input.txt")]
                (doall (line-seq file)))]
    (->> lines
         ;; group of 3 lines
         (partition 3)
         ;; find common element in the three lines
         (mapv (fn [group] (->>
                            (mapv set group)
                            (apply set/intersection))))
         ;; Combine [#{\A} #{\B}] into one list
         (apply concat)
         ;; convert to priority
         (mapv (fn [c] (if (Character/isUpperCase c)
                         (+ 27 (- (int c) (int \A)))
                         (+ 1 (- (int c) (int \a))))))
         ;; find sum
         (apply +))))
(problem-2) ; 2425
