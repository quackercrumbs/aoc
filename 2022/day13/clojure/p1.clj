(ns p1
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [clojure.test :as test :refer [deftest is]])
  (:import [java.lang Character
            System]))

(defn get-input
  [file]
  (->>
   (with-open [file (io/reader file)]
     (doall (line-seq file)))
   (into [])))

(defn get-sample-input
  []
  (get-input "/home/calvinq/projects/aoc/2022/day13/sample.txt"))

(defn get-problem-input
  []
  (get-input "/home/calvinq/projects/aoc/2022/day13/input.txt"))

(defn parse-input
  [lines]
  (->> lines
       (partition-by #(= % ""))
       (filter #(not= [""] %))
       (mapv (fn [pair] (mapv edn/read-string pair)))))
(comment
  (parse-input ["[1, [1], 3, 1, 1]"
                "[1, 1, [3, 2, [1]], 1, 1]"
                ""
                "1"
                "2"]))

(defn convert
  [is-coll? item]
  (if (and is-coll? (not (coll? item)))
    (vector item)
    item))
(defn is-in-order
  [pair]
  (let [left (first pair)
        right (second pair)]
    (loop [[l-curr & l-rest] left
           [r-curr & r-rest] right]
      (cond
        ;; we reached the end of the list, can't determine if they are in order.
        ;;  This is needed for testing nested collections (i think?)
        (and (nil? l-curr) (nil? r-curr))
        :eq

        ;; finished left side early
        (nil? l-curr)
        :yes

        ;; There are still elements in the left that need processing
        (nil? r-curr)
        :no

        :else
        (let [is-coll? (or (coll? l-curr) (coll? r-curr))
              l-curr (convert is-coll? l-curr)
              r-curr (convert is-coll? r-curr)]
          (if is-coll?
            (let [result (is-in-order (vector l-curr r-curr))]
              (if (= :eq result)
                (recur l-rest r-rest)
                ;; testing the collection version of the element pair either failed or passed
                ;;   the in order test
                result))
            (cond
              (< l-curr r-curr)
              :yes
              (> l-curr r-curr)
              :no

              :else
              (recur l-rest r-rest))))))))

(comment
  (time (->> (get-problem-input)
             parse-input
             (mapv is-in-order)
             ;; get indexes
             (map-indexed (fn [i result]
                            ;; 1 indexed, so have to increment
                            (vector (inc i) result)))
             ;; we only care about in order packets
             (filter (fn [[_index result]] (= :yes result)))
             ;; only want the index
             (mapv first)
             (apply +)))) ; 4643
; 4.4ms
