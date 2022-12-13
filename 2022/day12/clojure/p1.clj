(ns p1
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [clojure.test :as test :refer [deftest is]])
  (:import [java.lang Character
            System]))

(defn get-sample-input
  []
  (->>
   (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day12/sample.txt")]
     (doall (line-seq file)))
   (into [])))

(defn parse-height-map
  [lines]
  (->> lines
       (mapv (fn [line] (->> (str/split line #"")
                             (mapv (fn [c-str] (let [c (first c-str)
                                                     c (if (= c \S)
                                                         \a
                                                         (if (= c \E)
                                                           \z
                                                           c))]
                                                 (int c))))
                             (mapv (fn [c-int] (- c-int (int \a)))))))))
(defn get-start-and-end
  [lines]
  (->> lines
       (map-indexed (fn [row-idx line]
                      (map-indexed (fn [col-idx c]
                                     (when (get #{\S \E} c)
                                       [c [row-idx col-idx]]))
                                   line)))
       (apply concat [])
       (filter (comp not nil?))
       (into {})))

(comment
  (parse-height-map (get-sample-input))
  (is (= {\S [0 0]
          \E [2 5]}
         (get-start-and-end (get-sample-input)))))
(defn p1
  [lines]
  (let [height-map (parse-height-map lines)
        {[start-row start-col] \S
         [end-row end-col] \E} (get-start-and-end lines)]
    (println "height-map" height-map)
    (println "start" start-row start-col)
    (println "end" end-row end-col)))
(comment
  (p1 (get-sample-input)))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day12/input.txt")]
                (doall (line-seq file)))]))
