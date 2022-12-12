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
   (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day10/sample.txt")]
     (doall (line-seq file)))
   (into [])))
(comment (get-sample-input))

(def initial-state
  {:register 1
   :cycle 0})

(defn parse-instruction
  [line]
  (let [instruction-parts (-> line
                              (str/split #" "))
        instruction (get instruction-parts 0)
        arg (get instruction-parts 1)]
    (condp = instruction
      "addx" {:instruction :addx
              :arg (edn/read-string arg)
              :cycles 2}
      "noop" {:instruction :noop
              :cycles 1})))
(deftest test-parse-instruction
  (is (= {:instruction :addx
          :arg 20
          :cycles 2} (parse-instruction "addx 20")))
  (is (= {:instruction :addx
          :arg -1
          :cycles 2} (parse-instruction "addx -1")))
  (is  (= {:instruction :noop
           :cycles 1} (parse-instruction "noop"))))

(defn process-instructions
  [initial-state instructions]
  (reduce (fn [{:keys [_cpu _history] :as acc} {:keys [cycles instruction arg] :as _instruction}]
            (let [;; perform the empty cycles for the instruction
                  after-instruction
                  (reduce (fn [{:keys [history cpu]} _cycle]
                            (let [new-cpu (update cpu :cycle inc)]
                              {:history (conj history new-cpu)
                               :cpu new-cpu}))
                          acc
                          ;; 1 less cycle, since the actual instruction is processed at the end
                          (range (dec cycles)))
                  ;; do one more iteration to apply the actual instruction
                  new-state (condp = instruction
                              :addx (update-in after-instruction [:cpu :register] (fn [prev] (+ prev arg)))
                              :noop after-instruction
                              :else "shouldn't be possible...")
                  ;; add cpu state to history
                  new-state (update-in new-state [:cpu :cycle] inc)
                  new-state (update new-state :history conj (:cpu new-state))]
              new-state))
          (hash-map :cpu initial-state
                    :history [initial-state])
          instructions))
(deftest test-process-instructions
  (let [lines ["addx 20" "noop" "addx 10" "noop" "addx -31"]
        instructions (mapv parse-instruction lines)
        final-state (process-instructions initial-state instructions)]
    (is (= 8 (get-in final-state [:cpu :cycle])))
    (is (= 0 (get-in final-state [:cpu :register])))))

(deftest test-process-instructions-sample
  (let [lines (get-sample-input)
        instructions (mapv parse-instruction lines)
        {:keys [_cpu history]} (process-instructions initial-state instructions)]
    ;; Note: index 0 == cycle 1
    (is (= 21 (-> (get history (dec 20))
                  :register)))
    (is (= 19 (-> (get history (dec 60))
                  :register)))
    (is (= 18 (-> (get history (dec 100))
                  :register)))
    (is (= 21 (-> (get history (dec 140))
                  :register)))
    (is (= 16 (-> (get history (dec 180))
                  :register)))
    (is (= 18 (-> (get history (dec 220))
                  :register)))))

(defn signal-strength
  [{:as _cpu+history :keys [cpu history]}]
  (->> (mapv (fn [cycle]
               ;; Note: inc, since we're 0-indexed
               ;;   Multiply cycle with register
               (* (inc cycle) (->> (get history cycle)
                                   :register)))
             ;; index 19 = cycle 20, we want ever 40th cycle
             (range 19 (get cpu :cycle) 40))
       (apply +)))

(deftest test-signal-strength-sample
  (let [lines (get-sample-input)
        instructions (mapv parse-instruction lines)
        {:keys [_cpu _history] :as cpu+history} (process-instructions initial-state instructions)]
    (is (= 13140 (signal-strength cpu+history)))))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day10/input.txt")]
                (doall (line-seq file)))]

    (let [instructions (mapv parse-instruction lines)
          cpu+history (process-instructions initial-state instructions)]
      (signal-strength cpu+history))))
(time (problem-1)) ; 14760
; 0.77ms

(defn draw-crt
  [{:as _cpu+history :keys [history]}]
  (reduce (fn [{:keys [crt]} {:keys [_cycle register]}]
            (let [;; each line is 40 wide
                  crt-pos (mod (count crt) 40)
                  ;; sprite is 3 pixels wide
                  ;; register determiens the middle index 
                  sprite-range (hash-set (dec register) register (inc register))]
              (if (get sprite-range crt-pos)
                {:crt (conj crt "#")}
                {:crt (conj crt ".")})))
          {:crt []}
          (into [] history)))

(defn problem-2
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day10/input.txt")]
                (doall (line-seq file)))]
    (let [instructions (mapv parse-instruction lines)
          cpu+history (process-instructions initial-state instructions)]
      (draw-crt cpu+history))))
(defn print-crt
  [{:keys [crt]}]
  (let [lines (partition 40 crt)]
    (doseq [line lines]
      (doseq [pixel line]
        (print pixel))
      (println))))
(time (print-crt (problem-2)))
; EFGERURE
;; 3.8ms
