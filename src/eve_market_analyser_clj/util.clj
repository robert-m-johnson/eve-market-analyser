(ns eve-market-analyser-clj.util)

(defn apply-or-default
  "Applies the function f to the sequence s. If s is nil or empty,
  then returns the given default instead"
  [f default s]
  (if (or (not s) (empty? s))
    default
    (apply f s)))
