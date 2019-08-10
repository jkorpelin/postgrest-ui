(ns postgrest-ui.components.form
  (:require [postgrest-ui.impl.registry :as registry]
            [postgrest-ui.elements :refer [element]]
            [postgrest-ui.impl.schema :as schema]
            [postgrest-ui.display :as display]
            [reagent.core :as r])
  (:require-macros [postgrest-ui.impl.state :refer [define-stateful-component]]))


(defn- column-input [style defs table column value-atom]
  (let [{:strs [type format]} (schema/column-info defs table column)]
    (element style :text-input {:label (display/label table column)
                                :type type
                                :format format
                                :value-atom value-atom})))

(defn- foreign-key-link [style defs table {:keys [link-to]}]
  [:div "LINK-TO" link-to])

(define-stateful-component form [{:keys [endpoint table layout style
                                         header-fn footer-fn]}]
  {:state state}
  (let [defs @(registry/load-defs endpoint)
        current-state @state]
    (if-not defs
      (element style :loading-indicator)
      [:<>
       [:pre (pr-str defs)]
       (when header-fn
         (header-fn current-state))
       (doall
        (map-indexed
         (fn [i {:keys [group label columns]}]
           (with-meta
             (element style :form-group
                      label
                      (for [column columns]
                        (cond
                          (and (map? column) (:link-to column))
                          (foreign-key-link style defs table column)

                          :else
                          (column-input style defs table column
                                        (r/wrap (get-in current-state [:data column])
                                                #(swap! state assoc-in [:data column] %))))))
             {:key i}))
         layout))
       [:div "this is the form"]
       (when footer-fn
         (footer-fn current-state))])))
