(ns p2
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

(defn packet-comparator
  [left right]
  (loop [[l-curr & l-rest] left
         [r-curr & r-rest] right]
    (cond
        ;; we reached the end of the list, can't determine if they are in order.
        ;;  This is needed for testing nested collections (i think?)
      (and (nil? l-curr) (nil? r-curr))
      0

        ;; finished left side early
      (nil? l-curr)
      -1

        ;; There are still elements in the left that need processing
      (nil? r-curr)
      1

      :else
      (let [is-coll? (or (coll? l-curr) (coll? r-curr))
            l-curr (convert is-coll? l-curr)
            r-curr (convert is-coll? r-curr)]
        (if is-coll?
          (let [result (packet-comparator l-curr r-curr)]
            (if (= 0 result)
              (recur l-rest r-rest)
                ;; testing the collection version of the element pair either failed or passed
                ;;   the in order test
              result))
          (cond
            (< l-curr r-curr)
            -1
            (> l-curr r-curr)
            1

            :else
            (recur l-rest r-rest)))))))

(comment
  (do (println "testing comparators")
      (letfn [(my-compar [x y]
                (if (< x y)
                  -1
                  1))]
        (sort my-compar [1 2 3]))))

(comment
  (time (->>  ;; get input
         (get-problem-input)
         #_(get-sample-input)
             ;; create arrays
         (mapv edn/read-string)
             ;; ignore the blank spaces
         (filter (comp not nil?))
             ;; Add the two new packets for problem 2
         (into [[[2]] [[6]]])
             ;; sort the packets
         (sort packet-comparator)
             ;; add indexes to each packet (packets are 1 indexed)
         (map-indexed (fn [i x] (vector (inc i) x)))
             ;; find the two packets that were inserted for p2
         (filter #(contains? #{[[2]] [[6]]} (second %)))
             ;; we only want the indexes
         (mapv first)
             ;; we want their product
         (apply *))))

;
