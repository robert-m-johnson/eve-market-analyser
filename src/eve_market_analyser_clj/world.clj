(ns eve-market-analyser-clj.world
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set]))

(def types
  "A map of the type IDs and their corresponding item names"
  (with-open [rdr (io/reader (io/resource "types.csv"))]
    (let [lines (line-seq rdr)
          pairs (map
                 (fn [line]
                   ;; Split the CSV lines by the pipe | symbol
                   (let [[id-str name] (str/split line #"\|")]
                     ;; Parse the ID string into an int
                     [(java.lang.Integer/parseInt id-str) name]))
                 lines)]
      ;; Create a map of the ID-name pairs
      (->> pairs flatten (apply hash-map)))))

(def regions
  "A map of the region IDs and their corresponding names"
  {10000054 "Aridia"
   10000069 "Black Rise"
   10000055 "Branch"
   10000007 "Cache"
   10000014 "Catch"
   10000051 "Cloud Ring"
   10000053 "Cobalt Edge"
   10000012 "Curse"
   10000035 "Deklein"
   10000060 "Delve"
   10000001 "Derelik"
   10000005 "Detorid"
   10000036 "Devoid"
   10000043 "Domain"
   10000039 "Esoteria"
   10000064 "Essence"
   10000027 "Etherium Reach"
   10000037 "Everyshore"
   10000046 "Fade"
   10000056 "Feythabolis"
   10000058 "Fountain"
   10000029 "Geminate"
   10000067 "Genesis"
   10000011 "Great Wildlands"
   10000030 "Heimatar"
   10000025 "Immensea"
   10000031 "Impass"
   10000009 "Insmother"
   10000052 "Kador"
   10000049 "Khanid"
   10000065 "Kor-Azor"
   10000016 "Lonetrek"
   10000013 "Malpais"
   10000042 "Metropolis"
   10000028 "Molden Heath"
   10000040 "Oasa"
   10000062 "Omist"
   10000021 "Outer Passage"
   10000057 "Outer Ring"
   10000059 "Paragon Soul"
   10000063 "Period Basis"
   10000066 "Perrigen Falls"
   10000048 "Placid"
   10000047 "Providence"
   10000023 "Pure Blind"
   10000050 "Querious"
   10000008 "Scalding Pass"
   10000032 "Sinq Laison"
   10000044 "Solitude"
   10000022 "Stain"
   10000041 "Syndicate"
   10000020 "Tash-Murkon"
   10000045 "Tenal"
   10000061 "Tenerifis"
   10000038 "The Bleak Lands"
   10000033 "The Citadel"
   10000002 "The Forge"
   10000034 "The Kalevala Expanse"
   10000018 "The Spire"
   10000010 "Tribute"
   10000003 "Vale of the Silent"
   10000015 "Venal"
   10000068 "Verge Vendor"
   10000006 "Wicked Creek"})

(def ^:private region-names->ids
  (clojure.set/map-invert regions))

(def ^:private empire-regions
  "A set of the empire regions IDs and names"
  (let [empire-reg-names
        ["Aridia"
         "Black Rise"
         "The Bleak Lands"
         "The Citadel"
         "Derelik"
         "Devoid"
         "Domain"
         "Essence"
         "Everyshore"
         "The Forge"
         "Genesis"
         "Heimatar"
         "Kador"
         "Khanid"
         "Kor-Azor"
         "Lonetrek"
         "Metropolis"
         "Molden Heath"
         "Placid"
         "Sinq Laison"
         "Solitude"
         "Tash-Murkon"
         "Verge Vendor"]]
    (->> (select-keys region-names->ids empire-reg-names)
         seq
         flatten
         (into #{}))))

(def trade-hub-region-names
  "A vector of the trade hub region names, in descending order of importance"
  ["The Forge"
   "Domain"
   "Heimatar"
   "Sinq Laison"
   "Metropolis"])

(defn empire-region?
  "Returns whether x is the ID or name of a region in empire space"
  [x]
  (empire-regions x))
