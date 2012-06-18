(defproject syve.fonts "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[s3-wagon-private "1.1.1"]]
  :repositories {"releases" "s3p://syve-maven/releases/"
                 "snapshots" "s3p://syve-maven/snapshots/"}
  :dependencies [[org.clojure/clojure "1.4.0"]])
