(ns p2
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :as test :refer [deftest is]])
  (:import [java.lang Character]))

(defn get-sample-input
  []
  (->>
   (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day9/sample2.txt")]
     (doall (line-seq file)))
   (into [])))
(comment (get-sample-input)) ; ["R 4" "U 4" "L 3" "D 1" "R 4" "D 1" "L 5" "R 2"]
; ["R 5" "U 8" "L 8" "D 3" "R 17" "D 10" "L 25" "U 20"]

(def sample-input
  ["R 4"
   "U 4"
   "L 3"
   "D 1"
   "R 4"
   "D 1"
   "L 5"
   "R 2"])

(def sample-input-2
  ["R 5"
   "U 8"
   "L 8"
   "D 3"
   "R 17"
   "D 10"
   "L 25"
   "U 20"])

(def initial-state
  {:nodes (into [] (for [_ (range 10)] {:x 0 :y 0}))})

(defn make-pos
  [n]
  (if (> 0 n)
    (* -1 n)
    n))

(defn get-modifier
  [x]
  (if (> 0 x)
    dec
    inc))

(defn find-new-pos
  "First arg is start
  Second arg is end"
  [{x1 :x y1 :y} {x2 :x y2 :y}]
  (let [x-diff (- x1 x2)
        y-diff (- y1 y2)]
    (if
     (or (>= 1 (make-pos x-diff) (make-pos y-diff))
         (and (= 1 (make-pos x-diff)) (= 0 y-diff))
         (and (= 0 x-diff) (= 1 (make-pos y-diff))))
      ;; we are still touching (or overlapping, when diff == 0 for both)
      {:x x2 :y y2}
      (if (= x1 x2)
        ;; move vertically (since y is different)
        {:x x2 :y ((get-modifier y-diff) y2)}
        (if (= y1 y2)
          ;; move horizontally (since x is different)
          {:x ((get-modifier x-diff) x2) :y y2}
          ;; columns are different, so we gotta move diagonally towards goal
          (let [x-delta (get-modifier x-diff)
                y-delta (get-modifier y-diff)]
            {:x (x-delta x2) :y (y-delta y2)}))))))

(deftest test-in-range
  (is (= {:x 1 :y 1} (find-new-pos {:x 0 :y 0} {:x 1 :y 1})))
  (is (= {:x 0 :y 1} (find-new-pos {:x 0 :y 0} {:x 0 :y 1})))
  (is (= {:x 1 :y 0} (find-new-pos {:x 0 :y 0} {:x 1 :y 0})))
  (is (= {:x 0 :y 0} (find-new-pos {:x 0 :y 0} {:x 0 :y 0})))
  (is (= {:x 0 :y 0} (find-new-pos {:x 1 :y 1} {:x 0 :y 0})))
  ;; have to move

  ;; diagonal
  (is (= {:x 1 :y 1} (find-new-pos {:x 2 :y 1} {:x 0 :y 0})))
  (is (= {:x 1 :y 1} (find-new-pos {:x 1 :y 2} {:x 0 :y 0})))
  (is (= {:x 1 :y 0} (find-new-pos  {:x 0 :y 0} {:x 2 :y 1})))
  (is (= {:x 0 :y 1} (find-new-pos  {:x 0 :y 0} {:x 1 :y 2})))

  ;; horiztonally / vertically

  (is (= {:x 1 :y 0} (find-new-pos {:x 2 :y 0} {:x 0 :y 0})))
  (is (= {:x 0 :y 1} (find-new-pos {:x 0 :y 2} {:x 0 :y 0}))))
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
  [nodes {:as _action :keys [direction steps]}]
  (let [{:keys [nodes history] :as final-state}
        (reduce (fn [{:keys [nodes history] :as _state} _step]
                  (let [[head & _other-nodes] nodes
                        new-head
                        (condp = direction
                          "R" (update head :x inc)
                          "L" (update head :x dec)
                          "U" (update head :y inc)
                          "D" (update head :y dec))
                        ;; update all nodes if it is out of range
                        new-nodes (reduce (fn [final-nodes [index curr-node]]
                                            (let [final-prev-node (get final-nodes (dec index))
                                                  new-pos (find-new-pos final-prev-node curr-node)]
                                              (conj final-nodes new-pos)))
                                          [new-head]
                                          (->> (map-indexed vector nodes)
                                               (drop 1)))]
                    {:nodes new-nodes
                     :history (conj history new-nodes)}))
                ;; initial state
                {:nodes nodes
                 :history [nodes]}
                (range steps))]
    final-state))

(deftest test-process-action
  (is (= {:nodes [{:x 4, :y 0} {:x 3, :y 0} {:x 2, :y 0} {:x 1, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0}], :history [[{:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0}] [{:x 1, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0}] [{:x 2, :y 0} {:x 1, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0}] [{:x 3, :y 0} {:x 2, :y 0} {:x 1, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0}] [{:x 4, :y 0} {:x 3, :y 0} {:x 2, :y 0} {:x 1, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0} {:x 0, :y 0}]]}
         (process-action (into [] (for [_x (range 9)] {:x 0 :y 0})) {:direction "R" :steps 4}))))

(defn process-actions
  [{:keys [nodes] :as state} actions]
  (let [{:keys [_nodes _history _long-history] :as final-state}
        (reduce (fn [{:keys [long-history history nodes]} action]
                  (let [{new-nodes :nodes
                         action-history :history} (process-action nodes action)]
                    {:nodes new-nodes
                     :history (conj history new-nodes)
                     ;; Note, action-history will contain current head and tail
                     ;;  so there are duplicate entries.
                     ;;  This shouldn't matter for this problem thou
                     :long-history (into long-history action-history)}))
                (assoc state
                       :history [nodes]
                       :long-history [nodes])
                actions)]
    final-state))

(deftest test-process-actions
  (let [{:as _final-state :keys [nodes history long-history]}
        (process-actions initial-state [{:direction "R" :steps 4}
                                        {:direction "U" :steps 4}
                                        {:direction "L" :steps 3}
                                        {:direction "D" :steps 1}
                                        {:direction "R" :steps 4}
                                        {:direction "D" :steps 1}
                                        {:direction "L" :steps 5}
                                        {:direction "R" :steps 2}])]
    (println "results")
    (println nodes)))

(defn problem-2*
  "Given a collection of string actions.
  Runs each action and counts the number of positions the tail has been."
  [lines]
  (let [actions (mapv parse-action lines)
        {:as final-state :keys [nodes _history _long-history]}
        (process-actions initial-state actions)]
    (->> final-state
         ;; find all tail positions
         :long-history
         (mapv last)
         ;; there are duplicates
         set
         ;; count em
         count)))
(deftest test-problem-2-sample
  (is (= 1 (problem-2* sample-input)))
  (is (= 36 (problem-2* sample-input-2))))

(defn problem-2
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day9/input.txt")]
                (doall (line-seq file)))]

    (problem-2* lines)))
(time (problem-2)) ; 2661
; 70ms



