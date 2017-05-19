(ns google-cloud.oauth-test
  (:require [clojure.test :refer :all]
            [clj-time.core             :as time]
            [cheshire.core             :as ch]
            [buddy.sign.jwt            :as jwt]
            [buddy.core.keys           :as keys]
            [google-cloud.oauth :as sut]))

(def dummy-creds (ch/generate-string {:client_email "a@a.com" :private_key "dummy"}))

(deftest retrieving-token-from-google

  (testing "The refresh-token function updates the token-state"
    (with-redefs [sut/get-token-from-google (fn [_] "new-token")
                  slurp                     (fn [_] dummy-creds)
                  jwt/sign                  (fn [& args] "XXX")
                  keys/str->private-key     (fn [_] "YYY")]
      (is (= nil (:token @sut/token-state)))
      (sut/refresh-token "pubsub")
      (is (= "new-token" (:token @sut/token-state))))))

(deftest retrieving-local-token

  (testing "The get-oauth-token function refreshes token-state when existing token has expired"
    (let [call-counter (atom 0)]
      (with-redefs [sut/refresh-token (fn [_] (swap! call-counter inc))
                    slurp                    (fn [_] dummy-creds)
                    jwt/sign                 (fn [& args] "XXX")
                    keys/str->private-key    (fn [_] "YYY")]
        (swap! sut/token-state assoc :expires-at (time/minus (time/now) (time/minutes 5)))
        (sut/get-token)
        (is (= 1 @call-counter)))))

  (testing "The get-oauth-token function does not refresh token-state when existing token is still valid"
    (let [call-counter (atom 0)]
      (with-redefs [sut/refresh-token (fn [] (swap! call-counter inc))
                    slurp                    (fn [_] dummy-creds)
                    jwt/sign                 (fn [& args] "XXX")
                    keys/str->private-key    (fn [_] "YYY")]
        (swap! sut/token-state assoc :expires-at (time/plus (time/now) (time/minutes 5)))
        (sut/get-token)
        (is (= 0 @call-counter))))))
