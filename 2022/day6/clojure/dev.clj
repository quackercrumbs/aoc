(ns dev
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :as test :refer [deftest is]])
  (:import [java.lang Character]))

(defn inspect
  [x]
  (do (println x) x))

(defn find-signal-marker-pos
  [len s]
  (let [{:as ret :keys [seen-map]} (->> (take len s)
                                        (reduce (fn [{:keys [seen-map]} c]
                                                  {:seen-map (update seen-map c (fn [old-val]
                                                                                  (+ 1 (or old-val 0))))})
                                                {:seen-map {}}))]
    (->> (drop len s)
         (reduce (fn [{:as acc :keys [left right seen-map]} c]
                   #_(println acc)
                   (if (= len (count (keys seen-map)))
                     (reduced right)
                     (let [to-delete-char (get s left)
                           to-delete-char-count (get seen-map to-delete-char)
                           c-count (or (get seen-map c) 0)
                           ;; remove to-delete-char when it's count will go to 0
                           seen-map (if (not= to-delete-char c)
                                      (if (= to-delete-char-count 1)
                                        (-> (dissoc seen-map to-delete-char)
                                            (assoc c (inc c-count)))
                                        (-> (assoc seen-map to-delete-char (dec to-delete-char-count))
                                            (assoc c (inc c-count))))
                                      seen-map)]
                       {:left (inc left)
                        :right (inc right)
                        :seen-map seen-map})))
                 {:left 0
                  :right len 
                  :seen-map seen-map}))))
(deftest test-signal-marker-pos
  (let [f (partial find-signal-marker-pos 4)]
    (is (= 7 (f "mjqjpqmgbljsphdztnvjfqwrcgsmlb")))
    (is (= 5 (f "bvwbjplbgvbhsrlpgdmjqwftvncz")))
    (is (= 6 (f "nppdvjthqldpwncqszvftbrmjlhg")))
    (is (= 10 (f "nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg")))
    (is (= 11 (f "zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw")))))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day6/input.txt")]
                (doall (line-seq file)))]
    (->> lines
         first
         (find-signal-marker-pos 4))))

(time
 (problem-1)) ; 1287
;; 1.8ms

(deftest test-signal-marker-pos-p2
  (let [f (partial find-signal-marker-pos 14)]
    (is (= 19 (f "mjqjpqmgbljsphdztnvjfqwrcgsmlb")))
    (is (= 23 (f "bvwbjplbgvbhsrlpgdmjqwftvncz")))
    (is (= 23 (f "nppdvjthqldpwncqszvftbrmjlhg")))
    (is (= 29 (f "nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg")))
    (is (= 26 (f "zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw")))))
(defn problem-2
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day6/input.txt")]
                (doall (line-seq file)))]
    (->> lines
         first
         (find-signal-marker-pos 14))))
(time (problem-2)) ; 3716
; 7.4ms

