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

(defn pretty-print-stacks
  [state]
  (let [num-stacks (count (keys state))]
    (reduce (fn [_ n]
              (let [stack (get state n)]
                (println n ":" stack)))
            nil
            (range 1 (+ 1 num-stacks)))))

(defn calculate-num-stacks
  "Assumes line adheres to a specific format"
  [line]
  (let [line (str line " ")]
    (quot (count line) 4)))
(deftest test-calculate-num-stacks
  (is (= 3 (calculate-num-stacks "    [D]    ")))
  (is (= 3 (calculate-num-stacks "[N] [C]    ")))
  (is (= 3 (calculate-num-stacks "[Z] [M] [P]"))))

(defn add-line-state
  "state = {0 (vector \"A\")
            1 (vector)}
   line  = (  vector \"B\"     \"C\")
  
  Results in:
  (0 (vector \"A\" \"B\")
   1 (vector \"C\"))

  Where: B is added to the end
  "
  [state line]
  (->> (reduce (fn [{:keys [state index] :as acc} letter]
                 (if letter
                   (let [stack (get state index)]
                     {:state (assoc state index (conj stack letter))
                      :index (inc index)})
                   (assoc acc :index (inc index))))
               {:state state :index 1}
               line)
       :state))
(deftest test-add-line-state
  (is (= {1 (vector "A" "X")}
         (add-line-state {1 (vector "A")}
                         (vector "X"))))
  (is (= {1 (vector "A" "X")
          2 (vector)
          3 (vector "Z")}
         (add-line-state {1 (vector "A") 2 (vector) 3 (vector)}
                         (vector "X"          nil       "Z")))))

(defn parse-initial-state-line
  [line]
  (reduce (fn [acc letter-chunk]
            (let [[_ letter] letter-chunk]
              (if (not= \space letter)
                (conj acc letter)
                (conj acc nil))))
          (vector)
          ;; pad extra space, keeps things easier to partition
          (->> (str line " ")
               (partition 4))))
;; test partitioning with padding with extra space
#_(partition 4 (str "[A] [B] " " "))
(deftest test-parse-initial-state-line
  (is (= (vector \A \A \A \A) (parse-initial-state-line "[A] [A] [A] [A] ")))
  (is (= (vector nil \A \A \A) (parse-initial-state-line "    [A] [A] [A] ")))
  (is (= (vector nil \A \A nil) (parse-initial-state-line "    [A] [A]    "))))

(defn create-initial-state
  [n]
  (reduce (fn [acc n]
            (if (< n 1)
              acc
              (assoc acc n (vector))))
          {}
          (range 1 (inc n))))
(deftest test-create-initial-state
  (is (= {1 [] 2 []} (create-initial-state 2))))

(defn parse-initial-state
  [lines]
  (let [number-of-stacks (calculate-num-stacks (first lines))
        ;; remove last line with the numbers
        lines (take (-  (count lines) 1) lines)]
    (reduce (fn [state line]
              (let [line-state (parse-initial-state-line line)]
                (add-line-state state line-state)))
            (create-initial-state number-of-stacks)
            lines)))
(deftest test-parse-initial-state
  (is (= {1 [\N \Z]
          2 [\D \C \M]
          3 [\P]}
         (parse-initial-state (vector "    [D]    "
                                      "[N] [C]    "
                                      "[Z] [M] [P]"
                                      " 1   2   3 ")))))

(defn parse-actions
  [lines]
  (->> lines
       (mapv (fn [line] (->> (re-find #"move (\d+) from (\d+) to (\d+)" line)
                             ;; ignore first match since it'll be the full match
                             (drop 1)
                             (mapv edn/read-string))))
       (mapv (fn [[amount src dest]]
               {:amount amount
                :src src
                :dest dest}))))
(deftest test-parse-actions
  (is (= [[1 2 1]
          [3 1 3]
          [2 2 1]
          [1 1 2]
          [10 3 5]]
         (->> (parse-actions (vector "move 1 from 2 to 1"
                                     "move 3 from 1 to 3"
                                     "move 2 from 2 to 1"
                                     "move 1 from 1 to 2"
                                     "move 10 from 3 to 5"))
              (mapv (fn [{:keys [amount src dest]}] (vector amount src dest)))))))

(defn parse-data
  [lines]
  (let [[initial-state-lines action-lines] (->> lines
                                                ;; parse input
                                                (partition-by #(= % ""))
                                                (filter #(not= % [""])))]
    (vector
     (parse-initial-state initial-state-lines)
     (parse-actions action-lines))))

(deftest test-parse-data
  (let [test-str ["    [D]    "
                  "[N] [C]    "
                  "[Z] [M] [P]"
                  " 1   2   3 "
                  ""
                  "move 1 from 2 to 1"
                  "move 3 from 1 to 3"
                  "move 2 from 2 to 1"
                  "move 1 from 1 to 2"]
        [initial-state actions] (parse-data test-str)]
    (is (= {1 [\N \Z]
            2 [\D \C \M]
            3 [\P]} initial-state))
    (is (= [[1 2 1]
            [3 1 3]
            [2 2 1]
            [1 1 2]]
           (->> actions
                (mapv (fn [{:keys [amount src dest]}] (vector amount src dest))))))))

(defn run-actions
  [init-state actions]
  (reduce (fn [state {:keys [amount src dest] :as action}]
            #_(println "State:")
            #_(pretty-print-stacks state)
            #_(println "Action:" action)
            (let [src-stack (get state src)
                  dest-stack (get state dest)
                  elems-to-move (reverse (take amount src-stack))
                  new-dest-stack (concat elems-to-move dest-stack)
                  new-src-stack (drop amount src-stack)]
              #_(println "--------------")
              (assoc state
                     src new-src-stack
                     dest new-dest-stack)))
          init-state
          actions))

(defn get-answer
  [state]
  (reduce (fn [final-string n]
            (str final-string (->> (get state n)
                                   first)))
          ""
          (range 1 (+ 1 (count (keys state))))))

(deftest test-run-actions
  (let [test-str ["    [D]    "
                  "[N] [C]    "
                  "[Z] [M] [P]"
                  " 1   2   3 "
                  ""
                  "move 1 from 2 to 1"
                  "move 3 from 1 to 3"
                  "move 2 from 2 to 1"
                  "move 1 from 1 to 2"]
        [initial-state actions] (parse-data test-str)
        final-state (run-actions initial-state actions)]
    (is (= "CMZ" (get-answer final-state)))))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day5/input.txt")]
                (doall (line-seq file)))]
    (let [[initial-state actions] (->> lines
                                       parse-data)
          final-state (run-actions initial-state actions)
          answer (get-answer final-state)]
      answer)))

(time
 (problem-1)) ; "SPFMVDTZT"
;; 147.915 ms

