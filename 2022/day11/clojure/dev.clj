(ns dev
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [clojure.test :as test :refer [deftest is]])
  (:import [java.lang Character]))

(defn get-sample-input
  []
  (->>
   (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day11/sample.txt")]
     (doall (line-seq file)))
   (into [])))

(->> ["Test1" "Test2" "" "Test3"]
     (partition-by #(= "" %))
     (filter #(not= [""] %)))
(defn find-1-num
  [s]
  (->> (re-find #"\d+" s) edn/read-string))
(defn parse-lines-monkey-rules
  "There are five lines per monkey string"
  [lines]
  (let [monkey-id (->> (get lines 0) find-1-num)
        starting-items (->> (get lines 1) (re-seq #"\d+") (mapv edn/read-string))
        operation-raw (get lines 2)
        operation-op (->> operation-raw (re-find #"[\+\*]") keyword)
        [_ _ operation-arg-str] (->> operation-raw (re-find #".*(\+|\*) (\d+|old)"))
        operation-arg (if (number? (edn/read-string operation-arg-str))
                        (edn/read-string operation-arg-str)
                        (keyword operation-arg-str))
        test (->> (get lines 3) find-1-num)
        test-true (->> (get lines 4) find-1-num)
        test-false (->> (get lines 5) find-1-num)]
    {monkey-id {:items starting-items
                :operation {:op operation-op :arg operation-arg}
                :test test
                :true test-true
                :false test-false}}))
(deftest test-parse-input
  (let [test-string-1 ["Monkey 0"
                       "Starting items: 79, 98"
                       "Operation: new = old * 19"
                       "Test: divisble by 23"
                       "If true: throw to monkey 2"
                       "If false: throw to monkey 3"]
        test-string-2 ["Monkey 1"
                       "Starting items: 1, 2, 3"
                       "Operation: new = old + 6"
                       "Test: divisble by 19"
                       "If true: throw to monkey 2"
                       "If false: throw to monkey 0"]
        test-string-3 ["Monkey 3"
                       "Starting items: 5"
                       "Operation: new = old + old"
                       "Test: divisble by 13"
                       "If true: throw to monkey 1"
                       "If false: throw to monkey 3"]]
    (is (= {0 {:items [79 98]
               :operation {:op :* :arg 19}
               :test 23
               :true 2
               :false 3}}
           (parse-lines-monkey-rules test-string-1)))
    (is (= {1 {:items [1 2 3]
               :operation {:op :+ :arg 6}
               :test 19
               :true 2
               :false 0}}
           (parse-lines-monkey-rules test-string-2)))
    (is (= {3 {:items [5]
               :operation {:op :+ :arg :old}
               :test 13
               :true 1
               :false 3}}
           (parse-lines-monkey-rules test-string-3)))))

(defn get-op
  [op]
  (condp = op
        :* *
        :+ +))

(defn get-arg
  [self arg]
  (if (= :old arg)
    self
    arg))

(defn process-monkey
  [{items :items operation :operation test :test true-id :true false-id :false}]
  (reduce (fn [to-return item]
            (let [{:keys [op arg]} operation
                  op (get-op op)
                  arg (get-arg item arg)
                  ;; perform operation
                  modified-worry (op item arg)
                  ;; quot does floor division
                  modified-worry (quot modified-worry 3)]
              ;; test the new worry score and send to correct monkey
              (if (= 0 (mod modified-worry test))
                (update to-return true-id conj modified-worry)
                (update to-return false-id conj modified-worry))))
          {}
          items))
(deftest test-process-monkey
  (let [result (process-monkey {:items [79 98]
                                :operation {:op :* :arg 19}
                                :test 23
                                :true 2
                                :false 3})]
    (is (= {3 [620 500]} result)))
  ;; testing old * old
  (let [result (process-monkey {:items [10 20 30]
                                :operation {:op :* :arg :old}
                                :test 2
                                :true 1
                                :false 2})]
    (is (= {2 [133 33] 1 [300]} result))))

(defn process-monkeys
  [monkeys]
  (reduce (fn [monkeys monkey-id]
            ;; get current monkey
            ;; process monkey
            ;; update monkeys map with results from process monkey
            ;; Also update current monkey, (clear their items)
            )
          monkeys
          (range 1 (count monkeys))))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day11/input.txt")]
                (doall (line-seq file)))]

    (let [])))
