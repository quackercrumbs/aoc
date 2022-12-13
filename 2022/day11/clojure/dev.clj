(ns dev
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
   (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day11/sample.txt")]
     (doall (line-seq file)))
   (into [])))

(defn find-1-num
  [s]
  (->> (re-find #"\d+" s) edn/read-string))
(defn parse-lines-monkey-rules
  "There are five lines per monkey string"
  [lines]
  (let [monkey-id (->> (get lines 0) find-1-num)
        starting-items (->> (get lines 1) (re-seq #"\d+") (mapv edn/read-string) (mapv bigint))
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
                (update to-return true-id (fnil conj []) modified-worry)
                (update to-return false-id (fnil conj []) modified-worry))))
          {}
          items))
(deftest test-process-monkey
  (let [result (process-monkey {:items [79 98]
                                :operation {:op :* :arg 19}
                                :test 23
                                :true 2
                                :false 3})]
    (is (= {3 [500 620]} result)))
  ;; testing old * old
  (let [result (process-monkey {:items [10 20 30]
                                :operation {:op :* :arg :old}
                                :test 2
                                :true 1
                                :false 2})]
    (is (= {2 [33 133] 1 [300]} result))))

(defn process-monkeys
  [monkeys]
  (reduce (fn [monkeys monkey-id]
            (let [monkey (get monkeys monkey-id)
                  num-inspected (count (get-in monkeys [monkey-id :items]))
                  processed-monkey (process-monkey monkey)
                  monkeys (reduce-kv (fn [monkeys k v]
                                       (let [monkeys (update-in monkeys [k :items] into v)]
                                         monkeys))
                                     monkeys
                                     processed-monkey)
                  monkeys (assoc-in monkeys [monkey-id :items] [])
                  monkeys (update-in monkeys [monkey-id :num-inspected] (fnil + 0) num-inspected)]
              monkeys)
            )
          monkeys
          (range 0 (count monkeys))))

(defn calculate-monkey-business
  [lines rounds]
  (let [monkeys (->>
                 ;; partition by monkey spec
                 lines
                 (partition-by #(= "" %))
                 (filter #(not= [""] %))
                 (mapv #(into [] %)) ;; convert to vector to allow indexing lines in a monkey spec
                 ;; parse monkey spec
                 (mapv parse-lines-monkey-rules)
                 (apply merge) ;; combine all monkey specs 
                 )

        monkeys (reduce (fn [{:keys [monkeys]} _round_counter]
                          (let [new-monkeys (process-monkeys monkeys)]
                            {:monkeys new-monkeys}))
                        {:monkeys monkeys}
                        (range rounds))
        monkey-num-inspected (->> monkeys
                                  :monkeys
                                  keys
                                  (mapv #(get-in monkeys [:monkeys % :num-inspected]))
                                  (sort >))
        monkey-business (apply * (take 2 monkey-num-inspected))]
    (assoc monkeys
           :monkey-business monkey-business)))

(deftest test-sample-p1
  (is (= 10605 (->> (calculate-monkey-business (get-sample-input) 20)
                    :monkey-business))))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day11/input.txt")]
                (doall (line-seq file)))]
    (calculate-monkey-business lines 20)))
(time (->> (problem-1)
             :monkey-business))
; 99852
; 2.6ms


