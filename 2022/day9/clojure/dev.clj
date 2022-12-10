(ns dev
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :as test :refer [deftest is]])
  (:import [java.lang Character]))

(defn get-sample-input
  []
  (->>
   (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day9/sample.txt")]
     (doall (line-seq file)))
   (into [])))
(comment (get-sample-input)) ; ["R 4" "U 4" "L 3" "D 1" "R 4" "D 1" "L 5" "R 2"]

(def sample-input
  ["R 4"
   "U 4"
   "L 3"
   "D 1"
   "R 4"
   "D 1"
   "L 5"
   "R 2"])

(def initial-state
  {:head {:x 0 :y 0}
   :tail {:x 0 :y 0}})

(defn make-pos
  [n]
  (if (> 0 n)
    (* -1 n)
    n))

(defn in-range
  [{x1 :x y1 :y} {x2 :x y2 :y}]
  (let [x-dist (-> (- x1 x2) make-pos)
        y-dist (-> (- y1 y2) make-pos)
        min-distance 1]
    (and (>= min-distance x-dist) (>= min-distance y-dist))))
(deftest test-in-range
  (is (in-range {:x 0 :y 0} {:x 1 :y 1}))
  (is (in-range {:x 0 :y 0} {:x 0 :y 1}))
  (is (in-range {:x 0 :y 0} {:x 1 :y 0}))
  (is (in-range {:x 0 :y 0} {:x 0 :y 0}))
  (is (in-range {:x 1 :y 1} {:x 0 :y 0})))

(defn parse-action
  [line]
  (let [action  (-> line
                    (str/split #" "))]
    {:direction (get action 0)
     :steps (->> (get action 1)
                 edn/read-string)}))
(deftest test-parse-action
  (is (= {:direction "R"
          :steps 4} (parse-action "R 4"))))

(defn process-action
  "for a given action returns the final state along with the movement history
  :head, final head position
  :tail, final tail position
  :history, for each step take, includes the position of previous state
    NOTE, this includes the starting position 
  "
  [head tail {:as _action :keys [direction steps]}]
  (let [{:keys [head tail history] :as _final-state}
        (reduce (fn [{:keys [head tail history] :as _state} _step]
                  (let [new-head
                        (condp = direction
                          "R" (update head :x inc)
                          "L" (update head :x dec)
                          "U" (update head :y inc)
                          "D" (update head :y dec))
                  ;; update tail if it is out of range
                        new-tail (if (not (in-range new-head tail))
                                   head
                                   tail)]
                    {:prev-head head
                     :prev-tail tail
                     :head new-head
                     :tail new-tail
                     :history (conj history {:head head
                                             :tail tail})}))
          ;; initial state
                {:head head
                 :tail tail
                 :prev-head head
                 :prev-tail tail
                 :history []}
                (range steps))]
    {:head head
     :tail tail
     :history (conj history {:head head
                             :tail tail})}))

(deftest test-process-action
  (is (= {:head {:x 4 :y 0}
          :tail {:x 3 :y 0}
          :history [{:head {:x 0 :y 0}
                     :tail {:x 0 :y 0}}
                    {:head {:x 1 :y 0}
                     :tail {:x 0 :y 0}}
                    {:head {:x 2 :y 0}
                     :tail {:x 1 :y 0}}
                    {:head {:x 3 :y 0}
                     :tail {:x 2 :y 0}}
                    {:head {:x 4 :y 0}
                     :tail {:x 3 :y 0}}]}
         (process-action {:x 0 :y 0} {:x 0 :y 0} {:direction "R" :steps 4}))))

(defn process-actions
  [state actions]
  (let [{:keys [head tail history long-history]} (reduce (fn [{:keys [long-history history head tail]} action]
                                                           (let [updated-history (conj history {:head head :tail tail})
                                                                 {head :head
                                                                  tail :tail
                                                                  action-history :history} (process-action head tail action)]
                                                             {:head head
                                                              :tail tail
                                                              :history updated-history
                                                 ;; Note, action-history will contain current head and tail
                                                 ;;  so there are duplicate entries.
                                                 ;;  This shouldn't matter for this problem thou
                                                              :long-history (into long-history action-history)}))
                                                         (assoc state
                                                                :history []
                                                                :long-history [])
                                                         actions)]
    {:head head
     :tail tail
     :history (conj history {:head head :tail tail})
     :long-history long-history}))
(deftest test-process-actions
  (let [{:as _final-state :keys [head tail _history _long-history]}
        (process-actions initial-state [{:direction "R" :steps 4}
                                        {:direction "U" :steps 4}
                                        {:direction "L" :steps 3}
                                        {:direction "D" :steps 1}
                                        {:direction "R" :steps 4}
                                        {:direction "D" :steps 1}
                                        {:direction "L" :steps 5}
                                        {:direction "R" :steps 2}])]
    (is (= {:x 2 :y 2}
           head))
    (is (= {:x 1 :y 2}
           tail))))

(defn problem-1*
  "Given a collection of string actions.
  Runs each action and counts the number of positions the tail has been."
  [lines]
  (let [actions (mapv parse-action lines)
        {:as final-state :keys [head tail _history _long-history]}
        (process-actions initial-state actions)]
    (->> final-state
         ;; find all tail positions
         :long-history
         (mapv :tail)
         ;; there are duplicates
         set
         ;; count em
         count)))
(deftest test-problem-1-sample
  (is (= 13 (problem-1* sample-input))))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day9/input.txt")]
                (doall (line-seq file)))]

    (problem-1* lines)))
(time (problem-1)) ; 6284
; 41ms



