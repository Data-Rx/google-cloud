(ns google-cloud.oauth
  (:require [environ.core :refer [env]]
            [clj-http.client :as http]
            [buddy.sign.jwt  :as jwt]
            [buddy.core.keys :as keys]
            [cheshire.core   :as ch]
            [clj-time.core   :as time]))

(def token-state (atom {:token nil :expires-at (time/now)}))

(defn get-token-from-google
  "@param {jwt} oauth-token
   @return oauth bearer token from google"
  [oauth-token]
  (-> (http/post "https://www.googleapis.com/oauth2/v4/token"
                 {:form-params {:grant_type "urn:ietf:params:oauth:grant-type:jwt-bearer"
                                :assertion  oauth-token}
                  :as           :json})
      :body
      :access_token))

(defn refresh-token
  "@side-effect Update the token-state with a new token and new expires-at timestamp"
  [scope]
  (if (nil? (env :google-application-credentials))
    (throw (Exception. "Unable to refresh the OAuth token because the Google Application Credentials are nil. During development set an environment variable named GOOGLE_APPLICATION_CREDENTIALS.  See https://github.com/GoogleCloudPlatform/google-cloud-java#authentication"))
    (let [creds       (ch/parse-string (slurp (env :google-application-credentials)) true)
          oauth-data  {:iss   (:client_email creds)
                       :scope (str "https://www.googleapis.com/auth/" scope)
                       :aud   "https://www.googleapis.com/oauth2/v4/token"
                       :exp   (time/plus (time/now) (time/hours 1))
                       :iat   (time/now)}
          oauth-token (jwt/sign oauth-data (keys/str->private-key (:private_key creds)) {:alg :rs256})
          fresh-token (get-token-from-google oauth-token)]
      (swap! token-state assoc :token fresh-token)
      (swap! token-state assoc :expires-at (time/plus (time/now) (time/minutes 55))))))

(defn get-token
  "@return A valid token from the token-state"
  ([]
   (get-token "pubsub"))
  ([scope]
   (when (time/after? (time/now) (:expires-at @token-state))
     (refresh-token scope))
   (:token @token-state)))
