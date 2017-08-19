(ns browsercookie.core
  (:gen-class)
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io]))

(import (javax.crypto SecretKeyFactory Cipher KeyGenerator SecretKey)
        (javax.crypto.spec IvParameterSpec PBEKeySpec SecretKeySpec))

(defn build-decryption-key
  "Given a chrome raw key, construct a decryption key for the cookies"
  [raw-key]
  (let [salt (.getBytes "saltysalt")]
  (-> (SecretKeyFactory/getInstance "pbkdf2withhmacsha1")
      (.generateSecret (PBEKeySpec. raw-key salt 1003 128))
      (.getEncoded)
      (SecretKeySpec. "AES"))))

(defn decrypt [aeskey text]
  "Decrypt AES encrypted text given an aes key"
  (let [ivsbytes (-> (repeat 16 " ") (clojure.string/join) (.getBytes))
        iv       (IvParameterSpec. ivsbytes)
        cipher   (Cipher/getInstance "AES/CBC/PKCS5Padding")
        _        (.init cipher Cipher/DECRYPT_MODE aeskey iv)]
    (String. (.doFinal cipher text))))

(defn is-encrypted?
  "Returns true if the provided encrypted value (bytes[]) is an encrypted
  value by Chrome that is, if it starts with v10"
  [cookie]
  (-> cookie
      (:encrypted_value)
      (String. "UTF-8")
      (clojure.string/starts-with? "v10")))

(defn decrypt-cookie
  "Given a cookie, return a cookie with decrypted value"
  [aes-key cookie]
  (-> cookie
      (assoc :value
             (if (is-encrypted? cookie)
               ;; Drop 3 removes leading "v10"
               (->> cookie (:encrypted_value) (drop 3) (byte-array) (decrypt aes-key))
               (-> cookie (:value))))
      (dissoc :encrypted_value)))


(defn get-chrome-rawkey-osx
  "Get the chrome raw decryption key using the `security` cli on OSX"
  []
  (-> (shell/sh "security" "find-generic-password" "-a" "Chrome" "-w" :out-enc "UTF-8")
      (:out)
      (clojure.string/trim)
      (.toCharArray)))

(defn temp-file-copy-of
  "Create a copy of file as a temp file that will be removed when
  the program exits"
  [file]
  (let [tempfile (java.io.File/createTempFile "store" "db")]
    (.deleteOnExit tempfile)
    (io/copy (io/file file) (io/file tempfile))
    tempfile))

(def cookies-file-osx
  "Return the path to the cookie store on OSX"
  (str
   (System/getProperty "user.home")
   "/Library/Application Support/Google/Chrome/Default/Cookies"))

(defn -main
  [& args]
  (if (not= 1 (count args))
    (.println *err* "Usage ./program <site-url>")
    (let [site-url (first args)
          aes-key (build-decryption-key (get-chrome-rawkey-osx))
          db-spec {:dbtype "sqlite" :dbname (temp-file-copy-of cookies-file-osx)}
          query (str "select * from cookies where host_key like '" site-url "'")
          cookies (jdbc/query db-spec [query])]
      (println
       (json/write-str
        (map (partial decrypt-cookie aes-key) cookies)))
      (System/exit 0))))
