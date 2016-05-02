(ns eve-market-analyser-clj.views
  (:require [hiccup.core :as hicc]
            [hiccup.page :as hicp]
            [clojure.string :as st]
            [eve-market-analyser-clj.format :as fmt]
            [clj-time.coerce :as tcoerce]))

(defn render [h]
  (hicp/html5 h))

(defn layout [content]
  (list
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:title "Eve Market Analyzer"]
    [:meta {:name "description" :content "Eve Market Analyzer"}]
    [:meta {:name "author" :content "Robert Johnson"}]
    [:link {:rel "stylesheet" :href "css/styles.css"}]]
   [:body content]))

(defn hub-item-search-form []
  [:form {:action "/hub-item" :method "GET"}
   [:input {:type "text" :name "itemName" :method "GET" :autofocus "autofocus"}]
   [:input {:type "submit" :value "Submit"}]])

(defn index []
  (layout (hub-item-search-form)))

(defn hub-item
  [{:keys [itemName hubItems earliestGenerated]}]
  (layout
   (list
    (hub-item-search-form)
    [:p itemName]
    (if (empty? hubItems)
      [:p "No data found"]
      (list
       [:table
        [:tr [:th "Hub"] [:th "Selling Price"] [:th "Buying Price"]]
        (map
         (fn [item]
           [:tr
            [:td (:regionName item)]
            [:td
             {:class (st/join
                      " "
                      [(if (:highestSellingPrice item) "highestSellingPrice")
                       (if (:lowestSellingPrice item) "lowestSellingPrice")])}
             (fmt/price (:sellingPrice item))]
            [:td
             {:class (st/join
                      " "
                      [(if (:highestBuyingPrice item) "highestBuyingPrice")
                       (if (:lowestBuyingPrice item) "lowestBuyingPrice")])}
             (fmt/price (:buyingPrice item))]])
         hubItems)]
       [:p (fmt/time-ago (tcoerce/to-long earliestGenerated))])))))
