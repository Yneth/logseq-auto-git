#!/usr/local/bin/bb
(require '[clojure.java.shell :refer [sh]] '[babashka.process :refer [process]])

(def home (System/getProperty "user.home"))

(defn run-proc [args]
  @(process args {:out :inherit :err :inherit}))


(defn run-rsync []
  (run-proc ["rsync" "-Pav" (str home "/notes/") (str home "/Library/Mobile Documents/iCloud~com~logseq~logseq/Documents")]))

(defn ok? [proc-res]
  (and proc-res (= (:exit proc-res) 0)))

(defn run-auto-git [timeout-sec]
  (while true
    (let [wait-time-ms (* (or (when timeout-sec (Integer/parseInt timeout-sec)) 60) 1000)

          _ (run-proc ["git" "add" "-A"])

          _ (run-proc ["git" "status"])

          _ (run-proc ["git" "commit" "-m" "autogit: update"])

          push-res
          (run-proc ["git" "push" "--no-verify" "origin" "master"])

          pull-res
          (run-proc ["git" "push" "--no-verify" "origin" "master"])

          notif-res
          (when-not (ok? pull-res)
            (run-proc ["osascript" "-e" "display notification \"autogit failed\""]))

          _ (when (and (ok? push-res) (ok? pull-res)) (run-rsync))]
      (Thread/sleep wait-time-ms))))

(run-auto-git (first *command-line-args*))

