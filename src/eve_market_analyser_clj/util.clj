(ns eve-market-analyser-clj.util)

(defn apply-or-default
  "Applies the function f to the sequence s. If s is nil or empty,
  then returns the given default instead"
  [f default s]
  (if (or (not s) (empty? s))
    default
    (apply f s)))

;; Still about 10x slower than separate reductions
(defn reduce-multi
  "Given a sequence of fns and a coll, returns a vector of the result of each fn
  when reduced over the coll. The reduction is performed in a single pass."
  [fns coll]
  (let [n (count fns)
        r (rest coll)
        initial-v (transient (into [] (repeat n (first coll))))
        fns (into [] fns)
        reduction-fn
        (fn [v x]
          (loop [acc v, i 0]
            (let [y (nth acc i)
                  f (nth fns i)]
              (if (= i (- n 1))
                (assoc! acc i (f y x))
                (recur (assoc! acc i (f y x)) (inc i))))))]
    (persistent! (reduce reduction-fn initial-v r))))
