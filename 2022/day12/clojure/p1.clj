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

(defn print-grid
  [grid]
  (doseq [line grid]
    (doseq [c line]
      (if (some? c) (printf "%5s" c)
          (printf "%5s" "-")))
    (println)))

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

(defn get-neighbors
  [grid [pos-r pos-c]]
  (let [curr (get-in grid [pos-r pos-c])
        up (let [dir [(dec pos-r) pos-c]] (vector dir (get-in grid dir)))
        down (let [dir [(inc pos-r) pos-c]] (vector dir (get-in grid dir)))
        left (let [dir [pos-r (dec pos-c)]] (vector dir (get-in grid dir)))
        right (let [dir [pos-r (inc pos-c)]] (vector dir (get-in grid dir)))]
    (->> [up down left right]
         (filterv #(some? (second %)))
         (filterv #(>= 1 (- (second %) curr) #_(abs (- curr (second %)))))
         (mapv first))))

(comment
  (let [test-grid [[1 2 3]
                   [0 5 6]
                   [7 4 9]]]
    (get-neighbors test-grid [1 1])))
(defn create-blank-grid
  [grid]
  (let [_max-count (* (count grid) (count (first grid)))]
    (vec (for [_ grid]
           (vec (for [_ (first grid)]
                  nil))))))
(comment
  (let [test-grid [[1 2 3]
                   [4 5 6]
                   [7 8 9]]]
    (create-blank-grid test-grid)))

(defn find-smallest-unvisited-node
  [visited unvisited distance-grid]
  (let [unvisited-clean (set/difference unvisited visited)
        unvisisted-dist (->> unvisited-clean
                             (mapv (fn [n] [(get-in distance-grid n) n]))
                             (filter #(not (nil? (first %))))
                             (sort-by first >))]
    (if (empty? unvisisted-dist)
      nil
      (->> (last unvisisted-dist)
           second))))

(defn find-shortest-distance
  [_curr _target heights solution]
  (loop [{:keys [distance-grid visited unvisited] :as solution} solution]
    (let [curr (find-smallest-unvisited-node visited unvisited distance-grid)]
      (cond
        (empty? unvisited)
        solution

        (nil? curr)
        (assoc solution
               :unvisited (disj unvisited curr))

        (get visited curr)
        ;; pop from q, since we already processed this node
        (assoc solution
               :unvisited (disj unvisited curr))

        :else
        (let [curr-dist (get-in distance-grid curr)
              neighbors (get-neighbors heights curr)
              unvisited-neighbors (set (filterv #(nil? (get visited %)) neighbors))

              distance-grid (reduce (fn [distance-grid n]
                                      (if (not (get visited n))
                                        (let [n-distance (get-in distance-grid n)]
                                          (if (nil? n-distance)
                                            (assoc-in distance-grid n (inc curr-dist))
                                            (assoc-in distance-grid n (min (inc curr-dist) n-distance))))
                                        distance-grid))
                                    distance-grid
                                    unvisited-neighbors)
              #_#__ (do
                      (print-grid distance-grid)
                      (println "curr" curr)
                      (println "visited" visited)
                      (println "unvisited" unvisited-neighbors)
                      (println "------"))]
          (recur (assoc solution
                        :distance-grid distance-grid
                        :visited (conj visited curr)
                        :unvisited (into unvisited unvisited-neighbors))))))))

(defn p1
  [lines]
  (let [height-map (parse-height-map lines)
        {start-pos \S
         end-pos \E} (get-start-and-end lines)
        shortest-distance-grid (create-blank-grid height-map)
        shortest-distance-grid (assoc-in shortest-distance-grid start-pos 0)]
    (println "start" start-pos)
    (println "end" end-pos)
    (let [final
          (find-shortest-distance start-pos end-pos height-map {:distance-grid shortest-distance-grid
                                                                :visited #{}
                                                                :unvisited #{start-pos}})]
      #_final
      (get-in final (into [:distance-grid] end-pos)))))

(comment
  (print-grid (->> (p1 (get-sample-input))
                   :distance-grid)))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day12/input.txt")]
                (doall (line-seq file)))]
    (p1 lines)))
(comment
  (time (problem-1))) ; 520
; 5400ms
