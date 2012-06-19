(ns syve.fonts.core-test
  (:use [clojure test repl pprint reflect]
        syve.fonts.core
        clojure.java.javadoc)
  (:require [clojure.java.io :as io])
  (:import [java.awt GraphicsEnvironment Font]
           [javax.imageio ImageIO]))

(defn get-fonts
  ([size]
     (let [size (float size)]
       (map #(.deriveFont % size) (get-fonts))))
  ([]
     (seq (.getAllFonts (GraphicsEnvironment/getLocalGraphicsEnvironment)))))


(defn write-image
  [img filename]
  (ImageIO/write img "png" (io/as-file filename)))
