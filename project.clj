(defproject net.data-rx/google-cloud "0.2.0"
  :description "A library of functions for working with Google Cloud."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [environ "1.1.0"]
                 [http-kit "2.2.0"]
                 [clj-time "0.12.2"]
                 [lock-key "1.4.1"]
                 [clj-http "2.3.0"]
                 [buddy/buddy-sign "1.2.0"]
                 [buddy/buddy-core "1.1.1"]]
  :plugins [[lein-cljfmt "0.5.6"]
            [lein-environ "1.1.0"]]
  :profiles {:default-env {:env {}}
             :common-dev  {:source-paths ["src" "dev"]}
             :dev         [:common-dev :default-env :profile-env]
             :test        [:common-dev :default-env :profile-env]
             :uberjar     {:aot :all}})
