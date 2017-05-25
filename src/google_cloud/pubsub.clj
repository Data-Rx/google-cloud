(ns google-cloud.pubsub
  (:require [environ.core :refer [env]]
            [lock-key.core :as lock]
            [cheshire.core :as ch]
            [clj-http.client :as http]
            [taoensso.timbre :as timbre]
            [google-cloud.oauth :as oauth]))

(def google-api "https://pubsub.googleapis.com/v1/projects/")

(defn creds [] (ch/parse-string (slurp (env :google-application-credentials)) true))

(defn create-topic
  "@param {String} topic
   @side-effect Will create a new topic haveing that name
   @returns HTTP Response. If successful, the response body contains a newly
    created instance of Topic"
  [topic]
  (http/put (str google-api (:project_id (creds)) "/topics/" topic)
            {:oauth-token (oauth/get-token) :body ""}))

(defn delete-topic
  "@param {String} topic
   @side-effect Delete existing topic with argument name
   @returns HTTP Response. If successful, the response body will be empty"
  [topic]
  (http/delete (str google-api (:project_id (creds)) "/topics/" topic)
               {:oauth-token (oauth/get-token)}))

(defn create-subscription
  "@param {String} topic
   @param {String} subscription
   @side-effect Creates a new subscription in the given topic
   @returns HTTP Response. If successful, the response body contains a newly
    created instance of Subscription"
  [topic subscription]
  (http/put (str google-api (:project_id (creds)) "/subscriptions/" subscription)
            {:oauth-token (oauth/get-token)
             :body        (ch/generate-string {"topic"              (str "projects/" (:project_id (creds)) "/topics/" topic)
                                               "ackDeadlineSeconds" 300})}))
(defn delete-subscription
  "@param {String} subscription
   @side-effect Delete existing subscription with argument name
   @returns HTTP Response. If successful, the response body will be empty"
  [subscription]
  (http/delete (str google-api (:project_id (creds)) "/subscriptions/" subscription)
               {:oauth-token (oauth/get-token)}))
(defn publish
  "@param {String} topic
   @param {Vector} messages
   @param {String} crypto-key
   @side-effect Publish the given messages to the given topic
   @returns HTTP Response. If successful, the response body will contain the
    following object `{'messageIds': [string]}`"
  [topic messages crypto-key]
  (http/post (str google-api (:project_id (creds)) "/topics/" topic ":publish")
             {:oauth-token (oauth/get-token)
              :body        (ch/generate-string {"messages" (map (fn [m] {"data" (lock/encrypt-as-base64 m crypto-key)}) messages)})}))

(defn ^:private decrypt-message
  "@param {Map} message has the following structure {:ackID XXX :message {:data asd123 :messageId 1234}}
   @param {String} crypto-key
   @returns a map containing {:ackId :messageId :message-contents}"
  [message crypto-key]
  (let [message-contents (lock/decrypt-from-base64 (:data (:message message)) crypto-key)]
    {:ackId (:ackId message) :messageId (:messageId (:message message)) :message-contents message-contents}))

(defn get-messages
  "@param {String} subscription
   @param {Integer} max-num-of-messages <crypto-key> string.
   @param {String} crypto-key
   @return List of decrypted messages from the specified subscription"
  [subscription max-num-of-messages crypto-key]
  (let [raw-messages (-> (http/post (str google-api (:project_id (creds)) "/subscriptions/" subscription ":pull")
                                    {:oauth-token (oauth/get-token)
                                     :body        (ch/generate-string {"maxMessages" max-num-of-messages})
                                     :as :json})
                         :body
                         :receivedMessages)]
    (if raw-messages
      (map #(decrypt-message % crypto-key) raw-messages)
      false)))

(defn ack-message
  "@param {String} subscription
   @param {Integer} ackId
   @returns HTTP Response. If successful, the response body will be empty"
  [subscription ackId]
  (http/post (str google-api (:project_id (creds)) "/subscriptions/" subscription ":acknowledge")
             {:oauth-token (oauth/get-token)
              :body        (ch/generate-string {"ackIds" [ackId]})}))

(defn process-subscription-messages
  "@param {String} subscription
   @param {Function} callback
   @param {String} crypto-key
   @side-effect Infinitely process messages for a given <subscription> passing
    each one to the callback
   @returns Nothing ever"
  [subscription callback crypto-key]
  (loop []
    (let [messages (get-messages subscription 200 crypto-key)]
      (when messages
        (do (timbre/debug (str "Pulled " (count messages) " from pubsub"))
            (doseq [m messages]
              (try
                (callback m)
                (ack-message subscription (:ackId m))
                (catch Exception e (str "Message not processed successfully: " m)))))
        (recur)))))
