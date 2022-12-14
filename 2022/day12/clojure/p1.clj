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
         (filter #(some? (second %)))
         ;; TODO: switch to other condition commented out
         (filter #(>= 1 (- (second %) curr) #_(abs (- curr (second %)))))
         ;; TODO: split list to neighbors 1 distance up
         ;; TODO: split list to neighbors that are downwards
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

(defn find-shortest-distance
  [curr target heights {:as solution :keys [distance-grid visited]}]

  (do (println "-----")
        (println curr visited)
        (print-grid distance-grid))
  (cond
    ;; already visited this node
    (get visited curr)
    ;; check if there are any smallest neighbors
    (do (println "already visited:" curr)
        solution)

    ;; haven't visited this node before
    :else
    (let [neighbors (set (get-neighbors heights curr))
          not-visited
          (set/difference neighbors visited)
          _ (do (println "not-visited" not-visited)
                (println "neighbors" neighbors))

          ;; updated visited
          visited (conj visited curr)

          ;; for all the known neighbors, find shortest path
          smallest-neighbor-dist (->> (mapv (fn [pos] (get-in distance-grid pos)) neighbors)
                                      (filter some?)
                                      (apply min 10000000000000))
          curr-dist (get-in distance-grid curr)
          ;; TODO: only update curr-dist with neighbors that are up climbs, ignore downclimb visited nodes
          ;; update curr-dist with smallest known neighbor 
          distance-grid (assoc-in distance-grid curr (min (inc smallest-neighbor-dist) curr-dist))
          ;; refetch curr-dist, if it was updated
          curr-dist (get-in distance-grid curr)
          new-dist-for-neighbor (inc curr-dist)
          ;; update neighbor distances with smallest distance
          distance-grid (reduce (fn [distance-grid pos]
                                  (let [pos-height (get-in distance-grid pos)]
                                    (if (nil? pos-height)
                                      (assoc-in distance-grid pos new-dist-for-neighbor)
                                      (assoc-in distance-grid pos (min pos-height new-dist-for-neighbor)))))
                                distance-grid
                                neighbors)]

      (cond (= target curr)
            (do (println "hit")
                {:distance-grid distance-grid
                 :visited (conj visited curr)})

            ;; traverse to neighbors
            :else
            (reduce (fn [solution neighbor]
                      (find-shortest-distance neighbor target heights solution))
                    {:distance-grid distance-grid
                     :visited visited}
                    not-visited)))))

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
                                                                :visited #{}})]
      (println "result" (get-in final (into [:distance-grid] end-pos)))
      final
      #_(get-in final (into [:distance-grid] start-pos)))))

(comment
  (print-grid (->> (p1 (get-sample-input))
                   :distance-grid)))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day12/input.txt")]
                (doall (line-seq file)))]
    (p1 lines)))
(comment
  (print-grid (->> (problem-1)
                   :distance-grid)))
