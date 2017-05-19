(ns google-cloud.storage
  (:require [org.httpkit.client :as http]
            [google-cloud.oauth :as oauth]))

(defn upload-data-as
  "Uploads the data to the bucket storing it with the name upload-name."
  ([data bucket upload-name]
   (upload-data-as data bucket upload-name "application/octet-stream"))
  ([data bucket upload-name content-type]
   (let [token (oauth/get-token "devstorage.read_write")
         url (str "https://www.googleapis.com/upload/storage/v1/b/" bucket "/o?uploadType=media&name=" upload-name)
         options {:oauth-token token
                  :headers {"Content-Type" content-type}
                  :body data}]
     (http/post url options))))

(defn upload-file-as
  "Uploads the file to the bucket storing it with the name upload-name."
  ([file bucket upload-name]
   (upload-file-as file bucket upload-name "application/octet-stream"))
  ([file bucket upload-name content-type]
   (upload-data-as (slurp file) bucket upload-name content-type)))

(defn upload-file
  "Uploads file to bucket.  If no content-type is given then
  'application/octet-stream' is used."
  ([file bucket]
   (upload-file file bucket "application/octet-stream"))
  ([file bucket content-type]
   (upload-file-as file bucket (.getName file) content-type)))

(defn download-file
  "Downloads the filename from the bucket and returns the httpkit response.

  When successful `(:body @response)` will contain the file's data."
  [filename bucket]
  (let [token (oauth/get-token "devstorage.read_write")
        url (str "https://www.googleapis.com/storage/v1/b/" bucket "/o/" filename "?alt=media")
        options {:oauth-token token}]
    (http/get url options)))
