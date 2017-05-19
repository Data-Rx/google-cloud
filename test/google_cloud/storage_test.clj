(ns google-cloud.storage-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [cheshire.core :as cheshire]
            [google-cloud.storage :as storage]))

(def temp-file (java.io.File/createTempFile "gcs-test" ".tmp"))
(def test-data "google-cloud.storage_test")

(deftest upload-download
  (testing "uploading a file to Google Storage"
    (spit temp-file test-data)
    (let [response (storage/upload-file temp-file (env :google-storage-bucket-name))
          body     (cheshire/parse-string (:body @response))]
      (is (= true (true? (boolean (get body "mediaLink")))))))
  (testing "downloading a file from Google Storage"
    (let [response (storage/download-file (.getName temp-file) (env :google-storage-bucket-name))
          body     (:body @response)]
      (is (= true (not (string? body))))
      (if-not (string? body)
        (is (= test-data (apply str (map char (.bytes (:body @response))))))))))
