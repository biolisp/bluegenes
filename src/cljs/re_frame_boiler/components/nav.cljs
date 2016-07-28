(ns re-frame-boiler.components.nav
  (:require [re-frame.core :as re-frame :refer [subscribe]]))

(defn main []
  (let [active-panel (subscribe [:active-panel])
        app-name     (subscribe [:name])
        panel-is     (fn [panel-key] (= @active-panel panel-key))]
    (fn []
      [:nav.navbar.navbar-default.navbar-fixed-top.down-shadow
       [:div.container-fluid
        [:div.navbar-header
         [:a.navbar-brand {:href "/#"} @app-name]]
        [:ul.nav.navbar-nav
         [:li {:class (if (panel-is :home-panel) "active")} [:a {:href "/#"} "Home"]]
         [:li {:class (if (panel-is :about-panel) "active")} [:a {:href "/#/about"} "About"]]
         [:li {:class (if (panel-is :debug-panel) "active")} [:a {:href "/#/debug"} "Debug"]]]
        [:ul.nav.navbar-nav.navbar-right
         [:li [:a {:href "/#"} [:i.fa.fa-cog]]]
         [:li [:a {:href "/#"} [:i.fa.fa-user-times]]]]]])))