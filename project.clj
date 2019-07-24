(defproject genekim/google-cloud "0.3.2"
  :description "A library of functions for working with Google Cloud."
  ;:dependencies [[org.clojure/clojure "1.9.0"]
  ;               [environ "1.1.0"]
  ;               [http-kit "2.3.0"]
  ;               [clj-time "0.14.4"]
  ;               [lock-key "1.5.0"]
  ;               [clj-http "3.9.1"]
  ;               [cheshire "5.8.0"]
  ;               [buddy/buddy-sign "3.0.0"]
  ;               [buddy/buddy-core "1.5.0"]
  ;               [com.taoensso/timbre "4.10.0"]]
  :plugins [[lein-cljfmt "0.5.6"]
            [lein-environ "1.1.0"]
            [lein-ancient "0.6.15"]
            [lein-localrepo "0.5.4"]
            [lein-tools-deps "0.4.1"]]

  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]}
            
  :profiles {:default-env {:env {}}
             :common-dev  {:source-paths ["src" "dev"]}
             :dev         [:common-dev :default-env :profile-env]
             :test        [:common-dev :default-env :profile-env]
             :uberjar     {:aot :all}}
  ; needed for Java 9+
  ; https://www.deps.co/blog/how-to-upgrade-clojure-projects-to-use-java-9/
  :jvm-opts ~(let [version     (System/getProperty "java.version")
                   [major _ _] (clojure.string/split version #"\.")]
               (if (>= (java.lang.Integer/parseInt major) 9)
                 ["--add-modules" "java.xml.bind"]
                 [])))
