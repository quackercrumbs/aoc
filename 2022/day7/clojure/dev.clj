(ns dev
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :as test :refer [deftest is]])
  (:import [java.lang Character]))

(defn inspect
  [x]
  (println x)
  x)

(defn parse-command
  [line]
  (let [[_ exe arg] (str/split line #" ")]
    {:exe exe
     :arg arg}))
(deftest test-parse-command
  (is (= {:exe "cd"
          :arg ".."}
         (parse-command "$ cd ..")))
  (is (= {:exe "cd"
          :arg "/"}
         (parse-command "$ cd /")))
  (is (= {:exe "cd"
          :arg "ee"}
         (parse-command "$ cd ee")))
  (is (= {:exe "cd"
          :arg "e"}
         (parse-command "$ cd e")))
  (is (= {:exe "ls"
          :arg nil}
         (parse-command "$ ls"))))

(defn parse-output-line
  [line]
  (let [[col-1 col-2] (str/split line #" ")]
    (if (= "dir" col-1)
      {:type :directory
       :name col-2}
      {:type :file
       :name col-2
       :size (edn/read-string col-1)})))
(deftest test-parse-output-line
  (is (= {:type :directory
          :name "directory-name"}
         (parse-output-line "dir directory-name")))
  (is (= {:type :file
          :name "filename"
          :size 123}
         (parse-output-line "123 filename"))))

(defn get-sample-data []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day7/sample.txt")]
                (doall (line-seq file)))]
    lines))

(defn get-command+outputs
  [lines]
  (let [{:keys [parsed curr-cmd+output]} (reduce (fn [{:keys [curr-cmd+output parsed]} line]
                                                   (let [cmd (:cmd curr-cmd+output)
                                                         output (:output curr-cmd+output)]
                                                     (if (= (first line) \$)
                                                       {:parsed (conj parsed curr-cmd+output)
                                                        :curr-cmd+output {:cmd (parse-command line)
                                                                          :output []}}
                                                       {:parsed parsed
                                                        :curr-cmd+output {:cmd cmd
                                                                          :output (conj output (parse-output-line line))}})))
                                                 {:parsed []}
                                                 lines)]
    (conj parsed curr-cmd+output)))
(deftest test-get-commands+outputs
  (let [lines
        ["$ cd /"
         "$ ls"
         "dir a"
         "14848514 b.txt"
         "8504156 c.dat"
         "dir d"
         "$ cd a"
         "$ ls"
         "dir e"
         "29116 f"
         "2557 g"
         "62596 h.lst"
         "$ cd e"
         "$ ls"
         "584 i"
         "$ cd .."
         "$ cd .."
         "$ cd d"
         "$ ls"
         "4060174 j"
         "8033020 d.log"
         "5626152 d.ext"
         "7214296 k"]]
    (is (= [nil
            {:cmd {:exe "cd", :arg "/"}, :output []}
            {:cmd {:exe "ls", :arg nil}, :output [{:type :directory, :name "a"} {:type :file, :name "b.txt", :size 14848514} {:type :file, :name "c.dat", :size 8504156} {:type :directory, :name "d"}]}
            {:cmd {:exe "cd", :arg "a"}, :output []}
            {:cmd {:exe "ls", :arg nil}, :output [{:type :directory, :name "e"} {:type :file, :name "f", :size 29116} {:type :file, :name "g", :size 2557} {:type :file, :name "h.lst", :size 62596}]}
            {:cmd {:exe "cd", :arg "e"}, :output []}
            {:cmd {:exe "ls", :arg nil}, :output [{:type :file, :name "i", :size 584}]}
            {:cmd {:exe "cd", :arg ".."}, :output []}
            {:cmd {:exe "cd", :arg ".."}, :output []}
            {:cmd {:exe "cd", :arg "d"}, :output []}
            {:cmd {:exe "ls", :arg nil}, :output [{:type :file, :name "j", :size 4060174} {:type :file, :name "d.log", :size 8033020} {:type :file, :name "d.ext", :size 5626152} {:type :file, :name "k", :size 7214296}]}]
           (get-command+outputs lines)))))

(defn print-tree
  [tree]
  (clojure.pprint/pprint tree))

(defn directory-tree
  [commands+outputs]
  (let [tree {"/" {:type :directory}}
        cwd (list)]
    (reduce (fn [{:as state :keys [tree cwd]} {:as cmd+output :keys [cmd output]}]
              #_(print-tree state)
              #_(println cmd+output)
              #_(println "----")
              (let [exe (:exe cmd)]
                (cond
                  (= "ls" exe) (let [r-cwd (reverse cwd)
                                     cwd-files (or (get-in tree r-cwd) {})
                                     new-cwd-files (->> (mapv (fn [{:keys [name] :as ls-item}]
                                                                [name (dissoc ls-item :name)])
                                                              output)
                                                        (into {})
                                                        (merge cwd-files))
                                     new-tree (assoc-in tree r-cwd new-cwd-files)]
                                 {:tree new-tree
                                  :cwd cwd})
                  (= "cd" exe) (let [new-cwd (cond
                                               (= ".." (:arg cmd)) (pop cwd)
                                               (= "/" (:arg cmd)) (list "/")
                                               (seq (:arg cmd)) (conj cwd (:arg cmd)))]
                                 {:tree tree
                                  :cwd new-cwd}))))
            {:tree tree
             :cwd cwd}
            commands+outputs)))

(defn update-tree-with-sizes
  "update directories in tree with their size"
  [tree path]
  (if (:size tree)
    tree
    ;; find size for current directory / node
    (do
      #_(println "children" (keys (get-in tree path)))
      (let [curr-tree (get-in tree path)
            children (-> (keys curr-tree)
                         set
                         (disj :type))
            result (reduce (fn [{:keys [running-size tree]} child-key]
                             (let [full-path (conj path child-key)
                                   child (get-in tree full-path)]
                               #_(println "full-path" full-path)
                               (if (:size child)
                                 {:running-size (+ (:size child) running-size)
                                  :tree tree
                                  :path path}
                                 (let [updated-tree (update-tree-with-sizes tree (conj full-path))
                                       child-size (get-in updated-tree (conj full-path :size) child)]
                                   #_(println "updated-tree" child-key full-path)
                                   #_(print-tree updated-tree)
                                   {:running-size (+ running-size child-size)
                                    :tree updated-tree
                                    :path path}))))
                           {:running-size 0
                            :tree tree
                            :path []}
                           children)]
        (assoc-in (:tree result) (conj path :size) (:running-size result))))))
(deftest test-update-tree-with-sizes
  (let [lines
        ["$ cd /"
         "$ ls"
         "dir a"
         "14848514 b.txt"
         "8504156 c.dat"
         "dir d"
         "$ cd a"
         "$ ls"
         "dir e"
         "29116 f"
         "2557 g"
         "62596 h.lst"
         "$ cd e"
         "$ ls"
         "584 i"
         "$ cd .."
         "$ cd .."
         "$ cd d"
         "$ ls"
         "4060174 j"
         "8033020 d.log"
         "5626152 d.ext"
         "7214296 k"]
        cmd+outputs (->> (get-command+outputs lines)
                         (drop 1))
        ret (directory-tree cmd+outputs)]
    ;; dir tree is correct
    (is (= {:tree {"/" {:type :directory, "a" {:type :directory, "e" {:type :directory, "i" {:type :file, :size 584}}, "f" {:type :file, :size 29116}, "g" {:type :file, :size 2557}, "h.lst" {:type :file, :size 62596}}, "b.txt" {:type :file, :size 14848514}, "c.dat" {:type :file, :size 8504156}, "d" {:type :directory, "j" {:type :file, :size 4060174}, "d.log" {:type :file, :size 8033020}, "d.ext" {:type :file, :size 5626152}, "k" {:type :file, :size 7214296}}}}, :cwd '("d" "/")} ret))

    (let [updated-tree (update-tree-with-sizes (:tree ret) [])]
      (is (= {"/" {:type :directory, "a" {:type :directory, "e" {:type :directory, "i" {:type :file, :size 584}, :size 584}, "f" {:type :file, :size 29116}, "g" {:type :file, :size 2557}, "h.lst" {:type :file, :size 62596}, :size 94853}, "b.txt" {:type :file, :size 14848514}, "c.dat" {:type :file, :size 8504156}, "d" {:type :directory, "j" {:type :file, :size 4060174}, "d.log" {:type :file, :size 8033020}, "d.ext" {:type :file, :size 5626152}, "k" {:type :file, :size 7214296}, :size 24933642}, :size 48381165}, :size 48381165} updated-tree)))))

(defn at-most-limit
  [size]
  (<= size 100000))

(defn problem-1*
  [tree]
  (if (nil? tree) 0
      (let [children (keys tree)
            result (reduce (fn [{:keys [running-size]} child-key]
                             (let [child (get tree child-key)]
                               (if (= :directory (:type child))
                                 (let [running-size (if (at-most-limit (:size child))
                                                      (+ running-size (:size child))
                                                      running-size)
                                       sub-tree-result (problem-1* child)]
                                   {:running-size (+ running-size sub-tree-result)})
                                 ;; ignore files
                                 {:running-size running-size})))
                           {:running-size 0}
                           children)]
        (:running-size result))))

(deftest test-sample-p1
  (let [lines
        ["$ cd /"
         "$ ls"
         "dir a"
         "14848514 b.txt"
         "8504156 c.dat"
         "dir d"
         "$ cd a"
         "$ ls"
         "dir e"
         "29116 f"
         "2557 g"
         "62596 h.lst"
         "$ cd e"
         "$ ls"
         "584 i"
         "$ cd .."
         "$ cd .."
         "$ cd d"
         "$ ls"
         "4060174 j"
         "8033020 d.log"
         "5626152 d.ext"
         "7214296 k"]
        cmd+outputs (->> (get-command+outputs lines)
                         (drop 1))
        ret (directory-tree cmd+outputs)]
    (let [updated-tree (update-tree-with-sizes (:tree ret) [])]
      (is (= 95437 (problem-1* updated-tree))))))

(defn problem-1
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day7/input.txt")]
                (doall (line-seq file)))]
    (let [cmd+outputs (->> (get-command+outputs lines)
                           (drop 1))
          tree (directory-tree cmd+outputs)
          tree (update-tree-with-sizes (:tree tree) [])]
      ;; NOTE: check problem-2, could replace all of problem-1 tbh
      ;;    just need to run the filter against result of p2
      (problem-1* tree))))
(time
 (problem-1)) ; 1667443
; 5ms

(defn get-directory-sizes
  [tree]
  (if (nil? tree) nil
      (let [children (keys tree)
            result (reduce (fn [{:keys [directories]} child-key]
                             (let [child (get tree child-key)]
                               (if (= :directory (:type child))
                                 (let [child-directory-size (:size child)
                                       directories (conj directories child-directory-size)
                                       sub-tree-result (get-directory-sizes child)]
                                   {:directories (into directories sub-tree-result)})
                                 ;; ignore files
                                 {:directories directories})))
                           {:directories []}
                           children)]
        (->> (:directories result)
             (sort)))))

(defn problem-2*
  [tree]
  (let [total-capacity    70000000
        required-capacity 30000000 ;; size of the update
        target-capacity   (- total-capacity required-capacity) ;; largest amount of space we can take up
        directory-sizes (get-directory-sizes tree)
        capacity-used (apply max directory-sizes)
        ;; assuming capacity-used > target-capacity
        ;; we need to free at least this much space to have room for the update
        to-free (- capacity-used target-capacity)]
    (some #(when (<= to-free %) %) directory-sizes)))

(deftest test-sample-p2
  (let [lines
        ["$ cd /"
         "$ ls"
         "dir a"
         "14848514 b.txt"
         "8504156 c.dat"
         "dir d"
         "$ cd a"
         "$ ls"
         "dir e"
         "29116 f"
         "2557 g"
         "62596 h.lst"
         "$ cd e"
         "$ ls"
         "584 i"
         "$ cd .."
         "$ cd .."
         "$ cd d"
         "$ ls"
         "4060174 j"
         "8033020 d.log"
         "5626152 d.ext"
         "7214296 k"]
        cmd+outputs (->> (get-command+outputs lines)
                         (drop 1))
        ret (directory-tree cmd+outputs)]
    (let [updated-tree (update-tree-with-sizes (:tree ret) [])]
      (is (= 24933642 (problem-2* updated-tree))))))

(defn problem-2
  []
  (let [lines (with-open [file (io/reader "/home/calvinq/projects/aoc/2022/day7/input.txt")]
                (doall (line-seq file)))]
    (let [cmd+outputs (->> (get-command+outputs lines)
                           (drop 1))
          tree (directory-tree cmd+outputs)
          tree (update-tree-with-sizes (:tree tree) [])]
      ;; NOTE: check problem-2, could replace all of problem-1 tbh
      ;;    just need to run the filter against result of p2
      (problem-2* tree))))
(time
 (problem-2)) ; 8998590
; 4.5 ms


