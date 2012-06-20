(ns syve.fonts.core
  (:import [java.awt.image BufferedImage]
           [java.awt
            Font FontMetrics
            Graphics2D RenderingHints
            Color]))

(defprotocol AFont
  (width [this str] "Get the rendered width of the given string.")
  (height [this str] "Get the rendered height of the given string.")
  (line-height [this] "Get the max height of any line drawn by this font.")
  (render ^BufferedImage [this s] "Render the given string into a BufferedImage"))

(defprotocol ARenderCache
  "Provide a mechanism to generate the image of a rendered char.

   NOTE: Each ARenderCache is specific to a given font."
  (get-char [this c] "Return an image of the given char."))

(defn make-buffered-image-cache
  [^BufferedImage img char-map]
  (reify ARenderCache
    (get-char [_ c]
      (let [{x :x-pos, w :width, h :height} (get char-map c)]
        (.getSubimage img x 0 w h)))))

(defn char-size
  "Given a font and a char return a map containing :width and :height properties."
  [font c & anti-alias?]
  (let [gfx (doto (-> (BufferedImage. 1 1 BufferedImage/TYPE_INT_ARGB) .createGraphics)
              (.setFont font))]
    (when anti-alias?
      (.setRenderingHint gfx RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON))

    (let [metrics (.getFontMetrics gfx)]
      {:width  (max 1 (.charWidth metrics c))
       :height (if (<= (.getHeight metrics) 0) (.getSize font) (.getHeight metrics))})))

(defn render-char
  "Given a font and a char, return a BufferedImage depicting c in font."
  [font c & anti-alias?]
  (when c
    (let [{w :width, h :height} (char-size font c anti-alias?)
          img (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
          gfx (.createGraphics img)]
      (when anti-alias?
        (.setRenderingHint gfx RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON))

      (doto gfx
        (.setFont font)
        (.setColor Color/WHITE)
        (.drawString (str c) 0 (-> gfx .getFontMetrics .getAscent)))

      img)))

(defmacro debug
  [form]
  `(let [res# ~form]
     (println '~form "=>" res#)
     res#))

;; TODO: This could be done more efficiently, but it works as is, and
;; is probably fast enough. 
(defn create-render-cache
  "Render all the characters into an image and return that image."
  [font chars & anti-alias?]
  (let [{width :width, height :height} (reduce (fn [{w1 :width, h1 :height}
                                                   {w2 :width, h2 :height}]
                                                 {:width (+ w1 w2)
                                                  :height (max h1 h2)})
                                               (map #(char-size font % anti-alias?) chars))
        cache-img (BufferedImage. width height  BufferedImage/TYPE_INT_ARGB)
        gfx (.getGraphics cache-img)]

    (doto gfx
      (.setColor (Color. 255 255 255 1))
      (.fillRect 0 0 width height))

    ;; Render each char into the cache image while maintaining the
    ;; information about where in that image the char can be found.
    (loop [[char & chars] chars, char-map {}, x-pos 0]
      (if (nil? char)
        (make-buffered-image-cache cache-img char-map)
        (let [rendered-char (render-char font char anti-alias?)]
          (.drawImage gfx rendered-char x-pos 0 nil)
          (recur chars
                 (assoc char-map char {:x-pos x-pos
                                       :width (.getWidth rendered-char)
                                       :height (.getHeight rendered-char)})
                 (+ x-pos (.getWidth rendered-char))))))))