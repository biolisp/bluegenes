(ns redgenes.sections.saveddata.events
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [com.rpl.specter.macros :refer [traverse select transform]])
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx dispatch]]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [imcljs.filters :as im-filters]
            [cljs-uuid-utils.core :as uuid]
            [cljs-time.core :as t]
            [imcljs.operations :as operations]
            [com.rpl.specter :as s]))


(defn get-parts
  [model query]
  (group-by :type
            (distinct
              (map (fn [path]
                     (assoc {}
                       :type (im-filters/im-type model path)
                       :path (str (im-filters/trim-path-to-class model path) ".id")))
                   (:select query)))))


(reg-event-db
  :saved-data/calculate-parts
  (fn [db]
    (let [model (get-in db [:assets :model])
          items (get-in db [:saved-data :items])])
    db))

(reg-event-fx
  :saved-data/toggle-edit-mode
  (fn [{db :db}]
    {:db       (update-in db [:saved-data :list-operations-enabled] not)
     :dispatch [:saved-data/calculate-parts]}))

(reg-event-fx
  :save-data
  (fn [{db :db} [_ data]]
    (let [new-id (str (uuid/make-random-uuid))
          model  (get-in db [:assets :model])]
      {:db       (assoc-in db [:saved-data :items new-id]
                           (-> data
                               (merge {:created (t/now)
                                       :updated (t/now)
                                       :parts   (get-parts model (:value data))
                                       :id      new-id})))
       :dispatch [:open-saved-data-tooltip
                  {:label (:label data)
                   :id    new-id}]})))

(reg-event-db
  :open-saved-data-tooltip
  (fn [db [_ data]]
    (assoc-in db [:tooltip :saved-data] data)))

(reg-event-db
  :save-saved-data-tooltip
  (fn [db [_ id label]]
    (-> db
        (assoc-in [:saved-data :items id :label] label)
        (assoc-in [:tooltip :saved-data] nil))))

(reg-event-db
  :saved-data/set-type-filter
  (fn [db [_ kw]]
    (let [clear? (> 1 (count (get-in db [:saved-data :editor :selected-items])))]
      (if clear?
        (update-in db [:saved-data :editor] dissoc :filter)
        (assoc-in db [:saved-data :editor :filter] kw)))))

(reg-event-db
  :saved-data/toggle-editable-item
  (fn [db [_ id path-info]]
    (let [loc               [:saved-data :editor :selected-items]
          datum-description (merge {:id id} path-info)]
      (if (empty? (filter #(= % datum-description) (get-in db loc)))
        (update-in db loc (fnil conj []) datum-description)
        (update-in db loc (fn [items] (into [] (remove #(= % datum-description) items))))))))

(reg-event-db
  :saved-data/perform-operation
  (fn [db]
    (let [selected-items (first (get-in db [:saved-data :editor :items]))]
      (let [i (seq selected-items)]
        (println "I" i))
      (let [q1 (assoc
                 (get-in db [:saved-data :items (first selected-items) :value])
                 :select [(:path (second selected-items))]
                 :orderBy nil)]
        (.log js/console q1)
        #_(go (println "done" (<! (operations/operation
                                    {:root "www.flymine.org/query"}
                                    q1 q1)))))
      db)))

(reg-event-db
  :saved-data/set-text-filter
  (fn [db [_ value]]
    (if (= value "")
      (update-in db [:saved-data :editor] dissoc :text-filter)
      (assoc-in db [:saved-data :editor :text-filter] value))))

(reg-event-db
  :saved-data/toggle-keep
  (fn [db [_ id]]
    (let [loc [:saved-data :editor :selected-items]]
      (update-in db loc
                 (partial map
                          (fn [item] (if (= id (:id item))
                                       (update-in item [:keep :self] not)
                                       item)))))))

(reg-event-db
  :saved-data/toggle-keep-intersections
  (fn [db [_ id]]
    (let [current-value true]
      (update-in db [:saved-data :editor :items]
                 (fn [items]
                   (let [selected? (some? (some true? (select [s/ALL s/LAST :keep :intersection] items)))]
                     (transform [s/MAP-VALS]
                                (fn [item]
                                  (assoc-in item [:keep :intersection] (not selected?))) items)))))))





