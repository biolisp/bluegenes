(ns bluegenes.sections.mymine.views.main
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [oops.core :refer [oget ocall]]))

(defn update-form [atom key evt]
  (swap! atom assoc key (oget evt :target :value)))

(defn login-form []
  (let [credentials (r/atom {:username nil :password nil})]
    (fn []
      [:form
       [:pre (str @credentials)]
       [:div.form-group
        [:label "Email Address"]
        [:input.form-control
         {:type "text"
          :value (:username @credentials)
          :on-change (partial update-form credentials :username)}]]
       [:div.form-group
        [:label "Password"]
        [:input.form-control
         {:type "password"
          :value (:password @credentials)
          :on-change (partial update-form credentials :password)}]]
       [:button.btn.btn-primary.btn-raised
        {:type "button"
         :on-click (fn [] (dispatch [:bluegenes.events.auth/login @credentials]))}
        "Sign In"]
       [:button.btn.btn-default.btn-raised
        {:type "button"
         :on-click (fn [] (dispatch [:bluegenes.events.auth/register @credentials]))}
        "Register"]])))

(defn main []
  (fn []
    [:div.container
     [:h1 "MyMine"]
     [:div.panel
      [:div.panel-body
       [login-form]]]]))